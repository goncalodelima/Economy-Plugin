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

package pt.gongas.economy.shared.user.adapter;

import pt.gongas.database.adapter.DatabaseAdapter;
import pt.gongas.database.executor.DatabaseQuery;
import pt.gongas.economy.shared.user.RankingUser;
import pt.gongas.economy.shared.util.UUIDConverter;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.UUID;

public class RankingAdapter implements DatabaseAdapter<RankingUser> {

    private final

    @Override
    public RankingUser adapt(DatabaseQuery databaseQuery) throws SQLException {

        UUID uuid = UUIDConverter.convert((byte[]) databaseQuery.get("uuid"));
        String nickname = (String) databaseQuery.get("nickname");
        long cents = (long) databaseQuery.get("cents");
        LocalDateTime lastLoginDate = (LocalDateTime) databaseQuery.get("last_login_date");

        return new RankingUser(uuid, nickname, cents, lastLoginDate);
    }

}
