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

package pt.gongas.economy.shared.user.repository;

import pt.gongas.database.executor.DatabaseExecutor;
import pt.gongas.economy.shared.currency.Currency;
import pt.gongas.economy.shared.user.QueryUserResult;
import pt.gongas.economy.shared.user.RankingUser;
import pt.gongas.economy.shared.user.User;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface UserFoundationRepository {

    void setup();

    QueryUserResult updateCurrencies(UUID senderUuid, UUID receiverUuid, Currency currency, long cents);

    QueryUserResult updateCurrencies(UUID senderUuid, String receiverNickname, Currency currency, long cents);

    Map<UUID, Map<Currency, Long>> fetchPlayerBalances(Collection<UUID> uuids);

    QueryUserResult setCurrency(UUID uuid, Currency currency, long cents);

    QueryUserResult setCurrency(String nickname, Currency currency, long cents);

    QueryUserResult addCurrency(UUID uuid, Currency currency, long cents);

    QueryUserResult addCurrency(String nickname, Currency currency, long cents);

    QueryUserResult getCurrency(String nickname, Currency currency);

    QueryUserResult removeCurrency(UUID uuid, Currency currency, long cents);

    QueryUserResult removeCurrency(String nickname, Currency currency, long cents);

    QueryUserResult withdrawCurrency(UUID uuid, Currency currency, long cents);

    QueryUserResult withdrawCurrency(String nickname, Currency currency, long cents);

    User findOrCreateAndUpdate(UUID uuid, String nickname);

    List<RankingUser> findTop(Currency currency, int page, int pageSize);

    List<RankingUser> findTopSeek(Currency currency, Long lastAmount, LocalDateTime lastLogin, UUID lastUuid, int pageSize);

    List<RankingUser> findTopSeekBackward(Currency currency, Long firstAmount, LocalDateTime firstLogin, UUID firstUuid, int pageSize);

    QueryUserResult addCurrencyLowLevel(UUID uuid, Currency currency, long cents, DatabaseExecutor executor, Connection connection) throws SQLException;

    QueryUserResult withdrawCurrencyLowLevel(UUID uuid, Currency currency, long cents, DatabaseExecutor executor, Connection connection) throws SQLException;

    QueryUserResult updateCurrenciesLowLevel(UUID senderUuid, UUID receiverUuid, Currency currency, long cents, DatabaseExecutor executor, Connection connection) throws SQLException;

}
