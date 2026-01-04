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

package pt.gongas.economy.shared.user.service;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pt.gongas.economy.shared.user.QueryUserResult;
import pt.gongas.economy.shared.user.RankingUser;
import pt.gongas.economy.shared.currency.Currency;
import pt.gongas.economy.shared.user.User;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface UserFoundationService {

    /**
     * Transfers a certain amount of currency from one user to another by UUIDs.
     *
     * @param senderUuid   UUID of the sender
     * @param receiverUuid UUID of the receiver
     * @param currency     Currency to transfer
     * @param cents        Amount to transfer
     * @return CompletableFuture containing QueryUserResult with success or error
     */
    CompletableFuture<QueryUserResult> updateCurrencies(UUID senderUuid, UUID receiverUuid, Currency currency, long cents);

    /**
     * Transfers a certain amount of currency from a user to another by receiver's nickname.
     *
     * @param senderUuid       UUID of the sender
     * @param receiverNickname Nickname of the receiver
     * @param currency         Currency to transfer
     * @param cents            Amount to transfer
     * @return CompletableFuture containing QueryUserResult with success or error
     */
    CompletableFuture<QueryUserResult> updateCurrencies(UUID senderUuid, String receiverNickname, Currency currency, long cents);

    /**
     * Fetches the balances of multiple users for all currencies.
     *
     * @param uuids Collection of user UUIDs
     * @return CompletableFuture containing a map: UUID -> (Currency -> balance)
     */
    CompletableFuture<Map<UUID, Map<Currency, Long>>> fetchPlayerBalances(Collection<UUID> uuids);

    /**
     * Sets the currency amount for a user by UUID.
     *
     * @param uuid     User UUID
     * @param currency Currency to set
     * @param cents    Amount to set
     * @return CompletableFuture containing QueryUserResult with success or error
     */
    CompletableFuture<QueryUserResult> setCurrency(UUID uuid, Currency currency, long cents);

    /**
     * Sets the currency amount for a user by nickname.
     *
     * @param nickname User nickname
     * @param currency Currency to set
     * @param cents    Amount to set
     * @return CompletableFuture containing QueryUserResult with success or error
     */
    CompletableFuture<QueryUserResult> setCurrency(String nickname, Currency currency, long cents);

    /**
     * Adds currency to a user by UUID.
     *
     * @param uuid     User UUID
     * @param currency Currency to add
     * @param cents    Amount to add
     * @return CompletableFuture containing QueryUserResult with success or error
     */
    CompletableFuture<QueryUserResult> addCurrency(UUID uuid, Currency currency, long cents);

    /**
     * Adds currency to a user by nickname.
     *
     * @param nickname User nickname
     * @param currency Currency to add
     * @param cents    Amount to add
     * @return CompletableFuture containing QueryUserResult with success or error
     */
    CompletableFuture<QueryUserResult> addCurrency(String nickname, Currency currency, long cents);

    /**
     * Removes currency from a user by UUID, subtracting up to the available balance.
     *
     * @param uuid     User UUID
     * @param currency Currency to remove
     * @param cents    Amount to remove
     * @return CompletableFuture containing QueryUserResult with success or error
     */
    CompletableFuture<QueryUserResult> removeCurrency(UUID uuid, Currency currency, long cents);

    /**
     * Removes currency from a user by nickname, subtracting up to the available balance.
     *
     * @param nickname User nickname
     * @param currency Currency to remove
     * @param cents    Amount to remove
     * @return CompletableFuture containing QueryUserResult with success or error
     */
    CompletableFuture<QueryUserResult> removeCurrency(String nickname, Currency currency, long cents);

    /**
     * Withdraws currency from a user by UUID, only if sufficient balance is available.
     *
     * @param uuid     User UUID
     * @param currency Currency to withdraw
     * @param cents    Amount to withdraw
     * @return CompletableFuture containing QueryUserResult with success or error
     */
    CompletableFuture<QueryUserResult> withdrawCurrency(UUID uuid, Currency currency, long cents);

    /**
     * Withdraws currency from a user by nickname, only if sufficient balance is available.
     *
     * @param nickname User nickname
     * @param currency Currency to withdraw
     * @param cents    Amount to withdraw
     * @return CompletableFuture containing QueryUserResult with success or error
     */
    CompletableFuture<QueryUserResult> withdrawCurrency(String nickname, Currency currency, long cents);

    /**
     * Retrieves the amount of a specific currency for a user by nickname.
     *
     * @param nickname User nickname
     * @param currency Currency to query
     * @return CompletableFuture containing QueryUserResult with balance info or error
     */
    CompletableFuture<QueryUserResult> getCurrency(String nickname, Currency currency);

    /**
     * Removes a user from cache.
     *
     * @param uuid User UUID
     * @return The removed User object
     */
    @ApiStatus.Internal User remove(UUID uuid);

    /**
     * Stores or updates a User in cache.
     *
     * @param user User object to store
     */
    @ApiStatus.Internal void put(User user);

    /**
     * Retrieves a user by UUID from cache.
     *
     * @param uuid User UUID
     * @return User object if found, null otherwise
     */
    @Nullable User get(@NotNull UUID uuid);

    /**
     * Retrieves or creates a user and updates its nickname and last login.
     *
     * @param uuid     User UUID
     * @param nickname User nickname
     * @return User object
     */
    @Nullable User getOrCreateDataAndUpdate(UUID uuid, String nickname);

    /**
     * Retrieves a paginated leaderboard for a specific currency.
     *
     * @param currency Currency to rank
     * @param page     Page number
     * @return CompletableFuture containing list of RankingUser
     */
    CompletableFuture<List<RankingUser>> getTop(Currency currency, int page);

    /**
     * Retrieves the leaderboard starting from a specific position (seek forward).
     *
     * @param currency   Currency to rank
     * @param lastAmount Last amount on previous page
     * @param lastLogin  Last login date on previous page
     * @param lastUuid   Last UUID on previous page
     * @return CompletableFuture containing list of RankingUser
     */
    CompletableFuture<List<RankingUser>> getTopSeek(Currency currency, Long lastAmount, LocalDateTime lastLogin, UUID lastUuid);

    /**
     * Retrieves the leaderboard starting backward from a specific position (seek backward).
     *
     * @param currency   Currency to rank
     * @param firstAmount First amount on previous page
     * @param firstLogin  First login date on previous page
     * @param firstUuid   First UUID on previous page
     * @return CompletableFuture containing list of RankingUser
     */
    CompletableFuture<List<RankingUser>> getTopSeekBackward(Currency currency, Long firstAmount, LocalDateTime firstLogin, UUID firstUuid);

}
