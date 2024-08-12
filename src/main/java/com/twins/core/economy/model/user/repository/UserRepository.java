package com.twins.core.economy.model.user.repository;

import com.minecraftsolutions.database.Database;
import com.minecraftsolutions.database.executor.DatabaseExecutor;
import com.twins.core.CorePlugin;
import com.twins.core.economy.model.currency.Currency;
import com.twins.core.economy.model.user.User;
import com.twins.core.economy.model.user.adapter.RankingAdapter;
import com.twins.core.economy.model.user.adapter.UserAdapter;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

public class UserRepository implements UserFoundationRepository {

    private final Database database;

    private final UserAdapter userAdapter;

    private final RankingAdapter rankingAdapter;

    public UserRepository(Database database, UserAdapter userAdapter, RankingAdapter rankingAdapter) {
        this.database = database;
        this.userAdapter = userAdapter;
        this.rankingAdapter = rankingAdapter;
    }

    @Override
    public void setup() {
        try (DatabaseExecutor executor = database.execute()) {
            executor
                    .query("CREATE TABLE IF NOT EXISTS user_economy (nickname VARCHAR(16), currency VARCHAR(36), amount DOUBLE, PRIMARY KEY(nickname, currency))")
                    .write();
        }
    }

    @Override
    public void insertOrUpdate(User user) {

        Map<Currency, Double> currencies = user.currencies();

        try (DatabaseExecutor executor = database.execute()) {
            executor.query("INSERT INTO user_economy (nickname, currency, amount) VALUES(?,?,?) ON DUPLICATE KEY UPDATE amount = VALUES(amount)")
                    .batch(currencies.entrySet(), (entry, statement) -> {
                        statement.set(1, user.nickname());
                        statement.set(2, entry.getKey().name().toUpperCase());
                        statement.set(3, entry.getValue());
                    });
        }

    }

    public CompletableFuture<Boolean> updateCurrencies(String senderNickname, String receiverNickname, Currency currency, double amount) {
        return CompletableFuture.supplyAsync(() -> {

            DatabaseExecutor executor = null;

            try {

                executor = database.execute();
                executor.startTransaction();

                executor.query("UPDATE user_economy SET amount = amount - ? WHERE nickname = ? AND currency = ? AND amount >= ?")
                        .write(statement -> {
                            statement.set(1, amount);
                            statement.set(2, senderNickname);
                            statement.set(3, currency.name().toUpperCase());
                            statement.set(4, amount);
                        });

                executor.query("UPDATE user_economy SET amount = amount + ? WHERE nickname = ? AND currency = ?")
                        .write(statement -> {
                            statement.set(1, amount);
                            statement.set(2, receiverNickname);
                            statement.set(3, currency.name().toUpperCase());
                        });

                executor.commitTransaction();
                return true;

            } catch (SQLException e) {

                try {
                    executor.rollbackTransaction();
                } catch (SQLException ex) {
                    CorePlugin.INSTANCE.getLogger().log(Level.SEVERE, e.getMessage());
                }

                CorePlugin.INSTANCE.getLogger().log(Level.SEVERE, e.getMessage());
                return false;
            } finally {
                if (executor != null) {
                    executor.close();
                }
            }

        }).exceptionally(e -> {
            CorePlugin.INSTANCE.getLogger().log(Level.SEVERE, "Failed to update both currencies data", e);
            return false;
        });

    }

    public CompletableFuture<Boolean> incrementCurrency(String nickname, Currency currency, double amount) {
        return CompletableFuture.supplyAsync(() -> {
            try (DatabaseExecutor executor = database.execute()) {

                executor.query("UPDATE user_economy SET amount = amount + ? WHERE nickname = ? AND currency = ?")
                        .write(statement -> {
                            statement.set(1, amount);
                            statement.set(2, nickname);
                            statement.set(3, currency.name().toUpperCase());
                        });

                return true;

            }
        }).exceptionally(e -> {
            CorePlugin.INSTANCE.getLogger().log(Level.SEVERE, "Failed to increment currency data", e);
            return false;
        });
    }

    public CompletableFuture<Boolean> decrementCurrency(String nickname, Currency currency, double amount) {
        return CompletableFuture.supplyAsync(() -> {
            try (DatabaseExecutor executor = database.execute()) {

                executor.query("UPDATE user_economy SET amount = amount - ? WHERE nickname = ? AND currency = ?")
                        .write(statement -> {
                            statement.set(1, amount);
                            statement.set(2, nickname);
                            statement.set(3, currency.name().toUpperCase());
                        });

                return true;

            }
        }).exceptionally(e -> {
            CorePlugin.INSTANCE.getLogger().log(Level.SEVERE, "Failed to decrement currency data", e);
            return false;
        });
    }

    @Override
    public User findOne(String nickname) {
        try (DatabaseExecutor executor = database.execute()) {
            return executor
                    .query("SELECT * FROM user_economy WHERE nickname = ?")
                    .readOne(statement -> statement.set(1, nickname), this.userAdapter).orElse(null);
        }
    }

    @Override
    public List<User> findTop(Currency currency) {
        try (DatabaseExecutor executor = database.execute()) {
            return executor
                    .query("SELECT * FROM user_economy WHERE currency = ? ORDER BY amount DESC LIMIT 10")
                    .readMany(statement -> statement.set(1, currency.name().toUpperCase()), this.rankingAdapter, ArrayList::new);
        }
    }

}
