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

package pt.gongas.economy.shared.user.adapter;

import pt.gongas.database.adapter.DatabaseAdapter;
import pt.gongas.database.executor.DatabaseQuery;
import pt.gongas.economy.shared.util.UUIDConverter;
import pt.gongas.economy.shared.currency.Currency;
import pt.gongas.economy.shared.currency.service.CurrencyFoundationService;
import pt.gongas.economy.shared.user.User;

import java.sql.SQLException;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class UserAdapter implements DatabaseAdapter<User> {

    private final CurrencyFoundationService currencyService;

    public UserAdapter(CurrencyFoundationService currencyService) {
        this.currencyService = currencyService;
    }

    @Override
    public User adapt(DatabaseQuery query) throws SQLException {

        UUID uuid = UUIDConverter.convert(query.getBytes("uuid"));
        String nickname = query.getString("nickname");
        ConcurrentMap<Currency, Long> currencies = new ConcurrentHashMap<>();

        do {

            Currency currency = currencyService.get(query.getString("currency").toLowerCase());

            if (currency != null) {
                currencies.put(currency, query.getLong("cents"));
            }

        } while (query.next());

        return new User(uuid, nickname, currencies);
    }

}
