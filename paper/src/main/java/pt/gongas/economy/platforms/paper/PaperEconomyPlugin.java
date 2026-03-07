/*
 *
 *  * This file is part of Economy-Plugin - https://github.com/goncalodelima/Economy-Plugin
 *  * Copyright (c) 2026 goncalodelima and contributors
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

package pt.gongas.economy.platforms.paper;

import co.aikar.commands.PaperCommandManager;
import org.bstats.bukkit.Metrics;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import pt.gongas.database.Database;
import pt.gongas.database.DatabaseType;
import pt.gongas.database.connection.CustomDatabaseConnection;
import pt.gongas.database.credentials.impl.DatabaseCredentialsImpl;
import org.bukkit.plugin.java.JavaPlugin;
import org.redisson.api.RTopic;
import pt.gongas.economy.platforms.paper.api.PaperEconomyApi;
import pt.gongas.economy.platforms.paper.command.EconomyCommand;
import pt.gongas.economy.platforms.paper.hook.EconomyPlaceholderExpansion;
import pt.gongas.economy.platforms.paper.listener.PlayerListener;
import pt.gongas.economy.platforms.paper.runnable.PlayerBalanceRunnable;
import pt.gongas.economy.platforms.paper.view.RankingView;
import pt.gongas.economy.shared.api.EconomyApi;
import pt.gongas.economy.shared.currency.service.CurrencyFoundationService;
import pt.gongas.economy.shared.messaging.Messaging;
import pt.gongas.economy.platforms.paper.messaging.MessagingBungee;
import pt.gongas.economy.shared.messaging.MessagingRedis;
import pt.gongas.economy.shared.currency.Currency;
import pt.gongas.economy.platforms.paper.model.currency.loader.PaperCurrencyLoader;
import pt.gongas.economy.shared.currency.service.CurrencyService;
import pt.gongas.economy.shared.messaging.TransactionMessage;
import pt.gongas.economy.shared.transaction.EconomyTransactionManager;
import pt.gongas.economy.shared.user.User;
import pt.gongas.economy.shared.user.service.UserFoundationService;
import pt.gongas.economy.shared.user.service.UserService;
import pt.gongas.economy.platforms.paper.util.config.Configuration;
import pt.gongas.economy.shared.util.Formatter;
import pt.gongas.redis.redis.RedisManager;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.*;

public class PaperEconomyPlugin extends JavaPlugin {

    private Database datacenter;

    private ExecutorService databaseExecutor;

    private Metrics metrics;

    private EconomyApi<Player> economyApi;

    public Formatter formatter;

    public static PaperEconomyPlugin plugin;

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

        CurrencyFoundationService currencyService = new CurrencyService();
        new PaperCurrencyLoader(currency).setup().forEach(currencyService::put);

        int hikariMaxPoolSize = getConfig().getInt("hikari.maximumPoolSize", 10);
        long hikariConnectionTimeout = getConfig().getLong("hikari.connectionTimeout", 5_000);
        int hikariMinimumIdle = getConfig().getInt("hikari.minimumIdle", 10);
        long hikariMaximumLifeTime = getConfig().getLong("hikari.maximumLifetime", 1_800_000);
        long hikariKeepaliveTime = getConfig().getLong("hikari.keepaliveTime", 30_000);

        datacenter = new CustomDatabaseConnection(
                new DatabaseCredentialsImpl(DatabaseType.MYSQL,
                        getConfig().getString("database.host"),
                        getConfig().getString("database.port"),
                        getConfig().getString("database.database"),
                        getConfig().getString("database.user"),
                        getConfig().getString("database.password"),
                        getConfig().getString("database.file"))
        ).setup(
                hikariMaxPoolSize,
                hikariConnectionTimeout,
                hikariMinimumIdle,
                hikariMaximumLifeTime,
                hikariKeepaliveTime
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

        databaseExecutor = Executors.newVirtualThreadPerTaskExecutor();

        UserFoundationService userService = new UserService(getLogger(), databaseExecutor, currencyService, datacenter);

        RankingView view = new RankingView(inventory, userService, new NamespacedKey(this, "menu-item"));
        getServer().getPluginManager().registerEvents(view, this);

        PaperCommandManager commandManager = new PaperCommandManager(this);
        commandManager.enableUnstableAPI("help");

        Set<UUID> uuids = ConcurrentHashMap.newKeySet();

        EconomyTransactionManager transactionManager = new EconomyTransactionManager(userService);
        economyApi = new PaperEconomyApi(lang, currencyService, userService, transactionManager, messaging, transactions, uuids);

        for (Currency economy : currencyService.getAll()) {
            commandManager.getCommandReplacements().addReplacement("currency", economy.name().toLowerCase());
            commandManager.registerCommand(new EconomyCommand(lang, economy, userService, view, economyApi));
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

        new EconomyPlaceholderExpansion(currencyService, userService).register();

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
                Thread.currentThread().interrupt();
            }

        }

        if (datacenter != null) {
            datacenter.close();
        }

        if (metrics != null) {
            metrics.shutdown();
        }

    }

    public EconomyApi<Player> getEconomyApi() {
        return economyApi;
    }

}
