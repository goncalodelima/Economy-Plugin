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

package pt.gongas.economy.platforms.paper.api;

import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.redisson.api.RTopic;
import pt.gongas.economy.platforms.paper.PaperEconomyPlugin;
import pt.gongas.economy.platforms.paper.util.config.Configuration;
import pt.gongas.economy.shared.api.EconomyApi;
import pt.gongas.economy.shared.currency.Currency;
import pt.gongas.economy.shared.currency.service.CurrencyFoundationService;
import pt.gongas.economy.shared.messaging.Messaging;
import pt.gongas.economy.shared.messaging.TransactionMessage;
import pt.gongas.economy.shared.transaction.EconomyTransactionManager;
import pt.gongas.economy.shared.transaction.EconomyTransactionalApi;
import pt.gongas.economy.shared.user.ErrorType;
import pt.gongas.economy.shared.user.QueryUserResult;
import pt.gongas.economy.shared.user.User;
import pt.gongas.economy.shared.user.service.UserFoundationService;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class PaperEconomyApi implements EconomyApi<Player> {

    private final Configuration lang;

    private final CurrencyFoundationService currencyService;

    private final UserFoundationService userService;

    private final EconomyTransactionManager transactionManager;

    private final Messaging messaging;

    private final RTopic transactions;

    private final Set<UUID> uuids;

    public PaperEconomyApi(Configuration lang, CurrencyFoundationService currencyService, UserFoundationService userService, EconomyTransactionManager transactionManager, Messaging messaging, RTopic transactions, Set<UUID> uuids) {
        this.lang = lang;
        this.currencyService = currencyService;
        this.userService = userService;
        this.transactionManager = transactionManager;
        this.messaging = messaging;
        this.transactions = transactions;
        this.uuids = uuids;
    }

    @Override
    public @NotNull CurrencyFoundationService getCurrencyService() {
        return currencyService;
    }

    @Override
    public @NotNull UserFoundationService getUserService() {
        return userService;
    }

    @Override
    public @NotNull EconomyTransactionalApi getEconomyTransactionalApi() {
        return transactionManager;
    }

    @Override
    public @Nullable RTopic getTransactions() {
        return transactions;
    }

    @Override
    public @NotNull Set<UUID> getTrackedUuids() {
        return uuids;
    }

    @Override
    public @NotNull CompletableFuture<QueryUserResult> getCurrencyAndNotifyIfNeeded(@Nullable Player player, @NotNull String target, @NotNull Currency currency) {

        return userService.getCurrency(target, currency).thenApply(result -> {

            switch (result) {

                case QueryUserResult.Success s -> {
                    if (player != null)
                        player.sendRichMessage(lang.getString("view-other-balance", "<green>The player <white><target> <green>has <white><balance><icon></bold><green>."),
                                Placeholder.unparsed("target", s.nickname()),
                                Placeholder.unparsed("balance", getFormatted(s.cents() / 100D)),
                                Placeholder.parsed("icon", currency.icon())
                        );
                }

                case QueryUserResult.Error e -> {

                    if (player != null) {

                        if (e.type() == ErrorType.NOT_FOUND) {
                            player.sendRichMessage(lang.getString("database-not-found", "<red>No player with the entered name was found in the database."));
                        } else { // Exception Error
                            player.sendRichMessage(lang.getString("error2", "<red>An error occurred in the database while searching for this player's balance. Please contact an administrator."));
                        }

                    }

                }

                default -> throw new IllegalStateException("Unexpected value: " + result);
            }

            return result;
        });

    }

    @Override
    public @NotNull CompletableFuture<QueryUserResult> payCurrencyAndNotifyIfNeeded(@NotNull Player senderPlayer, @NotNull Player targetPlayer, @NotNull User senderUser, @NotNull User targetUser, @NotNull Currency currency, double amount, boolean notify) {

        long cents = (long) (amount * 100);

        return userService.updateCurrencies(senderPlayer.getUniqueId(), targetPlayer.getUniqueId(), currency, cents).thenApplyAsync(result -> {

            switch (result) {

                case QueryUserResult.SuccessNoData ignored -> {

                    if (notify) {

                        senderPlayer.sendRichMessage(lang.getString("transaction-successful", "<green>You have successfully executed a transaction of <white><amount><icon></bold> <green>to the player <white><target><green>!"),
                                Placeholder.unparsed("target", targetPlayer.getName()),
                                Placeholder.unparsed("amount", getFormatted(amount)),
                                Placeholder.parsed("icon", currency.icon())
                        );

                        targetPlayer.sendRichMessage(lang.getString("transaction-received-successful", "<green>You have successfully received <white><amount><icon> </bold><green>from the player <white><sender><green>!"),
                                Placeholder.unparsed("sender", senderPlayer.getName()),
                                Placeholder.unparsed("amount", getFormatted(amount)),
                                Placeholder.parsed("icon", currency.icon())
                        );

                    }

                    boolean isUserOnline = senderUser.isOnline();
                    boolean isTargetOnline = targetUser.isOnline();

                    if (isUserOnline) {
                        uuids.add(senderUser.getUuid());
                    }

                    if (isTargetOnline) {
                        uuids.add(targetUser.getUuid());
                    }

                    if (transactions != null && (!isUserOnline || !isTargetOnline)) {
                        UUID senderUuid = isUserOnline ? null : senderUser.getUuid();
                        UUID receiverUuid = isTargetOnline ? null : targetUser.getUuid();
                        transactions.publishAsync(new TransactionMessage(senderUuid, receiverUuid));
                    }

                }

                case QueryUserResult.Error e -> {

                    if (notify) {

                        if (e.type() == ErrorType.NOT_ENOUGH_BALANCE) {
                            senderPlayer.sendRichMessage(lang.getString("not-enough-balance", "<red>You don't have enough balance to complete this transaction!"));
                        } else if (e.type() == ErrorType.NOT_FOUND) {
                            senderPlayer.sendRichMessage(lang.getString("database-not-found", "<red>No player with the entered name was found in the database."));
                        } else { // Exception Error
                            senderPlayer.sendRichMessage(lang.getString("payment-error", "<red>An unexpected error occurred while attempting to complete a transaction. Please try again, and if the problem persists, contact an administrator."));
                        }

                    }

                }

                default -> throw new IllegalStateException("Unexpected value: " + result);
            }

            return result;

        }, Bukkit.getScheduler().getMainThreadExecutor(PaperEconomyPlugin.plugin));

    }

    @Override
    public @NotNull CompletableFuture<QueryUserResult> payCurrencyAndNotifyIfNeeded(@NotNull Player senderPlayer, @NotNull String target, @NotNull User senderUser, @NotNull Currency currency, double amount, boolean notify) {

        long cents = (long) (amount * 100);

        return userService.updateCurrencies(senderPlayer.getUniqueId(), target, currency, cents).thenApplyAsync(result -> {

            switch (result) {

                case QueryUserResult.Success s -> {

                    if (notify) {

                        senderPlayer.sendRichMessage(lang.getString("transaction-successful", "<green>You have successfully executed a transaction of <white><amount><icon></bold> <green>to the player <white><target><green>!"),
                                Placeholder.unparsed("target", s.nickname()),
                                Placeholder.unparsed("amount", getFormatted(amount)),
                                Placeholder.parsed("icon", currency.icon())
                        );

                        if (messaging != null) {
                            messaging.sendMessage(target, lang.getString("transaction-received-successful", "<green>You have successfully received <white><amount><icon></bold> <green>from the player <white><sender><green>!")
                                    .replace("<sender>", senderPlayer.getName())
                                    .replace("<amount>", getFormatted(amount))
                                    .replace("<icon>", currency.icon())
                            );
                        }

                    }

                    User targetUser = userService.get(s.uuid());

                    boolean isUserOnline = senderUser.isOnline();
                    boolean isTargetOnline = targetUser != null;

                    if (isUserOnline) {
                        uuids.add(senderUser.getUuid());
                    }

                    if (isTargetOnline) {
                        uuids.add(targetUser.getUuid());
                    }

                    if (transactions != null && (!isUserOnline || !isTargetOnline)) {
                        UUID senderUuid = isUserOnline ? null : senderUser.getUuid();
                        UUID receiverUuid = isTargetOnline ? null : s.uuid();
                        transactions.publishAsync(new TransactionMessage(senderUuid, receiverUuid));
                    }

                }

                case QueryUserResult.Error e -> {

                    if (notify) {

                        if (e.type() == ErrorType.NOT_FOUND) {
                            senderPlayer.sendRichMessage(lang.getString("database-not-found", "<red>No player with the entered name was found in the database."));
                        } else if (e.type() == ErrorType.NOT_ENOUGH_BALANCE) {
                            senderPlayer.sendRichMessage(lang.getString("not-enough-balance", "<red>You don't have enough balance to complete this transaction!"));
                        } else { // Exception Error
                            senderPlayer.sendRichMessage(lang.getString("payment-error", "<red>An unexpected error occurred while attempting to complete a transaction. Please try again, and if the problem persists, contact an administrator."));
                        }

                    }

                }

                default -> throw new IllegalStateException("Unexpected value: " + result);
            }

            return result;

        }, Bukkit.getScheduler().getMainThreadExecutor(PaperEconomyPlugin.plugin));

    }

    @Override
    public @NotNull CompletableFuture<QueryUserResult> setCurrencyAndNotifyIfNeeded(@Nullable Player senderPlayer, @NotNull Player targetPlayer, @NotNull User targetUser, @NotNull Currency currency, double amount) {

        long cents = (long) (amount * 100);

        return userService.setCurrency(targetPlayer.getUniqueId(), currency, cents).thenApplyAsync(result -> {

            switch (result) {

                case QueryUserResult.SuccessNoData ignored -> {

                    if (senderPlayer != null) {

                        senderPlayer.sendRichMessage(lang.getString("set-transaction-successful", "<green>You have successfully set <white><amount><icon></bold> <green>for <white><target><green>."),
                                Placeholder.unparsed("target", targetPlayer.getName()),
                                Placeholder.unparsed("amount", getFormatted(amount)),
                                Placeholder.parsed("icon", currency.icon())
                        );

                        targetPlayer.sendRichMessage(lang.getString("set-transaction-received-successful", "<green>Your balance was set to <white><amount><icon></bold> <green>by <white><sender><green>."),
                                Placeholder.unparsed("sender", senderPlayer.getName()),
                                Placeholder.unparsed("amount", getFormatted(amount)),
                                Placeholder.parsed("icon", currency.icon())
                        );

                    }

                    if (targetUser.isOnline()) {
                        uuids.add(targetUser.getUuid());
                    } else if (transactions != null) {
                        UUID targetUuid = targetUser.getUuid();
                        transactions.publishAsync(new TransactionMessage(null, targetUuid));
                    }

                }

                case QueryUserResult.Error e -> {

                    if (senderPlayer != null) {

                        if (e.type() == ErrorType.NOT_FOUND) {
                            senderPlayer.sendRichMessage(lang.getString("database-not-found", "<red>No player with the entered name was found in the database."));
                        } else { // Exception Error
                            senderPlayer.sendRichMessage(lang.getString("payment-error", "<red>An unexpected error occurred while attempting to complete a transaction. Please try again, and if the problem persists, contact an administrator."));
                        }

                    }

                }

                default -> throw new IllegalStateException("Unexpected value: " + result);
            }

            return result;

        }, Bukkit.getScheduler().getMainThreadExecutor(PaperEconomyPlugin.plugin));

    }

    @Override
    public @NotNull CompletableFuture<QueryUserResult> setCurrencyAndNotifyIfNeeded(@Nullable Player senderPlayer, @NotNull String target, @NotNull Currency currency, double amount) {

        long cents = (long) (amount * 100);

        return userService.setCurrency(target, currency, cents).thenApplyAsync(result -> {

            switch (result) {

                case QueryUserResult.Success s -> {

                    if (senderPlayer != null) {

                        senderPlayer.sendRichMessage(lang.getString("set-transaction-successful", "<green>You have successfully set <white><amount><icon></bold> <green>for <white><target><green>."),
                                Placeholder.unparsed("target", s.nickname()),
                                Placeholder.unparsed("amount", getFormatted(amount)),
                                Placeholder.parsed("icon", currency.icon())
                        );

                        if (messaging != null) {
                            messaging.sendMessage(s.nickname(), lang.getString("set-transaction-received-successful", "<green>Your balance was set to <white><amount><icon></bold> <green>by <white><sender><green>.")
                                    .replace("<sender>", senderPlayer.getName())
                                    .replace("<amount>", getFormatted(amount))
                                    .replace("<icon>", currency.icon())
                            );
                        }

                    }

                    User targetUser = userService.get(s.uuid());
                    boolean isTargetOnline = targetUser != null;

                    if (isTargetOnline) {
                        uuids.add(targetUser.getUuid());
                    } else if (transactions != null) {
                        UUID targetUuid = s.uuid();
                        transactions.publishAsync(new TransactionMessage(null, targetUuid));
                    }

                }

                case QueryUserResult.Error e -> {

                    if (senderPlayer != null) {

                        if (e.type() == ErrorType.NOT_FOUND) {
                            senderPlayer.sendRichMessage(lang.getString("database-not-found", "<red>No player with the entered name was found in the database."));
                        } else { // Exception Error
                            senderPlayer.sendRichMessage(lang.getString("payment-error", "<red>An unexpected error occurred while attempting to complete a transaction. Please try again, and if the problem persists, contact an administrator."));
                        }

                    }

                }

                default -> throw new IllegalStateException("Unexpected value: " + result);
            }

            return result;

        }, Bukkit.getScheduler().getMainThreadExecutor(PaperEconomyPlugin.plugin));

    }

    @Override
    public @NotNull CompletableFuture<QueryUserResult> addCurrencyAndNotifyIfNeeded(@Nullable Player senderPlayer, @NotNull Player targetPlayer, @NotNull User targetUser, @NotNull Currency currency, double amount) {

        long cents = (long) (amount * 100);

        return userService.addCurrency(targetPlayer.getUniqueId(), currency, cents).thenApplyAsync(result -> {

            switch (result) {

                case QueryUserResult.SuccessNoData ignored -> {

                    if (senderPlayer != null) {

                        senderPlayer.sendRichMessage(lang.getString("add-transaction-successful", "<green>You have successfully added <white><amount><icon></bold> <green>to <white><target><green>."),
                                Placeholder.unparsed("target", targetPlayer.getName()),
                                Placeholder.unparsed("amount", getFormatted(amount)),
                                Placeholder.parsed("icon", currency.icon())
                        );

                        targetPlayer.sendRichMessage(lang.getString("add-transaction-received-successful", "<green>You have successfully received <white><amount><icon></bold> <green>from <white><sender><green>."),
                                Placeholder.unparsed("sender", senderPlayer.getName()),
                                Placeholder.unparsed("amount", getFormatted(amount)),
                                Placeholder.parsed("icon", currency.icon())
                        );

                    }

                    if (targetUser.isOnline()) {
                        uuids.add(targetUser.getUuid());
                    } else if (transactions != null) {
                        UUID receiverUuid = targetUser.getUuid();
                        transactions.publishAsync(new TransactionMessage(null, receiverUuid));
                    }

                }

                case QueryUserResult.Error e -> {

                    if (senderPlayer != null) {

                        if (e.type() == ErrorType.NOT_ENOUGH_BALANCE) {
                            senderPlayer.sendRichMessage(lang.getString("not-enough-balance", "<red>You don't have enough balance to complete this transaction!"));
                        } else { // Exception Error
                            senderPlayer.sendRichMessage(lang.getString("payment-error", "<red>An unexpected error occurred while attempting to complete a transaction. Please try again, and if the problem persists, contact an administrator."));
                        }

                    }

                }

                default -> throw new IllegalStateException("Unexpected value: " + result);
            }

            return result;

        }, Bukkit.getScheduler().getMainThreadExecutor(PaperEconomyPlugin.plugin));

    }

    @Override
    public @NotNull CompletableFuture<QueryUserResult> addCurrencyAndNotifyIfNeeded(@Nullable Player senderPlayer, @NotNull String target, @NotNull Currency currency, double amount) {

        long cents = (long) (amount * 100);

        return userService.addCurrency(target, currency, cents).thenApplyAsync(result -> {

            switch (result) {

                case QueryUserResult.Success s -> {

                    if (senderPlayer != null) {

                        senderPlayer.sendRichMessage(lang.getString("add-transaction-successful", "<green>You have successfully added <white><amount><icon></bold> <green>to <white><target><green>."),
                                Placeholder.unparsed("target", s.nickname()),
                                Placeholder.unparsed("amount", getFormatted(amount)),
                                Placeholder.parsed("icon", currency.icon())
                        );

                        if (messaging != null) {
                            messaging.sendMessage(target, lang.getString("add-transaction-received-successful", "<green>You have successfully received <white><amount><icon></bold> <green>from <white><sender><green>.")
                                    .replace("<sender>", senderPlayer.getName())
                                    .replace("<amount>", getFormatted(amount))
                                    .replace("<icon>", currency.icon())
                            );
                        }

                    }

                    User targetUser = userService.get(s.uuid());
                    boolean isTargetOnline = targetUser != null;

                    if (isTargetOnline) {
                        uuids.add(targetUser.getUuid());
                    } else if (transactions != null) {
                        transactions.publishAsync(new TransactionMessage(null, s.uuid()));
                    }

                }

                case QueryUserResult.Error e -> {

                    if (senderPlayer != null) {

                        if (e.type() == ErrorType.NOT_FOUND) {
                            senderPlayer.sendRichMessage(lang.getString("database-not-found", "<red>No player with the entered name was found in the database."));
                        } else if (e.type() == ErrorType.NOT_ENOUGH_BALANCE) {
                            senderPlayer.sendRichMessage(lang.getString("not-enough-balance", "<red>You don't have enough balance to complete this transaction!"));
                        } else { // Exception Error
                            senderPlayer.sendRichMessage(lang.getString("payment-error", "<red>An unexpected error occurred while attempting to complete a transaction. Please try again, and if the problem persists, contact an administrator."));
                        }

                    }

                }

                default -> throw new IllegalStateException("Unexpected value: " + result);
            }

            return result;

        }, Bukkit.getScheduler().getMainThreadExecutor(PaperEconomyPlugin.plugin));

    }

    @Override
    public @NotNull CompletableFuture<QueryUserResult> removeCurrencyAndNotifyIfNeeded(@Nullable Player senderPlayer, @NotNull Player targetPlayer, @NotNull User targetUser, @NotNull Currency currency, double amount) {

        long cents = (long) (amount * 100);

        return userService.removeCurrency(targetPlayer.getUniqueId(), currency, cents).thenApplyAsync(result -> {

            switch (result) {

                case QueryUserResult.SuccessNoData ignored -> {

                    if (senderPlayer != null) {

                        senderPlayer.sendRichMessage(lang.getString("remove-transaction-successful", "<green>You have successfully removed <white><amount><icon></bold> <green>from <white><target><green>."),
                                Placeholder.unparsed("target", targetPlayer.getName()),
                                Placeholder.unparsed("amount", getFormatted(amount)),
                                Placeholder.parsed("icon", currency.icon())
                        );

                        targetPlayer.sendRichMessage(lang.getString("remove-transaction-received-successful", "<green><white><amount><icon></bold> <green>was successfully removed from your balance by <white><sender><green>."),
                                Placeholder.unparsed("sender", senderPlayer.getName()),
                                Placeholder.unparsed("amount", getFormatted(amount)),
                                Placeholder.parsed("icon", currency.icon())
                        );

                        boolean isTargetOnline = targetUser.isOnline();

                        if (isTargetOnline) {
                            uuids.add(targetUser.getUuid());
                        } else if (transactions != null) {
                            transactions.publishAsync(new TransactionMessage(null, targetUser.getUuid()));
                        }

                    }

                }

                case QueryUserResult.Error e -> {

                    if (senderPlayer != null) {

                        if (e.type() == ErrorType.NOT_FOUND) {
                            senderPlayer.sendRichMessage(lang.getString("database-not-found", "<red>No player with the entered name was found in the database."));
                        } else { // Exception Error
                            senderPlayer.sendRichMessage(lang.getString("payment-error", "<red>An unexpected error occurred while attempting to complete a transaction. Please try again, and if the problem persists, contact an administrator."));
                        }

                    }

                }

                default -> throw new IllegalStateException("Unexpected value: " + result);
            }

            return result;

        }, Bukkit.getScheduler().getMainThreadExecutor(PaperEconomyPlugin.plugin));

    }

    @Override
    public @NotNull CompletableFuture<QueryUserResult> removeCurrencyAndNotifyIfNeeded(@Nullable Player senderPlayer, @NotNull String target, @NotNull Currency currency, double amount) {

        long cents = (long) (amount * 100);

        return userService.removeCurrency(target, currency, cents).thenApplyAsync(result -> {

            switch (result) {

                case QueryUserResult.Success s -> {

                    if (senderPlayer != null) {

                        senderPlayer.sendRichMessage(lang.getString("remove-transaction-successful", "<green>You have successfully removed <white><amount><icon></bold> <green>from <white><target><green>."),
                                Placeholder.unparsed("target", s.nickname()),
                                Placeholder.unparsed("amount", getFormatted(amount)),
                                Placeholder.parsed("icon", currency.icon())
                        );

                        if (messaging != null) {
                            messaging.sendMessage(target, lang.getString("remove-transaction-received-successful", "<green><white><amount><icon></bold> <green>was successfully removed from your balance by <white><sender><green>.")
                                    .replace("<sender>", senderPlayer.getName())
                                    .replace("<amount>", getFormatted(amount))
                                    .replace("<icon>", currency.icon())
                            );
                        }

                    }

                    User targetUser = userService.get(s.uuid());
                    boolean isTargetOnline = targetUser != null;

                    if (isTargetOnline) {
                        uuids.add(targetUser.getUuid());
                    } else if (transactions != null) {
                        transactions.publishAsync(new TransactionMessage(null, s.uuid()));
                    }

                }

                case QueryUserResult.Error e -> {

                    if (senderPlayer != null) {

                        if (e.type() == ErrorType.NOT_FOUND) {
                            senderPlayer.sendRichMessage(lang.getString("database-not-found", "<red>No player with the entered name was found in the database."));
                        } else if (e.type() == ErrorType.NOT_ENOUGH_BALANCE) {
                            senderPlayer.sendRichMessage(lang.getString("not-enough-balance", "<red>You don't have enough balance to complete this transaction!"));
                        } else { // Exception Error
                            senderPlayer.sendRichMessage(lang.getString("payment-error", "<red>An unexpected error occurred while attempting to complete a transaction. Please try again, and if the problem persists, contact an administrator."));
                        }

                    }

                }

                default -> throw new IllegalStateException("Unexpected value: " + result);
            }

            return result;

        }, Bukkit.getScheduler().getMainThreadExecutor(PaperEconomyPlugin.plugin));

    }

    @Override
    public @NotNull CompletableFuture<QueryUserResult> withdrawCurrencyAndNotifyIfNeeded(@Nullable Player senderPlayer, @NotNull Player targetPlayer, @NotNull User targetUser, @NotNull Currency currency, double amount) {

        long cents = (long) (amount * 100);

        return userService.withdrawCurrency(targetPlayer.getUniqueId(), currency, cents).thenApplyAsync(result -> {

            switch (result) {

                case QueryUserResult.SuccessNoData ignored -> {

                    if (senderPlayer != null) {

                        senderPlayer.sendRichMessage(lang.getString("remove-transaction-successful", "<green>You have successfully removed <white><amount><icon></bold> <green>from <white><target><green>."),
                                Placeholder.unparsed("target", targetPlayer.getName()),
                                Placeholder.unparsed("amount", getFormatted(amount)),
                                Placeholder.parsed("icon", currency.icon())
                        );

                        targetPlayer.sendRichMessage(lang.getString("remove-transaction-received-successful", "<green><white><amount><icon></bold> <green>was successfully removed from your balance by <white><sender><green>."),
                                Placeholder.unparsed("sender", senderPlayer.getName()),
                                Placeholder.unparsed("amount", getFormatted(amount)),
                                Placeholder.parsed("icon", currency.icon())
                        );

                    }

                    boolean isTargetOnline = targetUser.isOnline();

                    if (isTargetOnline) {
                        uuids.add(targetUser.getUuid());
                    } else if (transactions != null) {
                        transactions.publishAsync(new TransactionMessage(null, targetUser.getUuid()));
                    }

                }

                case QueryUserResult.Error e -> {

                    if (senderPlayer != null) {

                        if (e.type() == ErrorType.NOT_ENOUGH_BALANCE) {
                            senderPlayer.sendRichMessage(lang.getString("not-enough-balance", "<red>You don't have enough balance to complete this transaction!"));
                        } else if (e.type() == ErrorType.NOT_FOUND) {
                            senderPlayer.sendRichMessage(lang.getString("database-not-found", "<red>No player with the entered name was found in the database."));
                        } else { // Exception Error
                            senderPlayer.sendRichMessage(lang.getString("payment-error", "<red>An unexpected error occurred while attempting to complete a transaction. Please try again, and if the problem persists, contact an administrator."));
                        }

                    }

                }

                default -> throw new IllegalStateException("Unexpected value: " + result);
            }

            return result;

        }, Bukkit.getScheduler().getMainThreadExecutor(PaperEconomyPlugin.plugin));

    }

    @Override
    public @NotNull CompletableFuture<QueryUserResult> withdrawCurrencyAndNotifyIfNeeded(@Nullable Player senderPlayer, @NotNull String target, @NotNull Currency currency, double amount) {

        long cents = (long) (amount * 100);

        return userService.withdrawCurrency(target, currency, cents).thenApplyAsync(result -> {

            switch (result) {

                case QueryUserResult.Success s -> {

                    if (senderPlayer != null) {

                        senderPlayer.sendRichMessage(lang.getString("remove-transaction-successful", "<green>You have successfully removed <white><amount><icon></bold> <green>from <white><target><green>."),
                                Placeholder.unparsed("target", s.nickname()),
                                Placeholder.unparsed("amount", getFormatted(amount)),
                                Placeholder.parsed("icon", currency.icon())
                        );

                        if (messaging != null) {
                            messaging.sendMessage(target, lang.getString("remove-transaction-received-successful", "<green><white><amount><icon></bold> <green>was successfully removed from your balance by <white><sender><green>.")
                                    .replace("<sender>", senderPlayer.getName())
                                    .replace("<amount>", getFormatted(amount))
                                    .replace("<icon>", currency.icon())
                            );
                        }

                    }

                    User targetUser = userService.get(s.uuid());
                    boolean isTargetOnline = targetUser != null;

                    if (isTargetOnline) {
                        uuids.add(targetUser.getUuid());
                    } else if (transactions != null) {
                        transactions.publishAsync(new TransactionMessage(null, s.uuid()));
                    }

                }

                case QueryUserResult.Error e -> {

                    if (senderPlayer != null) {

                        if (e.type() == ErrorType.NOT_FOUND) {
                            senderPlayer.sendRichMessage(lang.getString("database-not-found", "<red>No player with the entered name was found in the database."));
                        } else if (e.type() == ErrorType.NOT_ENOUGH_BALANCE) {
                            senderPlayer.sendRichMessage(lang.getString("not-enough-balance", "<red>You don't have enough balance to complete this transaction!"));
                        } else { // Exception Error
                            senderPlayer.sendRichMessage(lang.getString("payment-error", "<red>An unexpected error occurred while attempting to complete a transaction. Please try again, and if the problem persists, contact an administrator."));
                        }

                    }

                }

                default -> throw new IllegalStateException("Unexpected value: " + result);
            }

            return result;

        }, Bukkit.getScheduler().getMainThreadExecutor(PaperEconomyPlugin.plugin));

    }

    private String getFormatted(double amount) {
        return PaperEconomyPlugin.plugin.formatter.formatNumber(amount);
    }

}
