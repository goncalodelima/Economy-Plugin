package com.twins.core.economy.command;

import com.twins.core.CorePlugin;
import com.twins.core.economy.EconomyPlugin;
import com.twins.core.economy.inventory.RankingInventory;
import com.twins.core.economy.model.currency.Currency;
import com.twins.core.economy.model.user.User;
import com.twins.core.global.GlobalPlugin;
import com.twins.core.global.model.user.GlobalUser;
import com.twins.core.utils.CCommand;
import com.twins.core.utils.Configuration;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.concurrent.CompletableFuture;

public class EconomyCommand extends CCommand {

    private final EconomyPlugin plugin;

    private final Configuration config;

    private final Currency currency;

    public EconomyCommand(EconomyPlugin plugin, Currency currency) {
        super(currency.command());
        this.plugin = plugin;
        this.config = plugin.getMessages();
        this.currency = currency;
    }

    @Override
    public void run(CommandSender sender, String commandLabel, String[] args) {

        if (sender instanceof Player player) {

            CompletableFuture<GlobalUser> globalUserFuture = GlobalPlugin.INSTANCE.getUserService().get(player.getName());

            globalUserFuture.thenAcceptAsync(globalUser -> {

                if (args.length == 0) {
                    plugin.getUserService().get(player.getName()).thenAcceptAsync(user -> {
                        double amount = user.get(currency);
                        player.sendMessage(config.getString(globalUser.getLanguageType(), "see-currency").replace("&", "§")
                                .replace("{currency}", currency.name()).replace("{icon}", currency.icon())
                                .replace("{amount}", CorePlugin.INSTANCE.getFormatter().formatNumber(amount)));
                    }, CorePlugin.INSTANCE.getMainExecutor());
                    return;
                }

                if (args.length == 1) {

                    OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);

                    if (!target.hasPlayedBefore()) {
                        player.sendMessage(config.getString(globalUser.getLanguageType(), "invalid-player").replace("&", "§"));
                        return;
                    }

                    plugin.getUserService().get(target.getName()).thenAcceptAsync(targetUser -> {
                        double amount = targetUser.get(currency);
                        player.sendMessage(config.getString(globalUser.getLanguageType(), "see-other-currency").replace("&", "§").replace("{currency}", currency.name()).replace("{icon}", currency.icon()).replace("{amount}", CorePlugin.INSTANCE.getFormatter().formatNumber(amount)).replace("{player}", targetUser.nickname()));
                    }, CorePlugin.INSTANCE.getMainExecutor());

                    return;
                }

                if (args[0].equalsIgnoreCase("pay") || args[0].equalsIgnoreCase("enviar")) {

                    if (args.length != 3) {
                        player.sendMessage(config.getString(globalUser.getLanguageType(), "pay-syntax-error").replace("&", "§").replace("{command}", currency.command()));
                        return;
                    }

                    Player receiver = Bukkit.getPlayer(args[1]);

                    if (receiver == null) {
                        player.sendMessage(config.getString(globalUser.getLanguageType(), "invalid-player").replace("&", "§"));
                        return;
                    }

                    if (receiver.getName().equals(player.getName())) {
                        player.sendMessage(config.getString(globalUser.getLanguageType(), "pay-yourself").replace("&", "§").replace("{currency}", currency.name()));
                        return;
                    }

                    if (plugin.getUserService().containsTemporaryCache(player.getName())) {
                        player.sendMessage(config.getString(globalUser.getLanguageType(), "wait").replace("&", "§"));
                        return;
                    }

                    plugin.getUserService().addTemporaryCache(player.getName());
                    plugin.getUserService().addTemporaryCache(receiver.getName());

                    CompletableFuture<User> senderFuture = plugin.getUserService().get(player.getName());
                    CompletableFuture<User> receiverFuture = plugin.getUserService().get(receiver.getName());

                    senderFuture.thenAcceptBothAsync(receiverFuture, (senderUser, receiverUser) -> {

                        double amount = CorePlugin.INSTANCE.getFormatter().parseFormattedNumber(args[2]);

                        if (amount <= -1) {
                            player.sendMessage(config.getString(globalUser.getLanguageType(), "invalid-amount").replace("&", "§"));
                            return;
                        }

                        if (senderUser.get(currency) < amount) {
                            player.sendMessage(config.getString(globalUser.getLanguageType(), "no-enough").replace("&", "§").replace("{currency}", currency.name()));
                            return;
                        }

                        plugin.getUserService().updateCurrencies(senderUser.nickname(), receiverUser.nickname(), currency, amount).thenAcceptAsync(value -> {

                            if (value) {

                                senderUser.remove(currency, amount);
                                receiverUser.remove(currency, amount);

                                player.sendMessage(config.getString(globalUser.getLanguageType(), "pay-sender").replace("&", "§").replace("{currency}", currency.name()).replace("{icon}", currency.icon()).replace("{amount}", CorePlugin.INSTANCE.getFormatter().formatNumber(amount)).replace("{receiver}", receiverUser.nickname()));

                                if (receiver.isOnline()) {
                                    GlobalPlugin.INSTANCE.getUserService().get(receiverUser.nickname()).thenAcceptAsync(globalUserReceiver -> receiver.sendMessage(config.getString(globalUserReceiver.getLanguageType(), "pay-receiver").replace("&", "§").replace("{currency}", currency.name()).replace("{icon}", currency.icon()).replace("{amount}", CorePlugin.INSTANCE.getFormatter().formatNumber(amount)).replace("{sender}", receiverUser.nickname())));
                                }

                            }

                            plugin.getUserService().removeTemporaryCache(senderUser.nickname());
                            plugin.getUserService().removeTemporaryCache(receiverUser.nickname());

                        }, CorePlugin.INSTANCE.getMainExecutor());

                    }, CorePlugin.INSTANCE.getMainExecutor());

                    return;
                }

                if (args[0].equalsIgnoreCase("top") || args[0].equalsIgnoreCase("ranking")) {
                    plugin.getViewFrame().open(RankingInventory.class, player, currency);
                    return;
                }

                if (!player.hasPermission("economy.admin")) {
                    player.sendMessage(config.getString(globalUser.getLanguageType(), "no-permission").replace("&", "§"));
                    return;
                }

                if (args[0].equalsIgnoreCase("add")) {

                    if (args.length != 3) {
                        player.sendMessage(config.getString(globalUser.getLanguageType(), "add-syntax-error").replace("&", "§").replace("{command}", currency.command()));
                        return;
                    }

                    OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);

                    if (!target.hasPlayedBefore()) {
                        player.sendMessage(config.getString(globalUser.getLanguageType(), "invalid-player").replace("&", "§"));
                        return;
                    }

                    double amount = CorePlugin.INSTANCE.getFormatter().parseFormattedNumber(args[2]);

                    if (amount <= -1) {
                        player.sendMessage(config.getString(globalUser.getLanguageType(), "invalid-amount").replace("&", "§"));
                        return;
                    }

                    if (plugin.getUserService().containsTemporaryCache(target.getName())) {
                        player.sendMessage(config.getString(globalUser.getLanguageType(), "wait").replace("&", "§"));
                        return;
                    }

                    plugin.getUserService().addTemporaryCache(target.getName());

                    plugin.getUserService().get(target.getName()).thenAcceptAsync(targetUser -> {

                        double newAmount = targetUser.get(currency) + amount;

                        plugin.getUserService().setCurrency(targetUser.nickname(), currency, newAmount).thenAcceptAsync(success -> {

                            if (success) {
                                targetUser.set(currency, newAmount);
                                player.sendMessage(config.getString(globalUser.getLanguageType(), "add-success").replace("&", "§").replace("{currency}", currency.name()).replace("{icon}", currency.icon()).replace("{amount}", CorePlugin.INSTANCE.getFormatter().formatNumber(amount)).replace("{player}", targetUser.nickname()));
                            }

                            plugin.getUserService().removeTemporaryCache(target.getName());

                        }, CorePlugin.INSTANCE.getMainExecutor());

                    }, CorePlugin.INSTANCE.getMainExecutor());

                    return;
                }

                if (args[0].equalsIgnoreCase("remove")) {

                    if (args.length != 3) {
                        player.sendMessage(config.getString(globalUser.getLanguageType(), "remove-syntax-error").replace("&", "§").replace("{command}", currency.command()));
                        return;
                    }

                    OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);

                    if (!target.hasPlayedBefore()) {
                        player.sendMessage(config.getString(globalUser.getLanguageType(), "invalid-player").replace("&", "§"));
                        return;
                    }

                    if (plugin.getUserService().containsTemporaryCache(target.getName())) {
                        player.sendMessage(config.getString(globalUser.getLanguageType(), "wait").replace("&", "§"));
                        return;
                    }

                    plugin.getUserService().addTemporaryCache(target.getName());

                    plugin.getUserService().get(target.getName()).thenAcceptAsync(targetUser -> {

                        double amount = CorePlugin.INSTANCE.getFormatter().parseFormattedNumber(args[2]);

                        if (amount <= -1) {
                            player.sendMessage(config.getString(globalUser.getLanguageType(), "invalid-amount").replace("&", "§"));
                            return;
                        }

                        double newAmount = targetUser.get(currency) - amount;

                        plugin.getUserService().setCurrency(targetUser.nickname(), currency, newAmount).thenAcceptAsync(success -> {

                            if (success) {
                                targetUser.set(currency, newAmount);
                                player.sendMessage(config.getString(globalUser.getLanguageType(), "remove-success").replace("&", "§").replace("{currency}", currency.name()).replace("{icon}", currency.icon()).replace("{amount}", CorePlugin.INSTANCE.getFormatter().formatNumber(amount)).replace("{player}", targetUser.nickname()));
                            }

                            plugin.getUserService().removeTemporaryCache(target.getName());

                        }, CorePlugin.INSTANCE.getMainExecutor());

                    });

                    return;
                }

                config.getStringList(globalUser.getLanguageType(), "help").forEach(string -> player.sendMessage(string.replace("&", "§")));

            }, CorePlugin.INSTANCE.getMainExecutor());

        }

    }

}
