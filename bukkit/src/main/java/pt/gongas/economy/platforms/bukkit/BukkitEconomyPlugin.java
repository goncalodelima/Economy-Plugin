/*
 *
 *  * This file is part of Economy-Plugin - https://github.com/goncalodelima/Economy-Plugin
 *  * Copyright (c) 2025 goncalodelima and contributors
 *  *
 *  * This program is free software: you can redistribute it and/or modify
 *  * it under the terms of the GNU General Public License as published by
 *  * the Free Software Foundation, either version 3 of the License, or
 *  * (at your option) any later version.
 *  *
 *  * This program is distributed in the hope that it will be useful,
 *  * but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  * GNU General Public License for more details.
 *  *
 *  * You should have received a copy of the GNU General Public License
 *  * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 *
 */

package pt.gongas.economy.platforms.bukkit;

import co.aikar.commands.BukkitCommandManager;
import org.bstats.bukkit.Metrics;
import pt.gongas.database.Database;
import pt.gongas.database.DatabaseType;
import pt.gongas.database.connection.CustomDatabaseConnection;
import pt.gongas.database.credentials.impl.DatabaseCredentialsImpl;
import org.bukkit.plugin.java.JavaPlugin;
import org.redisson.api.RTopic;
import pt.gongas.economy.platforms.bukkit.command.EconomyCommand;
import pt.gongas.economy.platforms.bukkit.listener.PlayerListener;
import pt.gongas.economy.platforms.bukkit.runnable.PlayerBalanceRunnable;
import pt.gongas.economy.platforms.bukkit.view.RankingView;
import pt.gongas.economy.shared.currency.service.CurrencyFoundationService;
import pt.gongas.economy.shared.messaging.Messaging;
import pt.gongas.economy.platforms.bukkit.messaging.MessagingBungee;
import pt.gongas.economy.shared.messaging.MessagingRedis;
import pt.gongas.economy.shared.currency.Currency;
import pt.gongas.economy.platforms.bukkit.model.currency.loader.BukkitCurrencyLoader;
import pt.gongas.economy.shared.currency.service.CurrencyService;
import pt.gongas.economy.shared.messaging.TransactionMessage;
import pt.gongas.economy.shared.user.User;
import pt.gongas.economy.shared.user.service.UserFoundationService;
import pt.gongas.economy.shared.user.service.UserService;
import pt.gongas.economy.platforms.bukkit.util.config.Configuration;
import pt.gongas.economy.shared.util.Formatter;
import pt.gongas.redis.redis.RedisManager;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class BukkitEconomyPlugin extends JavaPlugin {

    private Database datacenter;

    private ExecutorService databaseExecutor;

    private Metrics metrics;

    private CurrencyFoundationService currencyService;

    private UserFoundationService userService;

    public Formatter formatter;

    public static BukkitEconomyPlugin plugin;

    @Override
    public void onEnable() {

        plugin = this;

        saveDefaultConfig();

        Configuration currency = new Configuration(this, "currency", "currency.yml");
        currency.saveDefaultConfig();

        Configuration lang = new Configuration(this, "lang", "lang.yml");
        lang.saveDefaultConfig();

        Configuration inventory = new Configuration(this, "inventory", "inventory.yml");
        inventory.saveDefaultConfig();

        formatter = new Formatter();

        currencyService = new CurrencyService();
        new BukkitCurrencyLoader(currency).setup().forEach(currencyService::put);

        datacenter = new CustomDatabaseConnection(
                new DatabaseCredentialsImpl(DatabaseType.MYSQL,
                        getConfig().getString("database.host"),
                        getConfig().getString("database.port"),
                        getConfig().getString("database.database"),
                        getConfig().getString("database.user"),
                        getConfig().getString("database.password"),
                        getConfig().getString("database.file"))
        ).setup(
                getConfig().getInt("hikari.maximumPoolSize"),
                getConfig().getInt("hikari.connectionTimeout"),
                getConfig().getInt("hikari.minimumIdle"),
                getConfig().getInt("hikari.maximumLifetime"),
                getConfig().getInt("hikari.keepaliveTime")
        );

        String messagingConfig = getConfig().getString("messaging-service", "none");
        Messaging messaging;

        if (messagingConfig.equalsIgnoreCase("bungeecord")) {
            messaging = new MessagingBungee();
            messaging.setup();
        } else if (messagingConfig.equalsIgnoreCase("redis")) {
            messaging = new MessagingRedis();
            messaging.setup();
        } else {
            messaging = null;
        }

        RTopic transactions;

        if (messaging != null) {
            transactions = RedisManager.getClient().getTopic("economy:transactions");
        } else {
            transactions = null;
        }

        int cores = Runtime.getRuntime().availableProcessors();
        databaseExecutor = Executors.newFixedThreadPool(Math.min(cores / 2, Math.max(1, getConfig().getInt("database.executorThreads"))));

        if (cores < 4) {
            getLogger().log(Level.WARNING, "The plugin is running on a machine that provides less than 4 cores to the JVM, which is very low. The general rule is to switch machines when you deploy the server to production.");
        }

        userService = new UserService(getLogger(), databaseExecutor, currencyService, datacenter);

        RankingView view = new RankingView(inventory, userService);
        getServer().getPluginManager().registerEvents(view, this);

        BukkitCommandManager commandManager = new BukkitCommandManager(this);
        commandManager.enableUnstableAPI("help");

        Set<UUID> uuids = ConcurrentHashMap.newKeySet();

        for (Currency economy : currencyService.getAll()) {
            commandManager.getCommandReplacements().addReplacement("currency", economy.name().toLowerCase());
            commandManager.registerCommand(new EconomyCommand(lang, economy, userService, view, messaging, transactions, uuids));
        }

        getServer().getPluginManager().registerEvents(new PlayerListener(lang, userService), this);
        new PlayerBalanceRunnable(userService, uuids).runTaskTimer(this, 20, 20);

        if (transactions != null) {

            transactions.addListener(TransactionMessage.class, (channel, msg) -> {

                UUID senderUuid = msg.getSender();
                UUID targetUuid = msg.getTarget();

                if (senderUuid != null) {

                    User sender = userService.get(senderUuid);

                    if (sender != null) {
                        uuids.add(senderUuid);
                    }

                }

                if (targetUuid != null) {

                    User target = userService.get(targetUuid);

                    if (target != null) {
                        uuids.add(targetUuid);
                    }

                }

            });

        }

        // BStats Metrics
        metrics = new Metrics(this, 28595);

    }

    @Override
    public void onDisable() {

        if (databaseExecutor != null) {

            databaseExecutor.shutdown();

            try {
                // Wait for currently executing tasks to finish
                if (!databaseExecutor.awaitTermination(36, TimeUnit.SECONDS)) {
                    // Force shutdown if tasks are not finished in the given time
                    databaseExecutor.shutdownNow();
                    // Wait for tasks to respond to being cancelled
                    if (!databaseExecutor.awaitTermination(36, TimeUnit.SECONDS)) {
                        System.err.println("Database Executor did not terminate in the specified time.");
                    }
                }
            } catch (InterruptedException ie) {
                // (Re-)Cancel if current thread also interrupted
                databaseExecutor.shutdownNow();
            }

        }

        if (datacenter != null) {
            datacenter.close();
        }

        if (metrics != null) {
            metrics.shutdown();
        }

    }

    public CurrencyFoundationService getCurrencyService() {
        return currencyService;
    }

    public UserFoundationService getUserService() {
        return userService;
    }

}
