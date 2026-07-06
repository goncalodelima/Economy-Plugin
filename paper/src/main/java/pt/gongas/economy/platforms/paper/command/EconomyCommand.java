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

package pt.gongas.economy.platforms.paper.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandHelp;
import co.aikar.commands.annotation.*;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import pt.gongas.economy.platforms.paper.PaperEconomyPlugin;
import pt.gongas.economy.platforms.paper.lang.LangMessages;
import pt.gongas.economy.platforms.paper.view.RankingView;
import pt.gongas.economy.shared.api.EconomyApi;
import pt.gongas.economy.shared.currency.Currency;
import pt.gongas.economy.shared.user.User;
import pt.gongas.economy.shared.user.service.UserFoundationService;

@CommandAlias("%currency")
public class EconomyCommand extends BaseCommand {

    private final LangMessages messages;

    private final Currency currency;

    private final UserFoundationService userService;

    private final RankingView view;

    private final EconomyApi<Player> economyApi;

    public EconomyCommand(LangMessages messages, Currency currency, UserFoundationService userService, RankingView view, EconomyApi<Player> economyApi) {
        this.messages = messages;
        this.currency = currency;
        this.userService = userService;
        this.view = view;
        this.economyApi = economyApi;
    }

    @Default
    @Description("View your own balance")
    public void viewBalance(Player player) {

        User user = userService.get(player.getUniqueId());

        if (user == null) {
            player.sendRichMessage(messages.error);
            return;
        }

        player.sendRichMessage(messages.viewOwnBalance,
                Placeholder.unparsed("balance", getFormatted(user.get(currency) / 100D)),
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
                player.sendRichMessage(messages.error1);
                return;
            }

            player.sendRichMessage(messages.viewOtherBalance,
                    Placeholder.unparsed("target", targetPlayer.getName()),
                    Placeholder.unparsed("balance", getFormatted(user.get(currency) / 100D)),
                    Placeholder.parsed("icon", currency.icon())
            );

            return;
        }

        economyApi.getCurrencyAndNotifyIfNeeded(player, target, currency);
    }

    @Subcommand("pay")
    @Description("Pay another player's balance")
    @Syntax("<target> <amount>")
    @CommandCompletion("@players")
    public void pay(Player player, String target, String amountToParse) {

        double amount = PaperEconomyPlugin.plugin.formatter.parseFormattedNumber(amountToParse);

        if (amount <= 0) {
            player.sendRichMessage(messages.payInvalidAmount);
            return;
        }

        if (player.getName().equalsIgnoreCase(target)) {
            player.sendRichMessage(messages.cannotSendToSelf);
            return;
        }

        User user = userService.get(player.getUniqueId());

        if (user == null) {
            player.sendRichMessage(messages.error);
            return;
        }

        Player targetPlayer = Bukkit.getPlayer(target);

        if (targetPlayer != null && target.equalsIgnoreCase(targetPlayer.getName())) {

            User targetUser = userService.get(targetPlayer.getUniqueId());

            if (targetUser == null) {
                player.sendRichMessage(messages.error1);
                return;
            }

            economyApi.payCurrencyAndNotifyIfNeeded(player, targetPlayer, user, targetUser, currency, amount, true);
        } else {
            economyApi.payCurrencyAndNotifyIfNeeded(player, target, user, currency, amount, true);
        }

    }

    @Subcommand("set")
    @Description("Set another player's balance")
    @Syntax("<target> <amount>")
    @CommandPermission("economy-plugin.admin")
    @CommandCompletion("@players")
    public void set(CommandSender sender, String target, String amountToParse) {

        double amount = PaperEconomyPlugin.plugin.formatter.parseFormattedNumber(amountToParse);

        if (amount < 0) {
            sender.sendRichMessage(messages.setInvalidAmount);
            return;
        }

        Player senderPlayer = (sender instanceof Player player) ? player : null;

        if (senderPlayer != null) {

            User user = userService.get(senderPlayer.getUniqueId());

            if (user == null) {
                sender.sendRichMessage(messages.error);
                return;
            }

        }

        Player targetPlayer = Bukkit.getPlayer(target);

        if (targetPlayer != null && target.equalsIgnoreCase(targetPlayer.getName())) {

            User targetUser = userService.get(targetPlayer.getUniqueId());

            if (targetUser == null) {
                sender.sendRichMessage(messages.error1);
                return;
            }

            economyApi.setCurrencyAndNotifyIfNeeded(senderPlayer, targetPlayer, targetUser, currency, amount);

        } else {
            economyApi.setCurrencyAndNotifyIfNeeded(senderPlayer, target, currency, amount);
        }

    }

    @Subcommand("add")
    @Description("Add another player's balance")
    @Syntax("<target> <amount>")
    @CommandPermission("economy-plugin.admin")
    @CommandCompletion("@players")
    public void add(CommandSender sender, String target, String amountToParse) {

        double amount = PaperEconomyPlugin.plugin.formatter.parseFormattedNumber(amountToParse);

        if (amount <= 0) {
            sender.sendRichMessage(messages.addInvalidAmount);
            return;
        }

        Player senderPlayer = (sender instanceof Player player) ? player : null;

        if (senderPlayer != null) {

            User user = userService.get(senderPlayer.getUniqueId());

            if (user == null) {
                sender.sendRichMessage(messages.error);
                return;
            }

        }

        Player targetPlayer = Bukkit.getPlayer(target);

        if (targetPlayer != null && target.equalsIgnoreCase(targetPlayer.getName())) {

            User targetUser = userService.get(targetPlayer.getUniqueId());

            if (targetUser == null) {
                sender.sendRichMessage(messages.error1);
                return;
            }

            economyApi.addCurrencyAndNotifyIfNeeded(senderPlayer, targetPlayer, targetUser, currency, amount);

        } else {
            economyApi.addCurrencyAndNotifyIfNeeded(senderPlayer, target, currency, amount);
        }

    }

    @Subcommand("remove")
    @Description("Remove another player's balance")
    @Syntax("<target> <amount>")
    @CommandPermission("economy-plugin.admin")
    @CommandCompletion("@players")
    public void remove(CommandSender sender, String target, String amountToParse) {

        double amount = PaperEconomyPlugin.plugin.formatter.parseFormattedNumber(amountToParse);

        if (amount <= 0) {
            sender.sendRichMessage(messages.removeInvalidAmount);
            return;
        }

        Player senderPlayer = (sender instanceof Player player) ? player : null;

        if (senderPlayer != null) {

            User user = userService.get(senderPlayer.getUniqueId());

            if (user == null) {
                sender.sendRichMessage(messages.error);
                return;
            }

        }

        Player targetPlayer = Bukkit.getPlayer(target);

        if (targetPlayer != null && target.equalsIgnoreCase(targetPlayer.getName())) {

            User targetUser = userService.get(targetPlayer.getUniqueId());

            if (targetUser == null) {
                sender.sendRichMessage(messages.error1);
                return;
            }

            economyApi.removeCurrencyAndNotifyIfNeeded(senderPlayer, targetPlayer, targetUser, currency, amount);

        } else {
            economyApi.removeCurrencyAndNotifyIfNeeded(senderPlayer, target, currency, amount);
        }

    }

    @Subcommand("top")
    @Description("View the top richest players")
    public void openRanking(Player player) {

        User user = userService.get(player.getUniqueId());

        if (user == null) {
            player.sendRichMessage(messages.error);
            return;
        }

        view.open(player, currency);
    }

    @HelpCommand
    public static void onHelp(CommandHelp help) {
        help.showHelp();
    }

    private String getFormatted(double amount) {
        return PaperEconomyPlugin.plugin.formatter.formatNumber(amount);
    }

}
