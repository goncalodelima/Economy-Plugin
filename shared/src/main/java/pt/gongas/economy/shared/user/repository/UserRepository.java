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

import pt.gongas.database.Database;
import pt.gongas.database.executor.DatabaseExecutor;
import pt.gongas.economy.shared.currency.Currency;
import pt.gongas.economy.shared.currency.service.CurrencyFoundationService;
import pt.gongas.economy.shared.user.*;
import pt.gongas.economy.shared.user.adapter.RankingAdapter;
import pt.gongas.economy.shared.user.adapter.UserAdapter;
import pt.gongas.economy.shared.util.Pair;
import pt.gongas.economy.shared.util.UUIDConverter;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UserRepository implements UserFoundationRepository {

    private final Logger logger;

    private final Database database;

    private final CurrencyFoundationService currencyService;

    private final UserAdapter userAdapter;

    private final RankingAdapter rankingAdapter;

    public UserRepository(Logger logger, Database database, CurrencyFoundationService currencyService, UserAdapter userAdapter, RankingAdapter rankingAdapter) {
        this.logger = logger;
        this.database = database;
        this.currencyService = currencyService;
        this.userAdapter = userAdapter;
        this.rankingAdapter = rankingAdapter;
    }

    @Override
    public void setup() {

        try (DatabaseExecutor executor = database.execute(); Connection connection = executor.getHikariConnection().getConnection()) {

            executor.query("""
                            CREATE TABLE IF NOT EXISTS user_account (
                                uuid BINARY(16) NOT NULL,
                                nickname VARCHAR(16) NOT NULL,
                                last_login_date DATETIME NOT NULL,
                                PRIMARY KEY (uuid)
                            );
                            """)
                    .write(connection);

            executor.query("""
                            CREATE TABLE IF NOT EXISTS user_economy (
                                uuid BINARY(16) NOT NULL,
                                currency VARCHAR(36) NOT NULL,
                                cents BIGINT NOT NULL,
                                PRIMARY KEY (uuid, currency),
                                CONSTRAINT fk_user_economy_account
                                    FOREIGN KEY (uuid)
                                    REFERENCES user_account(uuid)
                                    ON DELETE CASCADE
                            );
                            """)
                    .write(connection);

            executor.query("""
                            CREATE INDEX IF NOT EXISTS idx_currency_amount
                            ON user_economy (currency, cents DESC, uuid)
                            """)
                    .write(connection);

            executor.query("""
                            CREATE INDEX IF NOT EXISTS idx_uuid_lastlogin
                            ON user_account (uuid, last_login_date DESC)
                            """)
                    .write(connection);

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error creating tables", e);
        }

    }

    @Override
    public QueryUserResult updateCurrencies(UUID senderUuid, UUID receiverUuid, Currency currency, long cents) {

        try (DatabaseExecutor executor = database.execute(); Connection connection = executor.getHikariConnection().getConnection()) {

            String currencyName = currency.name().toLowerCase();

            executor.startTransaction(connection);

            int updated = executor
                    .query("UPDATE user_economy SET cents = cents - ? WHERE uuid = ? AND currency = ? AND cents >= ?")
                    .writeAndReturnRowCount(statement -> {

                        try {
                            statement.set(1, cents);
                            statement.set(2, UUIDConverter.convert(senderUuid));
                            statement.set(3, currencyName);
                            statement.set(4, cents);
                        } catch (SQLException e) {
                            throw new RuntimeException(e);
                        }

                    }, connection);

            if (updated == 0) {
                executor.rollbackTransaction(connection);
                return new QueryUserResult.Error(ErrorType.NOT_ENOUGH_BALANCE);
            }

            int updated1 = executor.query("UPDATE user_economy SET cents = cents + ? WHERE uuid = ? and currency = ?")
                    .writeAndReturnRowCount(statement -> {

                        try {
                            statement.set(1, cents);
                            statement.set(2, UUIDConverter.convert(senderUuid));
                            statement.set(3, currencyName);
                        } catch (SQLException e) {
                            throw new RuntimeException(e);
                        }

                    }, connection);

            if (updated1 == 0) {
                executor.rollbackTransaction(connection);
                return new QueryUserResult.Error(ErrorType.NOT_FOUND);
            }

            executor.commitTransaction(connection);
            return new QueryUserResult.SuccessNoData();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public QueryUserResult updateCurrencies(UUID senderUuid, String receiverNickname, Currency currency, long cents) {

        try (DatabaseExecutor executor = database.execute(); Connection connection = executor.getHikariConnection().getConnection()) {

            String currencyName = currency.name().toLowerCase();

            executor.startTransaction(connection);

            Pair<byte[], String> pair = executor.query("SELECT uuid,nickname FROM user_account WHERE nickname = ? ORDER BY last_login_date DESC LIMIT 1")
                    .readOne(statement -> {

                        try {
                            statement.set(1, receiverNickname);
                        } catch (SQLException e) {
                            throw new RuntimeException(e);
                        }

                    }, query -> {
                        byte[] uuid = (byte[]) query.get("uuid");
                        String nickname = (String) query.get("nickname");
                        return new Pair<>(uuid, nickname);
                    }, connection).orElse(null);

            if (pair == null) {
                executor.rollbackTransaction(connection);
                return new QueryUserResult.Error(ErrorType.NOT_FOUND);
            }

            int updated = executor.query("UPDATE user_economy SET cents = cents - ? WHERE uuid = ? AND currency = ? AND cents >= ?")
                    .writeAndReturnRowCount(statement -> {

                        try {
                            statement.set(1, cents);
                            statement.set(2, UUIDConverter.convert(senderUuid));
                            statement.set(3, currencyName);
                            statement.set(4, cents);
                        } catch (SQLException e) {
                            throw new RuntimeException(e);
                        }

                    }, connection);

            if (updated == 0) {
                executor.rollbackTransaction(connection);
                return new QueryUserResult.Error(ErrorType.NOT_ENOUGH_BALANCE);
            }

            executor.query("INSERT INTO user_economy (uuid, currency, cents) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE cents = cents + ?")
                    .write(statement -> {

                        try {
                            statement.set(1, pair.key());
                            statement.set(2, currencyName);
                            statement.set(3, cents);
                            statement.set(4, cents);
                        } catch (SQLException e) {
                            throw new RuntimeException(e);
                        }

                    }, connection);

            executor.commitTransaction(connection);
            return new QueryUserResult.Success(UUIDConverter.convert(pair.key()), pair.value(), 0);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public Map<UUID, Map<Currency, Long>> fetchPlayerBalances(Collection<UUID> uuids) {

        Map<UUID, Map<Currency, Long>> result = new HashMap<>();

        try (DatabaseExecutor executor = database.execute()) {

            StringBuilder placeholders = new StringBuilder();

            for (int i = 0; i < uuids.size(); i++) {

                placeholders.append("?");

                if (i < uuids.size() - 1) {
                    placeholders.append(",");
                }

            }

            executor.query("SELECT uuid, currency, cents FROM user_economy WHERE uuid IN (" + placeholders + ")")
                    .readMany(statement -> {

                        int index = 1;

                        for (UUID uuid : uuids) {
                            try {
                                statement.set(index++, UUIDConverter.convert(uuid));
                            } catch (SQLException e) {
                                throw new RuntimeException(e);
                            }
                        }

                    }, query -> {

                        byte[] uuidBytes = (byte[]) query.get("uuid");
                        String currencyName = ((String) query.get("currency")).toLowerCase();
                        long cents = (Long) query.get("cents");

                        UUID uuid = UUIDConverter.convert(uuidBytes);
                        Currency currency = currencyService.get(currencyName);

                        if (currency == null) {
                            return null;
                        }

                        result.computeIfAbsent(uuid, k -> new HashMap<>()).put(currency, cents);
                        return null;
                    });

            return result;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public QueryUserResult setCurrency(UUID uuid, Currency currency, long cents) {

        try (DatabaseExecutor executor = database.execute()) {

            int rows = executor.query("UPDATE user_economy SET cents = ? WHERE uuid = ? AND currency = ?")
                    .writeAndReturnRowCount(statement -> {

                        try {
                            statement.set(1, cents);
                            statement.set(2, UUIDConverter.convert(uuid));
                            statement.set(3, currency.name().toLowerCase());
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }

                    });

            if (rows == 0) {
                return new QueryUserResult.Error(ErrorType.NOT_FOUND);
            }

            return new QueryUserResult.SuccessNoData();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public QueryUserResult setCurrency(String nickname, Currency currency, long cents) {

        try (DatabaseExecutor executor = database.execute(); Connection connection = executor.getHikariConnection().getConnection()) {

            Pair<byte[], String> pair = executor.query("SELECT uuid,nickname FROM user_account WHERE nickname = ? ORDER BY last_login_date DESC LIMIT 1")
                    .readOne(statement -> {

                        try {
                            statement.set(1, nickname);
                        } catch (SQLException e) {
                            throw new RuntimeException(e);
                        }

                    }, query -> {
                        byte[] uuid = (byte[]) query.get("uuid");
                        String name = (String) query.get("nickname");
                        return new Pair<>(uuid, name);
                    }, connection).orElse(null);

            if (pair == null) {
                return new QueryUserResult.Error(ErrorType.NOT_FOUND);
            }


            int rows = executor.query("UPDATE user_economy SET cents = ? WHERE uuid = ? AND currency = ?")
                    .writeAndReturnRowCount(statement -> {

                        try {
                            statement.set(1, cents);
                            statement.set(2, pair.key());
                            statement.set(3, currency.name().toLowerCase());
                        } catch (SQLException e) {
                            throw new RuntimeException(e);
                        }

                    }, connection);

            if (rows == 0) {
                return new QueryUserResult.Error(ErrorType.NOT_FOUND);
            }

            return new QueryUserResult.Success(UUIDConverter.convert(pair.key()), pair.value(), cents);

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public QueryUserResult addCurrency(UUID uuid, Currency currency, long cents) {

        try (DatabaseExecutor executor = database.execute()) {

            int rows = executor.query("UPDATE user_economy SET cents = cents + ? WHERE uuid = ? AND currency = ?")
                    .writeAndReturnRowCount(statement -> {

                        try {
                            statement.set(1, cents);
                            statement.set(2, UUIDConverter.convert(uuid));
                            statement.set(3, currency.name().toLowerCase());
                        } catch (SQLException e) {
                            throw new RuntimeException(e);
                        }

                    });

            if (rows == 0) {
                return new QueryUserResult.Error(ErrorType.NOT_FOUND);
            }

            return new QueryUserResult.SuccessNoData();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public QueryUserResult addCurrency(String nickname, Currency currency, long cents) {

        try (DatabaseExecutor executor = database.execute(); Connection connection = executor.getHikariConnection().getConnection()) {

            Pair<byte[], String> pair = executor.query("SELECT uuid,nickname FROM user_account WHERE nickname = ? ORDER BY last_login_date DESC LIMIT 1")
                    .readOne(statement -> {

                        try {
                            statement.set(1, nickname);
                        } catch (SQLException e) {
                            throw new RuntimeException(e);
                        }

                    }, query -> {
                        byte[] uuid = (byte[]) query.get("uuid");
                        String name = (String) query.get("nickname");
                        return new Pair<>(uuid, name);
                    }, connection).orElse(null);

            if (pair == null) {
                return new QueryUserResult.Error(ErrorType.NOT_FOUND);
            }

            int rows = executor.query("UPDATE user_economy SET cents = cents + ? WHERE uuid = ? AND currency = ?")
                    .writeAndReturnRowCount(statement -> {

                        try {
                            statement.set(1, cents);
                            statement.set(2, pair.key());
                            statement.set(3, currency.name().toLowerCase());
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }

                    }, connection);

            if (rows == 0) {
                return new QueryUserResult.Error(ErrorType.NOT_FOUND);
            }

            return new QueryUserResult.Success(UUIDConverter.convert(pair.key()), pair.value(), cents);

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public QueryUserResult getCurrency(String nickname, Currency currency) {

        try (DatabaseExecutor executor = database.execute()) {

            Optional<QueryUserResult> result = executor.query("SELECT ua.nickname, ue.cents FROM user_account ua LEFT JOIN user_economy ue ON ua.uuid = ue.uuid AND ue.currency = ? WHERE ua.nickname = ? ORDER BY ua.last_login_date DESC LIMIT 1")
                    .readOne(statement -> {

                        try {
                            statement.set(1, currency.name().toLowerCase());
                            statement.set(2, nickname);
                        } catch (SQLException e) {
                            throw new RuntimeException(e);
                        }

                    }, query -> new QueryUserResult.Success(null, (String) query.get("nickname"), (long) query.get("cents")));

            return result.orElse(new QueryUserResult.Error(ErrorType.NOT_FOUND));

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public QueryUserResult removeCurrency(UUID uuid, Currency currency, long cents) {

        try (DatabaseExecutor executor = database.execute()) {

            int rows = executor.query("UPDATE user_economy SET cents = cents - LEAST(cents, ?) WHERE uuid = ? AND currency = ?")
                    .writeAndReturnRowCount(statement -> {

                        try {
                            statement.set(1, cents);
                            statement.set(2, UUIDConverter.convert(uuid));
                            statement.set(3, currency.name().toLowerCase());
                        } catch (SQLException e) {
                            throw new RuntimeException(e);
                        }

                    });

            if (rows == 0) {
                return new QueryUserResult.Error(ErrorType.NOT_FOUND);
            }

            return new QueryUserResult.SuccessNoData();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public QueryUserResult removeCurrency(String nickname, Currency currency, long cents) {

        try (DatabaseExecutor executor = database.execute(); Connection connection = executor.getHikariConnection().getConnection()) {

            Pair<byte[], String> pair = executor.query("SELECT uuid,nickname FROM user_account WHERE nickname = ? ORDER BY last_login_date DESC LIMIT 1")
                    .readOne(statement -> {

                        try {
                            statement.set(1, nickname);
                        } catch (SQLException e) {
                            throw new RuntimeException(e);
                        }

                    }, query -> {
                        byte[] uuid = (byte[]) query.get("uuid");
                        String name = (String) query.get("nickname");
                        return new Pair<>(uuid, name);
                    }, connection).orElse(null);

            if (pair == null) {
                return new QueryUserResult.Error(ErrorType.NOT_FOUND);
            }

            int rows = executor.query("UPDATE user_economy SET cents = cents - LEAST(cents, ?) WHERE uuid = ? AND currency = ?")
                    .writeAndReturnRowCount(statement -> {

                        try {
                            statement.set(1, cents);
                            statement.set(2, pair.key());
                            statement.set(3, currency.name().toLowerCase());
                        } catch (SQLException e) {
                            throw new RuntimeException(e);
                        }

                    }, connection);

            if (rows == 0) {
                return new QueryUserResult.Error(ErrorType.NOT_FOUND);
            }

            return new QueryUserResult.Success(UUIDConverter.convert(pair.key()), pair.value(), cents);

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public QueryUserResult withdrawCurrency(UUID uuid, Currency currency, long cents) {

        try (DatabaseExecutor executor = database.execute(); Connection connection = executor.getHikariConnection().getConnection()) {

            String currencyName = currency.name().toLowerCase();

            executor.startTransaction(connection);

            long databaseCents = executor.query("SELECT cents FROM user_economy WHERE uuid = ? AND currency = ? FOR UPDATE")
                    .readOne(statement -> {

                                try {
                                    statement.set(1, UUIDConverter.convert(uuid));
                                    statement.set(2, currencyName);
                                } catch (SQLException e) {
                                    throw new RuntimeException(e);
                                }

                            },
                            query -> (long) query.get("cents"), connection)
                    .orElse(0L);

            if (databaseCents < cents) {
                executor.rollbackTransaction(connection);
                return new QueryUserResult.Error(ErrorType.NOT_ENOUGH_BALANCE);
            }

            // The `cents >= ?` check here is not strictly necessary because we already perform
            // a SELECT ... FOR UPDATE within a transaction (auto-commit = false),
            // which locks the row until the transaction either commits, rolls back,
            // or the connection is returned to the HikariCP pool (Hikari will automatically roll back
            // any uncommitted transaction when the connection is returned, releasing the lock).
            int rows = executor.query("UPDATE user_economy SET cents = cents - ? WHERE uuid = ? AND currency = ?")
                    .writeAndReturnRowCount(statement -> {

                        try {
                            statement.set(1, cents);
                            statement.set(2, UUIDConverter.convert(uuid));
                            statement.set(3, currencyName);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }

                    }, connection);

            if (rows == 0) {
                executor.rollbackTransaction(connection);
                return new QueryUserResult.Error(ErrorType.NOT_FOUND);
            }

            executor.commitTransaction(connection);
            return new QueryUserResult.SuccessNoData();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public QueryUserResult withdrawCurrency(String nickname, Currency currency, long cents) {

        try (DatabaseExecutor executor = database.execute(); Connection connection = executor.getHikariConnection().getConnection()) {

            String currencyName = currency.name().toLowerCase();

            executor.startTransaction(connection);

            Pair<byte[], String> pair = executor.query("SELECT uuid,nickname FROM user_account WHERE nickname = ? ORDER BY last_login_date DESC LIMIT 1")
                    .readOne(statement -> {

                        try {
                            statement.set(1, nickname);
                        } catch (SQLException e) {
                            throw new RuntimeException(e);
                        }

                    }, query -> {
                        byte[] uuid = (byte[]) query.get("uuid");
                        String name = (String) query.get("nickname");
                        return new Pair<>(uuid, name);
                    }, connection).orElse(null);

            if (pair == null) {
                executor.rollbackTransaction(connection);
                return new QueryUserResult.Error(ErrorType.NOT_FOUND);
            }

            long databaseCents = executor.query("SELECT cents FROM user_economy WHERE uuid = ? AND currency = ? FOR UPDATE")
                    .readOne(statement -> {

                                try {
                                    statement.set(1, pair.key());
                                    statement.set(2, currencyName);
                                } catch (SQLException e) {
                                    throw new RuntimeException(e);
                                }

                            },
                            query -> (long) query.get("cents"), connection)
                    .orElse(0L);

            if (databaseCents < cents) {
                executor.rollbackTransaction(connection);
                return new QueryUserResult.Error(ErrorType.NOT_ENOUGH_BALANCE);
            }

            // The `cents >= ?` check here is not strictly necessary because we already perform
            // a SELECT ... FOR UPDATE within a transaction (auto-commit = false),
            // which locks the row until the transaction either commits, rolls back,
            // or the connection is returned to the HikariCP pool (Hikari will automatically roll back
            // any uncommitted transaction when the connection is returned, releasing the lock).
            int rows = executor.query("UPDATE user_economy SET cents = cents - ? WHERE uuid = ? AND currency = ?")
                    .writeAndReturnRowCount(statement -> {

                        try {
                            statement.set(1, cents);
                            statement.set(2, pair.key());
                            statement.set(3, currencyName);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }

                    }, connection);

            if (rows == 0) {
                executor.rollbackTransaction(connection);
                return new QueryUserResult.Error(ErrorType.NOT_FOUND);
            }

            executor.commitTransaction(connection);
            return new QueryUserResult.Success(UUIDConverter.convert(pair.key()), pair.value(), cents);

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public User findOrCreateAndUpdate(UUID uuid, String nickname) {

        try (DatabaseExecutor executor = database.execute(); Connection connection = executor.getHikariConnection().getConnection()) {

            byte[] uuidBytes = UUIDConverter.convert(uuid);

            executor.query("""
                            INSERT INTO user_account (uuid, nickname, last_login_date)
                            VALUES (?, ?, ?)
                            ON DUPLICATE KEY UPDATE
                                nickname = VALUES(nickname),
                                last_login_date = VALUES(last_login_date)
                            """)
                    .write(statement -> {

                        try {
                            statement.set(1, uuidBytes);
                            statement.set(2, nickname);
                            statement.set(3, LocalDateTime.now());
                        } catch (SQLException e) {
                            throw new RuntimeException(e);
                        }

                    }, connection);

            StringBuilder insertCurrencies = new StringBuilder("INSERT IGNORE INTO user_economy (uuid, currency, cents) VALUES");

            Set<Currency> currencies = currencyService.getAll();

            for (int i = 0; i < currencies.size(); i++) {

                insertCurrencies.append("(?, ?, 0)");

                if (i < currencies.size() - 1) {
                    insertCurrencies.append(", ");
                }

            }

            executor.query(insertCurrencies.toString()).write(statement -> {

                int index = 1;

                for (Currency currency : currencies) {
                    try {
                        statement.set(index++, uuidBytes);
                        statement.set(index++, currency.name().toLowerCase());
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                }

            }, connection);

            return executor.query("""
                            SELECT ua.uuid, ua.nickname, ua.last_login_date,
                            ue.currency, ue.cents
                            FROM user_account ua
                            LEFT JOIN user_economy ue ON ua.uuid = ue.uuid
                            WHERE ua.uuid = ?
                            """)
                    .readOne(statement -> {

                        try {
                            statement.set(1, uuidBytes);
                        } catch (SQLException e) {
                            throw new RuntimeException(e);
                        }

                    }, this.userAdapter, connection).orElse(null);

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to retrieve economy user data", e);
            return null;
        }

    }

    @Override
    public List<RankingUser> findTop(Currency currency, int page) {

        int pageSize = 45;
        int offset = (page - 1) * pageSize;

        try (DatabaseExecutor executor = database.execute()) {
            return executor.query("""
                            SELECT ue.*, ua.nickname, ua.last_login_date
                            FROM user_economy ue
                            JOIN user_account ua ON ue.uuid = ua.uuid
                            WHERE currency = ?
                            ORDER BY ue.cents DESC, ua.last_login_date DESC LIMIT ? OFFSET ?
                            """)
                    .readMany(statement -> {

                        try {
                            statement.set(1, currency.name().toLowerCase());
                            statement.set(2, pageSize + 1); // this is for the inventory to know if there is a next page or not
                            statement.set(3, offset);
                        } catch (SQLException e) {
                            throw new RuntimeException(e);
                        }

                    }, this.rankingAdapter, ArrayList::new);

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public List<RankingUser> findTopSeek(Currency currency, Long lastAmount, LocalDateTime lastLogin, UUID lastUuid) {

        int pageSize = 45;

        try (DatabaseExecutor executor = database.execute()) {
            return executor.query("""
                    SELECT ue.*, ua.nickname, ua.last_login_date
                    FROM user_economy ue
                    JOIN user_account ua ON ue.uuid = ua.uuid
                    WHERE ue.currency = ?
                    AND (ue.cents < ? OR (ue.cents = ? AND ua.last_login_date < ?) OR (ue.cents = ? AND ua.last_login_date = ? AND ua.uuid < ?))
                    ORDER BY ue.cents DESC, ua.last_login_date DESC, ua.uuid DESC LIMIT ?
                    """
            ).readMany(statement -> {

                try {

                    statement.set(1, currency.name().toLowerCase());
                    statement.set(2, lastAmount);
                    statement.set(3, lastAmount);
                    statement.set(4, lastLogin);
                    statement.set(5, lastAmount);
                    statement.set(6, lastLogin);
                    statement.set(7, UUIDConverter.convert(lastUuid));
                    statement.set(8, pageSize + 1); // this is for the inventory to know if there is a next page or not

                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }

            }, this.rankingAdapter, ArrayList::new);

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public List<RankingUser> findTopSeekBackward(Currency currency, Long firstAmount, LocalDateTime firstLogin, UUID firstUuid) {

        int pageSize = 45;

        try (DatabaseExecutor executor = database.execute()) {
            return executor.query("""
                    SELECT ue.*, ua.nickname, ua.last_login_date
                    FROM user_economy ue
                    JOIN user_account ua ON ue.uuid = ua.uuid
                    WHERE ue.currency = ?
                    AND (ue.cents > ? OR (ue.cents = ? AND ua.last_login_date > ?) OR (ue.cents = ? AND ua.last_login_date = ? AND ua.uuid > ?))
                    ORDER BY ue.cents ASC, ua.last_login_date ASC, ua.uuid ASC LIMIT ?
                    """
            ).readMany(statement -> {

                try {

                    statement.set(1, currency.name().toLowerCase());
                    statement.set(2, firstAmount);
                    statement.set(3, firstAmount);
                    statement.set(4, firstLogin);
                    statement.set(5, firstAmount);
                    statement.set(6, firstLogin);
                    statement.set(7, UUIDConverter.convert(firstUuid));
                    statement.set(8, pageSize + 1); // this is for the inventory to know if there is a previous page or not

                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }

            }, this.rankingAdapter, ArrayList::new);

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

}
