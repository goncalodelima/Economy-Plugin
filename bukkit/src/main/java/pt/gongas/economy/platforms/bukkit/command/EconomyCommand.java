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

package pt.gongas.economy.platforms.bukkit.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandHelp;
import co.aikar.commands.annotation.*;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.redisson.api.RTopic;
import pt.gongas.economy.platforms.bukkit.BukkitEconomyPlugin;
import pt.gongas.economy.platforms.bukkit.view.RankingView;
import pt.gongas.economy.shared.messaging.Messaging;
import pt.gongas.economy.shared.currency.Currency;
import pt.gongas.economy.shared.messaging.TransactionMessage;
import pt.gongas.economy.shared.user.ErrorType;
import pt.gongas.economy.shared.user.QueryUserResult;
import pt.gongas.economy.shared.user.User;
import pt.gongas.economy.shared.user.service.UserFoundationService;
import pt.gongas.economy.platforms.bukkit.util.config.Configuration;

import java.util.Set;
import java.util.UUID;

@CommandAlias("%currency")
public class EconomyCommand extends BaseCommand {

    private final Configuration lang;

    private final Currency currency;

    private final UserFoundationService userService;

    private final RankingView view;

    private final Messaging messaging;

    private final RTopic transactions;

    private final Set<UUID> uuids;

    public EconomyCommand(Configuration lang, Currency currency, UserFoundationService userService, RankingView view, Messaging messaging, RTopic transactions, Set<UUID> uuids) {
        this.lang = lang;
        this.currency = currency;
        this.userService = userService;
        this.view = view;
        this.messaging = messaging;
        this.transactions = transactions;
        this.uuids = uuids;
    }

    @Default
    @Description("View your own balance")
    public void viewBalance(Player player) {

        User user = userService.get(player.getUniqueId());

        if (user == null) {
            player.sendRichMessage(lang.getString("error", "<red>Something unexpected happened. Please re-log into the server."));
            return;
        }

        player.sendRichMessage(lang.getString("view-own-balance", "<green>You have <white><balance><icon></bold><green>."),
                Placeholder.unparsed("balance", getFormatted(user.get(currency) / 100)),
                Placeholder.parsed("icon", currency.icon())
        );

    }

    @Subcommand("view")
    @Description("View the other's balance")
    @Syntax("<target>")
    @CommandCompletion("@players")
    public void viewOtherBalance(Player player, String target) {

        Player targetPlayer = Bukkit.getPlayer(target);

        if (targetPlayer != null && targetPlayer.getName().equalsIgnoreCase(target)) {

            User user = userService.get(targetPlayer.getUniqueId());

            if (user == null) {
                player.sendRichMessage(lang.getString("error1", "<red>Something unexpected happened. Contact an administrator."));
                return;
            }

            player.sendRichMessage(lang.getString("view-other-balance", "<green>The player <white><target> <green>has <white><balance><icon></bold><green>."),
                    Placeholder.unparsed("target", targetPlayer.getName()),
                    Placeholder.unparsed("balance", getFormatted(user.get(currency) / 100)),
                    Placeholder.parsed("icon", currency.icon())
            );

            return;
        }

        userService.getCurrency(target, currency).thenAccept(result -> {

            switch (result) {

                case QueryUserResult.Success s ->
                        player.sendRichMessage(lang.getString("view-other-balance", "<green>The player <white><target> <green>has <white><balance><icon></bold><green>."),
                                Placeholder.unparsed("target", s.nickname()),
                                Placeholder.unparsed("balance", getFormatted(s.cents() / 100D)),
                                Placeholder.parsed("icon", currency.icon())
                        );

                case QueryUserResult.Error e -> {

                    if (e.type() == ErrorType.NOT_FOUND) {
                        player.sendRichMessage(lang.getString("database-not-found", "<red>No player with the entered name was found in the database."));
                    } else { // Exception Error
                        player.sendRichMessage(lang.getString("error2", "<red>An error occurred in the database while searching for this player's balance. Please contact an administrator."));
                    }

                }

            }

        });

    }

    @Subcommand("pay")
    @Description("Pay another player's balance")
    @Syntax("<target> <amount>")
    @CommandCompletion("@players")
    public void pay(Player player, String target, String amountToParse) {

        double amount = BukkitEconomyPlugin.plugin.formatter.parseFormattedNumber(amountToParse);

        if (amount <= 0) {
            player.sendRichMessage(lang.getString("pay-invalid-amount", "<red>The amount must be greater than zero."));
            return;
        }

        if (player.getName().equalsIgnoreCase(target)) {
            player.sendRichMessage(lang.getString("cannot-send-to-self", "<red>You cannot send money to yourself!"));
            return;
        }

        User user = userService.get(player.getUniqueId());

        if (user == null) {
            player.sendRichMessage(lang.getString("error", "<red>Something unexpected happened. Please re-log into the server."));
            return;
        }

        long cents = (long) (amount * 100);
        Player targetPlayer = Bukkit.getPlayer(target);

        if (targetPlayer != null && target.equalsIgnoreCase(targetPlayer.getName())) {

            User targetUser = userService.get(targetPlayer.getUniqueId());

            if (targetUser == null) {
                player.sendRichMessage(lang.getString("error1", "<red>Something unexpected happened. Contact an administrator."));
                return;
            }

            userService.updateCurrencies(player.getUniqueId(), targetPlayer.getUniqueId(), currency, cents).thenAcceptAsync(result -> {

                if (result.success()) {

                    if (result.value()) {

                        player.sendRichMessage(lang.getString("transaction-successful", "<green>You have successfully executed a transaction of <white><amount><icon></bold> <green>to the player <white><target><green>!"),
                                Placeholder.unparsed("target", targetPlayer.getName()),
                                Placeholder.unparsed("amount", getFormatted(amount)),
                                Placeholder.parsed("icon", currency.icon())
                        );

                        targetPlayer.sendRichMessage(lang.getString("transaction-received-successful", "<green>You have successfully received <white><amount><icon></bold> <green>from the player <white><sender><green>!"),
                                Placeholder.unparsed("sender", player.getName()),
                                Placeholder.unparsed("amount", getFormatted(amount)),
                                Placeholder.parsed("icon", currency.icon())
                        );

                        boolean isUserOnline = user.isOnline();
                        boolean isTargetOnline = targetUser.isOnline();

                        if (isUserOnline) {
                            uuids.add(user.getUuid());
                        }

                        if (isTargetOnline) {
                            uuids.add(targetUser.getUuid());
                        }

                        if (messaging != null && (!isUserOnline || !isTargetOnline)) {
                            UUID senderUuid = isUserOnline ? null : user.getUuid();
                            UUID receiverUuid = isTargetOnline ? null : targetUser.getUuid();
                            transactions.publishAsync(new TransactionMessage(senderUuid, receiverUuid));
                        }

                    } else {
                        player.sendRichMessage(lang.getString("not-enough-balance", "<red>You don't have enough balance to complete this transaction!"));
                    }

                } else {
                    player.sendRichMessage(lang.getString("payment-error", "<red>An unexpected error occurred while attempting to complete a transaction. Please try again, and if the problem persists, contact an administrator."));
                }

            }, Bukkit.getScheduler().getMainThreadExecutor(BukkitEconomyPlugin.plugin));

        } else {

            userService.updateCurrencies(player.getUniqueId(), target, currency, cents).thenAcceptAsync(result -> {

                switch (result) {

                    case QueryUserResult.Success s -> {

                        player.sendRichMessage(lang.getString("transaction-successful", "<green>You have successfully executed a transaction of <white><amount><icon></bold> <green>to the player <white><target><green>!"),
                                Placeholder.unparsed("target", s.nickname()),
                                Placeholder.unparsed("amount", getFormatted(amount)),
                                Placeholder.parsed("icon", currency.icon())
                        );

                        if (messaging != null) {
                            messaging.sendMessage(target, lang.getString("transaction-received-successful", "<green>You have successfully received <white><amount><icon></bold> <green>from the player <white><sender><green>!")
                                    .replace("<sender>", player.getName())
                                    .replace("<amount>", getFormatted(amount))
                                    .replace("<icon>", currency.icon())
                            );
                        }

                        User targetUser = userService.get(s.uuid());

                        boolean isUserOnline = user.isOnline();
                        boolean isTargetOnline = targetUser != null;

                        if (isUserOnline) {
                            uuids.add(user.getUuid());
                        }

                        if (isTargetOnline) {
                            uuids.add(targetUser.getUuid());
                        }

                        if (messaging != null && (!isUserOnline || !isTargetOnline)) {
                            UUID senderUuid = isUserOnline ? null : user.getUuid();
                            UUID receiverUuid = isTargetOnline ? null : s.uuid();
                            transactions.publishAsync(new TransactionMessage(senderUuid, receiverUuid));
                        }

                    }

                    case QueryUserResult.Error e -> {

                        if (e.type() == ErrorType.NOT_FOUND) {
                            player.sendRichMessage(lang.getString("database-not-found", "<red>No player with the entered name was found in the database."));
                            return;
                        }

                        if (e.type() == ErrorType.NOT_ENOUGH_BALANCE) {
                            player.sendRichMessage(lang.getString("not-enough-balance", "<red>You don't have enough balance to complete this transaction!"));
                            return;
                        }

                        // Exception Error
                        player.sendRichMessage(lang.getString("payment-error", "<red>An unexpected error occurred while attempting to complete a transaction. Please try again, and if the problem persists, contact an administrator."));
                    }

                }

            }, Bukkit.getScheduler().getMainThreadExecutor(BukkitEconomyPlugin.plugin));

        }

    }

    @Subcommand("set")
    @Description("Set another player's balance")
    @Syntax("<target> <amount>")
    @CommandPermission("economy-plugin.admin")
    @CommandCompletion("@players")
    public void set(Player player, String target, String amountToParse) {

        double amount = BukkitEconomyPlugin.plugin.formatter.parseFormattedNumber(amountToParse);

        if (amount < 0) {
            player.sendRichMessage(lang.getString("set-invalid-amount", "<red>The value must be greater than or equal to zero."));
            return;
        }

        User user = userService.get(player.getUniqueId());

        if (user == null) {
            player.sendRichMessage(lang.getString("error", "<red>Something unexpected happened. Please re-log into the server."));
            return;
        }

        long cents = (long) (amount * 100);
        Player targetPlayer = Bukkit.getPlayer(target);

        if (targetPlayer != null && target.equalsIgnoreCase(targetPlayer.getName())) {

            User targetUser = userService.get(targetPlayer.getUniqueId());

            if (targetUser == null) {
                player.sendRichMessage(lang.getString("error1", "<red>Something unexpected happened. Contact an administrator."));
                return;
            }

            userService.setCurrency(targetPlayer.getUniqueId(), currency, cents).thenAcceptAsync(result -> {

                if (result.success()) {

                    if (result.value()) {

                        player.sendRichMessage(lang.getString("set-transaction-successful", "<green>You have successfully set <white><amount><icon></bold> <green>for <white><target><green>."),
                                Placeholder.unparsed("target", targetPlayer.getName()),
                                Placeholder.unparsed("amount", getFormatted(amount)),
                                Placeholder.parsed("icon", currency.icon())
                        );

                        targetPlayer.sendRichMessage(lang.getString("set-transaction-received-successful", "<green>Your balance was set to <white><amount><icon></bold> <green>by <white><sender><green>."),
                                Placeholder.unparsed("sender", player.getName()),
                                Placeholder.unparsed("amount", getFormatted(amount)),
                                Placeholder.parsed("icon", currency.icon())
                        );

                        boolean isTargetOnline = targetUser.isOnline();

                        if (isTargetOnline) {
                            uuids.add(targetUser.getUuid());
                        } else if (messaging != null) {
                            UUID targetUuid = targetUser.getUuid();
                            transactions.publishAsync(new TransactionMessage(null, targetUuid));
                        }

                    } else {
                        player.sendRichMessage(lang.getString("database-not-found", "<red>No player with the entered name was found in the database."));
                    }

                } else {
                    player.sendRichMessage(lang.getString("payment-error", "<red>An unexpected error occurred while attempting to complete a transaction. Please try again, and if the problem persists, contact an administrator."));
                }

            }, Bukkit.getScheduler().getMainThreadExecutor(BukkitEconomyPlugin.plugin));

        } else {

            userService.setCurrency(target, currency, cents).thenAcceptAsync(result -> {

                switch (result) {

                    case QueryUserResult.Success s -> {

                        player.sendRichMessage(lang.getString("set-transaction-successful", "<green>You have successfully set <white><amount><icon></bold> <green>for <white><target><green>."),
                                Placeholder.unparsed("target", s.nickname()),
                                Placeholder.unparsed("amount", getFormatted(amount)),
                                Placeholder.parsed("icon", currency.icon())
                        );

                        if (messaging != null) {
                            messaging.sendMessage(s.nickname(), lang.getString("set-transaction-received-successful", "<green>Your balance was set to <white><amount><icon></bold> <green>by <white><sender><green>.")
                                    .replace("<sender>", player.getName())
                                    .replace("<amount>", getFormatted(amount))
                                    .replace("<icon>", currency.icon())
                            );
                        }

                        User targetUser = userService.get(s.uuid());
                        boolean isTargetOnline = targetUser != null;

                        if (isTargetOnline) {
                            uuids.add(targetUser.getUuid());
                        } else if (messaging != null) {
                            UUID targetUuid = s.uuid();
                            new TransactionMessage(null, targetUuid);
                            transactions.publishAsync(new TransactionMessage(null, targetUuid));
                        }

                    }

                    case QueryUserResult.Error e -> {

                        if (e.type() == ErrorType.NOT_FOUND) {
                            player.sendRichMessage(lang.getString("database-not-found", "<red>No player with the entered name was found in the database."));
                        } else { // Exception Error
                            player.sendRichMessage(lang.getString("payment-error", "<red>An unexpected error occurred while attempting to complete a transaction. Please try again, and if the problem persists, contact an administrator."));
                        }

                    }

                }

            }, Bukkit.getScheduler().getMainThreadExecutor(BukkitEconomyPlugin.plugin));

        }

    }

    @Subcommand("add")
    @Description("Add another player's balance")
    @Syntax("<target> <amount>")
    @CommandPermission("economy-plugin.admin")
    @CommandCompletion("@players")
    public void add(Player player, String target, String amountToParse) {

        double amount = BukkitEconomyPlugin.plugin.formatter.parseFormattedNumber(amountToParse);

        if (amount <= 0) {
            player.sendRichMessage(lang.getString("add-invalid-amount", "<red>The amount must be greater than zero."));
            return;
        }

        User user = userService.get(player.getUniqueId());

        if (user == null) {
            player.sendRichMessage(lang.getString("error", "<red>Something unexpected happened. Please re-log into the server."));
            return;
        }

        long cents = (long) (amount * 100);
        Player targetPlayer = Bukkit.getPlayer(target);

        if (targetPlayer != null && target.equalsIgnoreCase(targetPlayer.getName())) {

            User targetUser = userService.get(targetPlayer.getUniqueId());

            if (targetUser == null) {
                player.sendRichMessage(lang.getString("error1", "<red>Something unexpected happened. Contact an administrator."));
                return;
            }

            userService.addCurrency(targetPlayer.getUniqueId(), currency, cents).thenAcceptAsync(result -> {

                if (result.success()) {

                    if (result.value()) {

                        player.sendRichMessage(lang.getString("add-transaction-successful", "<green>You have successfully added <white><amount><icon></bold> <green>to <white><target><green>."),
                                Placeholder.unparsed("target", targetPlayer.getName()),
                                Placeholder.unparsed("amount", getFormatted(amount)),
                                Placeholder.parsed("icon", currency.icon())
                        );

                        targetPlayer.sendRichMessage(lang.getString("add-transaction-received-successful", "<green>You have successfully received <white><amount><icon></bold> <green>from <white><sender><green>."),
                                Placeholder.unparsed("sender", player.getName()),
                                Placeholder.unparsed("amount", getFormatted(amount)),
                                Placeholder.parsed("icon", currency.icon())
                        );

                        boolean isUserOnline = user.isOnline();
                        boolean isTargetOnline = targetUser.isOnline();

                        if (isUserOnline) {
                            uuids.add(user.getUuid());
                        }

                        if (isTargetOnline) {
                            uuids.add(targetUser.getUuid());
                        }

                        if (messaging != null && (!isUserOnline || !isTargetOnline)) {
                            UUID senderUuid = isUserOnline ? null : user.getUuid();
                            UUID receiverUuid = isTargetOnline ? null : targetUser.getUuid();
                            transactions.publishAsync(new TransactionMessage(senderUuid, receiverUuid));
                        }

                    } else {
                        player.sendRichMessage(lang.getString("not-enough-balance", "<red>You don't have enough balance to complete this transaction!"));
                    }

                } else {
                    player.sendRichMessage(lang.getString("payment-error", "<red>An unexpected error occurred while attempting to complete a transaction. Please try again, and if the problem persists, contact an administrator."));
                }

            }, Bukkit.getScheduler().getMainThreadExecutor(BukkitEconomyPlugin.plugin));

        } else {

            userService.addCurrency(target, currency, cents).thenAcceptAsync(result -> {

                switch (result) {

                    case QueryUserResult.Success s -> {

                        player.sendRichMessage(lang.getString("add-transaction-successful", "<green>You have successfully added <white><amount><icon></bold> <green>to <white><target><green>."),
                                Placeholder.unparsed("target", s.nickname()),
                                Placeholder.unparsed("amount", getFormatted(amount)),
                                Placeholder.parsed("icon", currency.icon())
                        );

                        if (messaging != null) {
                            messaging.sendMessage(target, lang.getString("add-transaction-received-successful", "<green>You have successfully received <white><amount><icon></bold> <green>from <white><sender><green>.")
                                    .replace("<sender>", player.getName())
                                    .replace("<amount>", getFormatted(amount))
                                    .replace("<icon>", currency.icon())
                            );
                        }

                        User targetUser = userService.get(s.uuid());

                        boolean isUserOnline = user.isOnline();
                        boolean isTargetOnline = targetUser != null;

                        if (isUserOnline) {
                            uuids.add(user.getUuid());
                        }

                        if (isTargetOnline) {
                            uuids.add(targetUser.getUuid());
                        }

                        if (messaging != null && (!isUserOnline || !isTargetOnline)) {
                            UUID senderUuid = isUserOnline ? null : user.getUuid();
                            UUID receiverUuid = isTargetOnline ? null : s.uuid();
                            transactions.publishAsync(new TransactionMessage(senderUuid, receiverUuid));
                        }

                    }

                    case QueryUserResult.Error e -> {

                        if (e.type() == ErrorType.NOT_FOUND) {
                            player.sendRichMessage(lang.getString("database-not-found", "<red>No player with the entered name was found in the database."));
                            return;
                        }

                        if (e.type() == ErrorType.NOT_ENOUGH_BALANCE) {
                            player.sendRichMessage(lang.getString("not-enough-balance", "<red>You don't have enough balance to complete this transaction!"));
                            return;
                        }

                        // Exception Error
                        player.sendRichMessage(lang.getString("payment-error", "<red>An unexpected error occurred while attempting to complete a transaction. Please try again, and if the problem persists, contact an administrator."));
                    }

                }

            }, Bukkit.getScheduler().getMainThreadExecutor(BukkitEconomyPlugin.plugin));

        }

    }

    @Subcommand("remove")
    @Description("Remove another player's balance")
    @Syntax("<target> <amount>")
    @CommandPermission("economy-plugin.admin")
    @CommandCompletion("@players")
    public void remove(Player player, String target, String amountToParse) {

        double amount = BukkitEconomyPlugin.plugin.formatter.parseFormattedNumber(amountToParse);

        if (amount <= 0) {
            player.sendRichMessage(lang.getString("remove-invalid-amount", "<red>The amount must be greater than zero."));
            return;
        }

        User user = userService.get(player.getUniqueId());

        if (user == null) {
            player.sendRichMessage(lang.getString("error", "<red>Something unexpected happened. Please re-log into the server."));
            return;
        }

        long cents = (long) (amount * 100);
        Player targetPlayer = Bukkit.getPlayer(target);

        if (targetPlayer != null && target.equalsIgnoreCase(targetPlayer.getName())) {

            User targetUser = userService.get(targetPlayer.getUniqueId());

            if (targetUser == null) {
                player.sendRichMessage(lang.getString("error1", "<red>Something unexpected happened. Contact an administrator."));
                return;
            }

            userService.removeCurrency(targetPlayer.getUniqueId(), currency, cents).thenAcceptAsync(result -> {

                if (result.success()) {

                    if (result.value()) {

                        player.sendRichMessage(lang.getString("remove-transaction-successful", "<green>You have successfully removed <white><amount><icon></bold> <green>from <white><target><green>."),
                                Placeholder.unparsed("target", targetPlayer.getName()),
                                Placeholder.unparsed("amount", getFormatted(amount)),
                                Placeholder.parsed("icon", currency.icon())
                        );

                        targetPlayer.sendRichMessage(lang.getString("remove-transaction-received-successful", "<green><white><amount><icon></bold> <green>was successfully removed from your balance by <white><sender><green>."),
                                Placeholder.unparsed("sender", player.getName()),
                                Placeholder.unparsed("amount", getFormatted(amount)),
                                Placeholder.parsed("icon", currency.icon())
                        );

                        boolean isUserOnline = user.isOnline();
                        boolean isTargetOnline = targetUser.isOnline();

                        if (isUserOnline) {
                            uuids.add(user.getUuid());
                        }

                        if (isTargetOnline) {
                            uuids.add(targetUser.getUuid());
                        }

                        if (messaging != null && (!isUserOnline || !isTargetOnline)) {
                            UUID senderUuid = isUserOnline ? null : user.getUuid();
                            UUID receiverUuid = isTargetOnline ? null : targetUser.getUuid();
                            transactions.publishAsync(new TransactionMessage(senderUuid, receiverUuid));
                        }

                    } else {
                        player.sendRichMessage(lang.getString("not-enough-balance", "<red>You don't have enough balance to complete this transaction!"));
                    }

                } else {
                    player.sendRichMessage(lang.getString("payment-error", "<red>An unexpected error occurred while attempting to complete a transaction. Please try again, and if the problem persists, contact an administrator."));
                }

            }, Bukkit.getScheduler().getMainThreadExecutor(BukkitEconomyPlugin.plugin));

        } else {

            userService.removeCurrency(target, currency, cents).thenAcceptAsync(result -> {

                switch (result) {

                    case QueryUserResult.Success s -> {

                        player.sendRichMessage(lang.getString("remove-transaction-successful", "<green>You have successfully removed <white><amount><icon></bold> <green>from <white><target><green>."),
                                Placeholder.unparsed("target", s.nickname()),
                                Placeholder.unparsed("amount", getFormatted(amount)),
                                Placeholder.parsed("icon", currency.icon())
                        );

                        if (messaging != null) {
                            messaging.sendMessage(target, lang.getString("remove-transaction-received-successful", "<green><white><amount><icon></bold> <green>was successfully removed from your balance by <white><sender><green>.")
                                    .replace("<sender>", player.getName())
                                    .replace("<amount>", getFormatted(amount))
                                    .replace("<icon>", currency.icon())
                            );
                        }

                        User targetUser = userService.get(s.uuid());

                        boolean isUserOnline = user.isOnline();
                        boolean isTargetOnline = targetUser != null;

                        if (isUserOnline) {
                            uuids.add(user.getUuid());
                        }

                        if (isTargetOnline) {
                            uuids.add(targetUser.getUuid());
                        }

                        if (messaging != null && (!isUserOnline || !isTargetOnline)) {
                            UUID senderUuid = isUserOnline ? null : user.getUuid();
                            UUID receiverUuid = isTargetOnline ? null : s.uuid();
                            transactions.publishAsync(new TransactionMessage(senderUuid, receiverUuid));
                        }

                    }

                    case QueryUserResult.Error e -> {

                        if (e.type() == ErrorType.NOT_FOUND) {
                            player.sendRichMessage(lang.getString("database-not-found", "<red>No player with the entered name was found in the database."));
                            return;
                        }

                        if (e.type() == ErrorType.NOT_ENOUGH_BALANCE) {
                            player.sendRichMessage(lang.getString("not-enough-balance", "<red>You don't have enough balance to complete this transaction!"));
                            return;
                        }

                        // Exception Error
                        player.sendRichMessage(lang.getString("payment-error", "<red>An unexpected error occurred while attempting to complete a transaction. Please try again, and if the problem persists, contact an administrator."));
                    }

                }

            }, Bukkit.getScheduler().getMainThreadExecutor(BukkitEconomyPlugin.plugin));

        }

    }

    @Subcommand("top")
    @Description("View the top richest players")
    public void openRanking(Player player) {

        User user = userService.get(player.getUniqueId());

        if (user == null) {
            player.sendRichMessage(lang.getString("error", "<red>Something unexpected happened. Please re-log into the server."));
            return;
        }

        view.open(player, currency);
    }

    @HelpCommand
    public static void onHelp(CommandHelp help) {
        help.showHelp();
    }

    private String getFormatted(double amount) {
        return BukkitEconomyPlugin.plugin.formatter.formatNumber(amount);
    }

}
