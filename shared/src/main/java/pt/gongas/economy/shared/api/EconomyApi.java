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

package pt.gongas.economy.shared.api;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pt.gongas.economy.shared.currency.Currency;
import pt.gongas.economy.shared.currency.service.CurrencyFoundationService;
import pt.gongas.economy.shared.user.QueryUserResult;
import pt.gongas.economy.shared.user.User;
import pt.gongas.economy.shared.user.service.UserFoundationService;

import java.util.concurrent.CompletableFuture;

@SuppressWarnings({"UnusedReturnValue", "UnusedDeclaration"})
public interface EconomyApi<P> {

    /**
     * Returns the currency service instance.
     *
     * @return the CurrencyFoundationService
     */
    CurrencyFoundationService getCurrencyService();

    /**
     * Returns the user service instance.
     *
     * @return the UserFoundationService
     */
    UserFoundationService getUserService();

    /**
     * Gets the current balance of a specific currency for a target player.
     * Optionally notifies the requesting player if `player` is not null.
     *
     * @param player The player to receive the notification (nullable)
     * @param target The target player's name
     * @param currency The currency to query
     * @return CompletableFuture containing the query result
     */
    CompletableFuture<QueryUserResult> getCurrencyAndNotifyIfNeeded(@Nullable P player, @NotNull String target, @NotNull Currency currency);

    /**
     * Pays a certain amount of currency to another player by name.
     * If `notify` is true, both the sending player and the target are notified.
     *
     * @param senderPlayer The player performing the action (non-null)
     * @param targetPlayer The target player's name (non-null)
     * @param senderUser The sender's user data
     * @param currency The currency to transfer
     * @param amount The amount to transfer
     * @param notify If true, notify both player and target; otherwise, no notification
     * @return CompletableFuture with the result of the operation
     */
    CompletableFuture<QueryUserResult> payCurrencyAndNotifyIfNeeded(@NotNull P senderPlayer, @NotNull P targetPlayer, @NotNull User senderUser, @NotNull User targetUser, @NotNull Currency currency, double amount, boolean notify);

    /**
     * Pays a certain amount of currency to another player by name.
     * If `notify` is true, both the performing player and the target are notified.
     *
     * @param senderPlayer The player performing the action (non-null)
     * @param target The target player's name (non-null)
     * @param senderUser The sender's user data
     * @param currency The currency to transfer
     * @param amount The amount to transfer
     * @param notify If true, notify both player and target; otherwise, no notification
     * @return CompletableFuture with the result of the operation
     */
    CompletableFuture<QueryUserResult> payCurrencyAndNotifyIfNeeded(@NotNull P senderPlayer, @NotNull String target, @NotNull User senderUser, @NotNull Currency currency, double amount, boolean notify);

    /**
     * Sets the currency amount for another player.
     * If `senderPlayer` is not null, both the performing player and the target player are notified.
     *
     * @param senderPlayer The player performing the action (nullable). If null, no notification is sent.
     * @param targetPlayer The target player's object (non-null)
     * @param targetUser   The target's user data (non-null)
     * @param currency     The currency to set
     * @param amount       The new amount
     * @return CompletableFuture containing the result of the operation
     */
    CompletableFuture<QueryUserResult> setCurrencyAndNotifyIfNeeded(@Nullable P senderPlayer, @NotNull P targetPlayer, @NotNull User targetUser, @NotNull Currency currency, double amount);

    /**
     * Sets the currency amount for another player by name.
     * If `senderPlayer` is not null, both the performing player and the target are notified.
     *
     * @param senderPlayer The player performing the action (nullable). If null, no notification is sent.
     * @param target       The target player's name (non-null)
     * @param currency     The currency to set
     * @param amount       The new amount
     * @return CompletableFuture with the result of the operation
     */
    CompletableFuture<QueryUserResult> setCurrencyAndNotifyIfNeeded(@Nullable P senderPlayer, @NotNull String target, @NotNull Currency currency, double amount);

    /**
     * Adds currency to another player.
     * If `senderPlayer` is not null, both the performing player and the target player are notified.
     *
     * @param senderPlayer The player performing the action (nullable). If null, no notification is sent.
     * @param targetPlayer The target player's object (non-null)
     * @param targetUser   The target's user data (non-null)
     * @param currency     The currency to add
     * @param amount       The amount to add
     * @return CompletableFuture containing the result of the operation
     */

    CompletableFuture<QueryUserResult> addCurrencyAndNotifyIfNeeded(@Nullable P senderPlayer, @NotNull P targetPlayer, @NotNull User targetUser, @NotNull Currency currency, double amount);

    /**
     * Adds currency to another player by name.
     * If `senderPlayer` is not null, both the performing player and the target are notified.
     *
     * @param senderPlayer The player performing the action (nullable). If null, no notification is sent.
     * @param target       The target player's name (non-null)
     * @param currency     The currency to add
     * @param amount       The amount to add
     * @return CompletableFuture with the result of the operation
     */
    CompletableFuture<QueryUserResult> addCurrencyAndNotifyIfNeeded(@Nullable P senderPlayer, @NotNull String target, @NotNull Currency currency, double amount);

    /**
     * Removes currency from another player, subtracting up to the available balance.
     * <p>
     * If {@code notify} is true, both the performing player and the target player are notified.
     *
     * @param senderPlayer The player performing the action (non-null)
     * @param targetPlayer The target player's object (non-null)
     * @param targetUser   The target's user data (non-null)
     * @param currency     The currency to remove
     * @param amount       The amount to remove
     * @return CompletableFuture containing the result of the operation
     */
    CompletableFuture<QueryUserResult> removeCurrencyAndNotifyIfNeeded(@Nullable P senderPlayer, @NotNull P targetPlayer, @NotNull User targetUser, @NotNull Currency currency, double amount);

    /**
     * Removes currency from another player by name, subtracting up to the available balance.
     * If `senderPlayer` is not null, both the performing player and the target are notified.
     *
     * @param senderPlayer The player performing the action (nullable). If null, no notification is sent.
     * @param target       The target player's name (non-null)
     * @param currency     The currency to remove
     * @param amount       The amount to remove
     * @return CompletableFuture with the result of the operation
     */
    CompletableFuture<QueryUserResult> removeCurrencyAndNotifyIfNeeded(@Nullable P senderPlayer, @NotNull String target, @NotNull Currency currency, double amount);

    /**
     * Withdraws currency from another player, only if sufficient balance is available.
     * If `senderPlayer` and `senderUser` are not null, both the performing player and the target are notified.
     *
     * @param senderPlayer The player performing the action (nullable). If null, no notification is sent.
     * @param targetPlayer The target player's object (non-null)
     * @param targetUser   The target's user data (non-null)
     * @param currency     The currency to withdraw
     * @param amount       The amount to withdraw
     * @return CompletableFuture containing the result of the operation
     */
    CompletableFuture<QueryUserResult> withdrawCurrencyAndNotifyIfNeeded(@Nullable P senderPlayer, @NotNull P targetPlayer, @NotNull User targetUser, @NotNull Currency currency, double amount);

    /**
     * Withdraws currency from another player by name, only if sufficient balance is available.
     * If `senderPlayer` is not null, both the performing player and the target are notified.
     *
     * @param senderPlayer The player performing the action (nullable). If null, no notification is sent.
     * @param target The target player's name (non-null)
     * @param currency The currency to withdraw
     * @param amount The amount to withdraw
     * @return CompletableFuture with the result of the operation
     */
    CompletableFuture<QueryUserResult> withdrawCurrencyAndNotifyIfNeeded(@Nullable P senderPlayer, @NotNull String target, @NotNull Currency currency, double amount);

}