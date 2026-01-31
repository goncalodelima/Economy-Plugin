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

package pt.gongas.economy.shared.transaction;

import pt.gongas.database.Database;

import java.sql.SQLException;

/**
 * Interface for executing economy-related operations within a single database transaction.
 *
 * <p>This API allows you to perform multiple operations atomically, ensuring that
 * either all succeed or none are applied. It is especially useful when you need
 * to synchronize economy transactions with your plugin's own database changes.</p>
 *
 * <p>Operations executed via this API should use the low-level methods from
 * {@link pt.gongas.economy.shared.user.service.UserService} (e.g., {@code addCurrencyLowLevel},
 * {@code withdrawCurrencyLowLevel}, {@code updateCurrenciesLowLevel}) to ensure
 * that changes are applied directly in the database within the transaction.</p>
 *
 * <p><b>Important:</b> You should not call high-level API methods (from {@link pt.gongas.economy.shared.api.EconomyApi})
 * inside this transaction, as they may use caching or deferred updates and won't participate in the transaction.</p>
 *
 * <p><b>Post-Transaction Handling:</b> After executing executeInEconomyTransaction method, it is your responsibility to
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
 */
public interface EconomyTransactionalApi {

    boolean executeInEconomyTransaction(Database database, EconomyTransactionAction action) throws SQLException;

}