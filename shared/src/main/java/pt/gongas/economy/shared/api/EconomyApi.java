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
import pt.gongas.economy.shared.user.QueryUserResult;
import pt.gongas.economy.shared.user.User;

import java.util.concurrent.CompletableFuture;

@SuppressWarnings({"UnusedReturnValue", "UnusedDeclaration"})
public interface EconomyApi<P> {

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
     * <p>
     * If {@code notify} is true, both the performing player and the target player are notified.
     *
     * @param senderPlayer The player performing the action (non-null)
     * @param targetPlayer The target player's object (non-null)
     * @param targetUser   The target's user data (non-null)
     * @param currency     The currency to set
     * @param amount       The new amount
     * @param notify       If true, notify both senderPlayer and targetPlayer; otherwise, no notification
     * @return CompletableFuture containing the result of the operation
     */
    CompletableFuture<QueryUserResult> setCurrencyAndNotifyIfNeeded(@NotNull P senderPlayer, @NotNull P targetPlayer, @NotNull User targetUser, @NotNull Currency currency, double amount, boolean notify);

    /**
     * Sets the currency amount for another player by name.
     * If `notify` is true, both the performing player and the target are notified.
     *
     * @param senderPlayer The player performing the action (non-null)
     * @param target The target player's name (non-null)
     * @param currency The currency to set
     * @param amount The new amount
     * @param notify If true, notify both player and target; otherwise, no notification
     * @return CompletableFuture with the result of the operation
     */
    CompletableFuture<QueryUserResult> setCurrencyAndNotifyIfNeeded(@NotNull P senderPlayer, @NotNull String target, @NotNull Currency currency, double amount, boolean notify);

    /**
     * Adds currency to another player.
     * <p>
     * If {@code notify} is true, both the performing player and the target player are notified.
     *
     * @param senderPlayer The player performing the action (non-null)
     * @param targetPlayer The target player's object (non-null)
     * @param senderUser   The sender's user data (non-null)
     * @param targetUser   The target's user data (non-null)
     * @param currency     The currency to add
     * @param amount       The amount to add
     * @param notify       If true, notify both senderPlayer and targetPlayer; otherwise, no notification
     * @return CompletableFuture containing the result of the operation
     */
    CompletableFuture<QueryUserResult> addCurrencyAndNotifyIfNeeded(@NotNull P senderPlayer, @NotNull P targetPlayer, @NotNull User senderUser, @NotNull User targetUser, @NotNull Currency currency, double amount, boolean notify);

    /**
     * Adds currency to another player by name.
     * If `notify` is true, both the performing player and the target are notified.
     *
     * @param senderPlayer The player performing the action (non-null)
     * @param target The target player's name (non-null)
     * @param senderUser The sender's user data
     * @param currency The currency to add
     * @param amount The amount to add
     * @param notify If true, notify both player and target; otherwise, no notification
     * @return CompletableFuture with the result of the operation
     */
    CompletableFuture<QueryUserResult> addCurrencyAndNotifyIfNeeded(@NotNull P senderPlayer, @NotNull String target, @NotNull User senderUser, @NotNull Currency currency, double amount, boolean notify);

    /**
     * Removes currency from another player, subtracting up to the available balance.
     * <p>
     * If {@code notify} is true, both the performing player and the target player are notified.
     *
     * @param senderPlayer The player performing the action (non-null)
     * @param targetPlayer The target player's object (non-null)
     * @param senderUser   The sender's user data (non-null)
     * @param targetUser   The target's user data (non-null)
     * @param currency     The currency to remove
     * @param amount       The amount to remove
     * @param notify       If true, notify both senderPlayer and targetPlayer; otherwise, no notification
     * @return CompletableFuture containing the result of the operation
     */
    CompletableFuture<QueryUserResult> removeCurrencyAndNotifyIfNeeded(@NotNull P senderPlayer, @NotNull P targetPlayer, @NotNull User senderUser, @NotNull User targetUser, @NotNull Currency currency, double amount, boolean notify);

    /**
     * Removes currency from another player by name, subtracting up to the available balance.
     * If `notify` is true, both the performing player and the target are notified.
     *
     * @param senderPlayer The player performing the action (non-null)
     * @param target The target player's name (non-null)
     * @param senderUser The sender's user data
     * @param currency The currency to remove
     * @param amount The amount to remove
     * @param notify If true, notify both player and target; otherwise, no notification
     * @return CompletableFuture with the result of the operation
     */
    CompletableFuture<QueryUserResult> removeCurrencyAndNotifyIfNeeded(@NotNull P senderPlayer, @NotNull String target, @NotNull User senderUser, @NotNull Currency currency, double amount, boolean notify);

    /**
     * Withdraws currency from another player, only if sufficient balance is available.
     * <p>
     * If {@code notify} is true, both the performing player and the target player are notified.
     *
     * @param senderPlayer The player performing the action (non-null)
     * @param targetPlayer The target player's object (non-null)
     * @param senderUser   The sender's user data (non-null)
     * @param targetUser   The target's user data (non-null)
     * @param currency     The currency to withdraw
     * @param amount       The amount to withdraw
     * @param notify       If true, notify both senderPlayer and targetPlayer; otherwise, no notification
     * @return CompletableFuture containing the result of the operation
     */
    CompletableFuture<QueryUserResult> withdrawCurrencyAndNotifyIfNeeded(@NotNull P senderPlayer, @NotNull P targetPlayer, @NotNull User senderUser, @NotNull User targetUser, @NotNull Currency currency, double amount, boolean notify);

    /**
     * Withdraws currency from another player by name, only if sufficient balance is available.
     * If `notify` is true, both the performing player and the target are notified.
     *
     * @param senderPlayer The player performing the action (non-null)
     * @param target The target player's name (non-null)
     * @param senderUser The sender's user data
     * @param currency The currency to withdraw
     * @param amount The amount to withdraw
     * @param notify If true, notify both player and target; otherwise, no notification
     * @return CompletableFuture with the result of the operation
     */
    CompletableFuture<QueryUserResult> withdrawCurrencyAndNotifyIfNeeded(@NotNull P senderPlayer, @NotNull String target, @NotNull User senderUser, @NotNull Currency currency, double amount, boolean notify);

}