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

import org.jetbrains.annotations.NotNull;
import pt.gongas.database.Database;
import pt.gongas.database.executor.DatabaseExecutor;
import pt.gongas.economy.shared.user.QueryUserResult;
import pt.gongas.economy.shared.user.service.UserFoundationService;

import java.sql.Connection;
import java.sql.SQLException;

public class EconomyTransactionManager implements EconomyTransactionalApi {

    @NotNull
    private final UserFoundationService userService;

    public EconomyTransactionManager(@NotNull UserFoundationService userService) {
        this.userService = userService;
    }

    @Override
    public boolean executeInEconomyTransaction(@NotNull Database database, @NotNull EconomyTransactionAction action) throws SQLException {

        try (DatabaseExecutor executor = database.execute();
             Connection connection = executor.getHikariConnection().getConnection()) {

            executor.startTransaction(connection);

            QueryUserResult result;

            try {

                result = action.execute(userService, executor, connection);

                if (result instanceof QueryUserResult.Error) {
                    executor.rollbackTransaction(connection);
                    return false;
                }

                executor.commitTransaction(connection);
                return true;

            } catch (Exception exception) {
                executor.rollbackTransaction(connection);
                throw exception;
            }

        }

    }

}
