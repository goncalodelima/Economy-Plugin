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

package pt.gongas.economy.shared.user.repository;

import org.jetbrains.annotations.Nullable;
import pt.gongas.economy.shared.currency.Currency;
import pt.gongas.economy.shared.user.QueryUserResult;
import pt.gongas.economy.shared.user.RankingUser;
import pt.gongas.economy.shared.user.User;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface UserFoundationRepository {

    void setup();

    CompletableFuture<QueryUserResult> updateCurrencies(UUID senderUuid, UUID receiverUuid, Currency currency, long cents);

    CompletableFuture<QueryUserResult> updateCurrencies(UUID senderUuid, String receiverNickname, Currency currency, long cents);

    CompletableFuture<Map<UUID, Map<Currency, Long>>> fetchPlayerBalances(Collection<UUID> uuids);

    CompletableFuture<QueryUserResult> setCurrency(UUID uuid, Currency currency, long cents);

    CompletableFuture<QueryUserResult> setCurrency(String nickname, Currency currency, long cents);

    CompletableFuture<QueryUserResult> addCurrency(UUID uuid, Currency currency, long cents);

    CompletableFuture<QueryUserResult> addCurrency(String nickname, Currency currency, long cents);

    CompletableFuture<QueryUserResult> getCurrency(String nickname, Currency currency);

    CompletableFuture<QueryUserResult> removeCurrency(UUID uuid, Currency currency, long cents);

    CompletableFuture<QueryUserResult> removeCurrency(String nickname, Currency currency, long cents);

    CompletableFuture<QueryUserResult> withdrawCurrency(UUID uuid, Currency currency, long cents);

    CompletableFuture<QueryUserResult> withdrawCurrency(String nickname, Currency currency, long cents);

    @Nullable User findOrCreateAndUpdate(UUID uuid, String nickname);

    List<RankingUser> findTop(Currency currency, int page);

    List<RankingUser> findTopSeek(Currency currency, Long lastAmount, LocalDateTime lastLogin, UUID lastUuid);

    List<RankingUser> findTopSeekBackward(Currency currency, Long firstAmount, LocalDateTime firstLogin, UUID firstUuid);

}
