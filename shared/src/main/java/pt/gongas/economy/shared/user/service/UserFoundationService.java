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

package pt.gongas.economy.shared.user.service;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pt.gongas.database.executor.DatabaseExecutor;
import pt.gongas.economy.shared.user.QueryUserResult;
import pt.gongas.economy.shared.user.RankingUser;
import pt.gongas.economy.shared.currency.Currency;
import pt.gongas.economy.shared.user.User;

import java.sql.Connection;
import java.sql.SQLException;
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
    @NotNull CompletableFuture<QueryUserResult> updateCurrencies(@NotNull UUID senderUuid, @NotNull UUID receiverUuid, @NotNull Currency currency, long cents);

    /**
     * Transfers a certain amount of currency from a user to another by receiver's nickname.
     *
     * @param senderUuid       UUID of the sender
     * @param receiverNickname Nickname of the receiver
     * @param currency         Currency to transfer
     * @param cents            Amount to transfer
     * @return CompletableFuture containing QueryUserResult with success or error
     */
    @NotNull CompletableFuture<QueryUserResult> updateCurrencies(@NotNull UUID senderUuid, @NotNull String receiverNickname, @NotNull Currency currency, long cents);

    /**
     * Fetches the balances of multiple users for all currencies.
     *
     * @param uuids Collection of user UUIDs
     * @return CompletableFuture containing a map: UUID -> (Currency -> balance)
     */
    @NotNull CompletableFuture<Map<UUID, Map<Currency, Long>>> fetchPlayerBalances(@NotNull Collection<UUID> uuids);

    /**
     * Sets the currency amount for a user by UUID.
     *
     * @param uuid     User UUID
     * @param currency Currency to set
     * @param cents    Amount to set
     * @return CompletableFuture containing QueryUserResult with success or error
     */
    @NotNull CompletableFuture<QueryUserResult> setCurrency(@NotNull UUID uuid, @NotNull Currency currency, long cents);

    /**
     * Sets the currency amount for a user by nickname.
     *
     * @param nickname User nickname
     * @param currency Currency to set
     * @param cents    Amount to set
     * @return CompletableFuture containing QueryUserResult with success or error
     */
    @NotNull CompletableFuture<QueryUserResult> setCurrency(@NotNull String nickname, @NotNull Currency currency, long cents);

    /**
     * Adds currency to a user by UUID.
     *
     * @param uuid     User UUID
     * @param currency Currency to add
     * @param cents    Amount to add
     * @return CompletableFuture containing QueryUserResult with success or error
     */
    @NotNull CompletableFuture<QueryUserResult> addCurrency(@NotNull UUID uuid, @NotNull Currency currency, long cents);

    /**
     * Adds currency to a user by nickname.
     *
     * @param nickname User nickname
     * @param currency Currency to add
     * @param cents    Amount to add
     * @return CompletableFuture containing QueryUserResult with success or error
     */
    @NotNull CompletableFuture<QueryUserResult> addCurrency(@NotNull String nickname, @NotNull Currency currency, long cents);

    /**
     * Removes currency from a user by UUID, subtracting up to the available balance.
     *
     * @param uuid     User UUID
     * @param currency Currency to remove
     * @param cents    Amount to remove
     * @return CompletableFuture containing QueryUserResult with success or error
     */
    @NotNull CompletableFuture<QueryUserResult> removeCurrency(@NotNull UUID uuid, @NotNull Currency currency, long cents);

    /**
     * Removes currency from a user by nickname, subtracting up to the available balance.
     *
     * @param nickname User nickname
     * @param currency Currency to remove
     * @param cents    Amount to remove
     * @return CompletableFuture containing QueryUserResult with success or error
     */
    @NotNull CompletableFuture<QueryUserResult> removeCurrency(@NotNull String nickname, @NotNull Currency currency, long cents);

    /**
     * Withdraws currency from a user by UUID, only if sufficient balance is available.
     *
     * @param uuid     User UUID
     * @param currency Currency to withdraw
     * @param cents    Amount to withdraw
     * @return CompletableFuture containing QueryUserResult with success or error
     */
    @NotNull CompletableFuture<QueryUserResult> withdrawCurrency(@NotNull UUID uuid, @NotNull Currency currency, long cents);

    /**
     * Withdraws currency from a user by nickname, only if sufficient balance is available.
     *
     * @param nickname User nickname
     * @param currency Currency to withdraw
     * @param cents    Amount to withdraw
     * @return CompletableFuture containing QueryUserResult with success or error
     */
    @NotNull CompletableFuture<QueryUserResult> withdrawCurrency(@NotNull String nickname, @NotNull Currency currency, long cents);

    /**
     * Retrieves the amount of a specific currency for a user by nickname.
     *
     * @param nickname User nickname
     * @param currency Currency to query
     * @return CompletableFuture containing QueryUserResult with balance info or error
     */
    @NotNull CompletableFuture<QueryUserResult> getCurrency(@NotNull String nickname, @NotNull Currency currency);

    /**
     * Removes a user from cache.
     *
     * @param uuid User UUID
     * @return The removed User object
     */
    @ApiStatus.Internal User remove(@NotNull UUID uuid);

    /**
     * Stores or updates a User in cache.
     *
     * @param user User object to store
     */
    @ApiStatus.Internal void put(@NotNull User user);

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
    CompletableFuture<@Nullable User> getOrCreateDataAndUpdate(@NotNull UUID uuid, @NotNull String nickname);

    /**
     * Retrieves a paginated leaderboard for a specific currency.
     *
     * @param currency Currency to rank
     * @param page     Page number
     * @param pageSize Number of users per page
     * @return CompletableFuture containing list of RankingUser
     */
    @NotNull CompletableFuture<@Nullable List<RankingUser>> getTop(@NotNull Currency currency, int page, int pageSize);

    /**
     * Retrieves the leaderboard starting from a specific position (seek forward).
     *
     * @param currency   Currency to rank
     * @param lastAmount Last amount on previous page
     * @param lastLogin  Last login date on previous page
     * @param lastUuid   Last UUID on previous page
     * @param pageSize   Number of users per page
     * @return CompletableFuture containing list of RankingUser
     */
    @NotNull CompletableFuture<@Nullable List<RankingUser>> getTopSeek(@NotNull Currency currency, @NotNull Long lastAmount, @NotNull LocalDateTime lastLogin, @NotNull UUID lastUuid, int pageSize);

    /**
     * Retrieves the leaderboard starting backward from a specific position (seek backward).
     *
     * @param currency    Currency to rank
     * @param firstAmount First amount on previous page
     * @param firstLogin  First login date on previous page
     * @param firstUuid   First UUID on previous page
     * @param pageSize    Number of users per page
     * @return CompletableFuture containing list of RankingUser
     */
    @NotNull CompletableFuture<@Nullable List<RankingUser>> getTopSeekBackward(@NotNull Currency currency, @NotNull Long firstAmount, @NotNull LocalDateTime firstLogin, @NotNull UUID firstUuid, int pageSize);

    /**
     * Low-level method for adding currency to a user's balance in the database.
     *
     * <p>This method is intended for advanced use cases where you want to synchronize
     * economy transactions with your own plugin's database transactions. Use only if
     * your updates are applied directly in the database (real-time). For pending updates
     * in a cache or similar, use the standard methods provided by {@link pt.gongas.economy.shared.api.EconomyApi}.</p>
     *
     * <p>This method does not start, commit, or rollback transactions itself. If you want
     * transaction management, use {@link pt.gongas.economy.shared.transaction.EconomyTransactionalApi},
     * which handles transaction lifecycle for you.</p>
     *
     * <p><b>Important:</b> After executing this method, it is your responsibility to
     * handle post-transaction operations such as updating caches. This is commonly done asynchronously, for example:
     * <pre>
     * CompletableFuture.supplyAsync(() -&gt; transactionalApi.executeInEconomyTransaction(...))
     *     .thenAcceptAsync(result -&gt; {
     *
     *         // update economy cache here
     *
     *         UUID sellerUuid = auctionItem.getSellerUuid();
     *
     *         EconomyApi<Player> api = YOUR_PLUGIN_INSTANCE.economyApi.key();
     *         User buyerUser = api.getUserService().get(buyerUuid);
     *         User sellerUser = api.getUserService().get(sellerUuid);
     *
     *         boolean isBuyerOnline = buyerUser != null;
     *         boolean isSellerOnline = sellerUser != null;
     *
     *         if (isBuyerOnline) {
     *              api.getTrackedUuids().add(buyerUser.getUuid());
     *         }
     *
     *         if (isSellerOnline) {
     *              api.getTrackedUuids().add(sellerUser.getUuid());
     *         }
     *
     *         RTopic transactions = api.getTransactions();
     *
     *         if (transactions != null && (!isBuyerOnline || !isSellerOnline)) {
     *              UUID senderUuid = isBuyerOnline ? null : buyerUuid;
     *              UUID receiverUuid = isSellerOnline ? null : sellerUuid;
     *              transactions.publishAsync(new TransactionMessage(senderUuid, receiverUuid));
     *         }
     *
     *         // update your plugin cache here if needed
     *
     *     }, Bukkit.getScheduler().getMainThreadExecutor(YOUR_PLUGIN_INSTANCE));
     * </pre>
     *
     * <p>This ensures that the database operations are performed atomically while keeping
     * caches and other dependent systems in sync without blocking the main thread.</p>
     * </p>
     *
     * @param uuid       User UUID
     * @param currency   Currency to add
     * @param cents      Amount to add
     * @param executor   DatabaseExecutor to use for the transaction
     * @param connection Connection to use for the transaction
     * @return QueryUserResult indicating success or error
     * @throws SQLException If a database error occurs
     */
    QueryUserResult addCurrencyLowLevel(UUID uuid, Currency currency, long cents, DatabaseExecutor executor, Connection connection) throws SQLException;

    /**
     * Low-level method for adding currency to a user's balance in the database.
     *
     * <p>This method is intended for advanced use cases where you want to synchronize
     * economy transactions with your own plugin's database transactions. Use only if
     * your updates are applied directly in the database (real-time). For pending updates
     * in a cache or similar, use the standard methods provided by {@link pt.gongas.economy.shared.api.EconomyApi}.</p>
     *
     * <p>This method does not start, commit, or rollback transactions itself. If you want
     * transaction management, use {@link pt.gongas.economy.shared.transaction.EconomyTransactionalApi},
     * which handles transaction lifecycle for you.</p>
     *
     * <p><b>Important:</b> After executing this method, it is your responsibility to
     * handle post-transaction operations such as updating caches. This is commonly done asynchronously, for example:
     * <pre>
     * CompletableFuture.supplyAsync(() -&gt; transactionalApi.executeInEconomyTransaction(...))
     *     .thenAcceptAsync(result -&gt; {
     *
     *         // update economy cache here
     *
     *         UUID sellerUuid = auctionItem.getSellerUuid();
     *
     *         EconomyApi<Player> api = YOUR_PLUGIN_INSTANCE.economyApi.key();
     *         User buyerUser = api.getUserService().get(buyerUuid);
     *         User sellerUser = api.getUserService().get(sellerUuid);
     *
     *         boolean isBuyerOnline = buyerUser != null;
     *         boolean isSellerOnline = sellerUser != null;
     *
     *         if (isBuyerOnline) {
     *              api.getTrackedUuids().add(buyerUser.getUuid());
     *         }
     *
     *         if (isSellerOnline) {
     *              api.getTrackedUuids().add(sellerUser.getUuid());
     *         }
     *
     *         RTopic transactions = api.getTransactions();
     *
     *         if (transactions != null && (!isBuyerOnline || !isSellerOnline)) {
     *              UUID senderUuid = isBuyerOnline ? null : buyerUuid;
     *              UUID receiverUuid = isSellerOnline ? null : sellerUuid;
     *              transactions.publishAsync(new TransactionMessage(senderUuid, receiverUuid));
     *         }
     *
     *         // update your plugin cache here if needed
     *
     *     }, Bukkit.getScheduler().getMainThreadExecutor(YOUR_PLUGIN_INSTANCE));
     * </pre>
     *
     * <p>This ensures that the database operations are performed atomically while keeping
     * caches and other dependent systems in sync without blocking the main thread.</p>
     * </p>
     *
     * @param uuid       User UUID
     * @param currency   Currency to withdraw
     * @param cents      Amount to withdraw
     * @param executor   DatabaseExecutor to use for the transaction
     * @param connection Connection to use for the transaction
     * @return QueryUserResult indicating success or error
     * @throws SQLException If a database error occurs
     */
    QueryUserResult withdrawCurrencyLowLevel(UUID uuid, Currency currency, long cents, DatabaseExecutor executor, Connection connection) throws SQLException;

    /**
     * Low-level method for adding currency to a user's balance in the database.
     *
     * <p>This method is intended for advanced use cases where you want to synchronize
     * economy transactions with your own plugin's database transactions. Use only if
     * your updates are applied directly in the database (real-time). For pending updates
     * in a cache or similar, use the standard methods provided by {@link pt.gongas.economy.shared.api.EconomyApi}.</p>
     *
     * <p>This method does not start, commit, or rollback transactions itself. If you want
     * transaction management, use {@link pt.gongas.economy.shared.transaction.EconomyTransactionalApi},
     * which handles transaction lifecycle for you.</p>
     *
     * <p><b>Important:</b> After executing this method, it is your responsibility to
     * handle post-transaction operations such as updating caches. This is commonly done asynchronously, for example:
     * <pre>
     * CompletableFuture.supplyAsync(() -&gt; transactionalApi.executeInEconomyTransaction(...))
     *     .thenAcceptAsync(result -&gt; {
     *
     *         // update economy cache here
     *
     *         UUID sellerUuid = auctionItem.getSellerUuid();
     *
     *         EconomyApi<Player> api = YOUR_PLUGIN_INSTANCE.economyApi.key();
     *         User buyerUser = api.getUserService().get(buyerUuid);
     *         User sellerUser = api.getUserService().get(sellerUuid);
     *
     *         boolean isBuyerOnline = buyerUser != null;
     *         boolean isSellerOnline = sellerUser != null;
     *
     *         if (isBuyerOnline) {
     *              api.getTrackedUuids().add(buyerUser.getUuid());
     *         }
     *
     *         if (isSellerOnline) {
     *              api.getTrackedUuids().add(sellerUser.getUuid());
     *         }
     *
     *         RTopic transactions = api.getTransactions();
     *
     *         if (transactions != null && (!isBuyerOnline || !isSellerOnline)) {
     *              UUID senderUuid = isBuyerOnline ? null : buyerUuid;
     *              UUID receiverUuid = isSellerOnline ? null : sellerUuid;
     *              transactions.publishAsync(new TransactionMessage(senderUuid, receiverUuid));
     *         }
     *
     *         // update your plugin cache here if needed
     *
     *     }, Bukkit.getScheduler().getMainThreadExecutor(YOUR_PLUGIN_INSTANCE));
     * </pre>
     *
     * <p>This ensures that the database operations are performed atomically while keeping
     * caches and other dependent systems in sync without blocking the main thread.</p>
     * </p>
     *
     * @param senderUuid   UUID of the sender
     * @param receiverUuid UUID of the receiver
     * @param currency     Currency to transfer
     * @param cents        Amount to transfer
     * @param executor     DatabaseExecutor to use for the transaction
     * @param connection   Connection to use for the transaction
     * @return QueryUserResult indicating success or error
     * @throws SQLException If a database error occurs
     */
    QueryUserResult updateCurrenciesLowLevel(UUID senderUuid, UUID receiverUuid, Currency currency, long cents, DatabaseExecutor executor, Connection connection) throws SQLException;

}
