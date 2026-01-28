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
import pt.gongas.database.Database;
import pt.gongas.economy.shared.currency.Currency;
import pt.gongas.economy.shared.currency.service.CurrencyFoundationService;
import pt.gongas.economy.shared.user.ErrorType;
import pt.gongas.economy.shared.user.QueryUserResult;
import pt.gongas.economy.shared.user.RankingUser;
import pt.gongas.economy.shared.user.User;
import pt.gongas.economy.shared.user.adapter.RankingAdapter;
import pt.gongas.economy.shared.user.adapter.UserAdapter;
import pt.gongas.economy.shared.user.repository.UserFoundationRepository;
import pt.gongas.economy.shared.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UserService implements UserFoundationService {

    private final Map<UUID, User> cache = new ConcurrentHashMap<>();

    private final Logger logger;

    private final ExecutorService databaseExecutor;

    private final UserFoundationRepository userRepository;

    public UserService(Logger logger, ExecutorService databaseExecutor, CurrencyFoundationService currencyService, Database database) {
        this.logger = logger;
        this.databaseExecutor = databaseExecutor;
        this.userRepository = new UserRepository(logger, database, currencyService, new UserAdapter(currencyService), new RankingAdapter());
        this.userRepository.setup();
    }

    @Override
    public @NotNull CompletableFuture<QueryUserResult> updateCurrencies(@NotNull UUID senderUuid, @NotNull UUID receiverUuid, @NotNull Currency currency, long cents) {
        return CompletableFuture.supplyAsync(() -> userRepository.updateCurrencies(senderUuid, receiverUuid, currency, cents), databaseExecutor)
                .exceptionally(e -> {
                    logger.log(Level.SEVERE, "Failed to update both currencies data", e);
                    return new QueryUserResult.Error(ErrorType.EXCEPTION);
                });
    }

    @Override
    public @NotNull CompletableFuture<QueryUserResult> updateCurrencies(@NotNull UUID senderUuid, @NotNull String receiverNickname, @NotNull Currency currency, long cents) {
        return CompletableFuture.supplyAsync(() -> userRepository.updateCurrencies(senderUuid, receiverNickname, currency, cents), databaseExecutor)
                .exceptionally(e -> {
                    logger.log(Level.SEVERE, "Failed to update both currencies data", e);
                    return new QueryUserResult.Error(ErrorType.EXCEPTION);
                });
    }

    @Override
    public @NotNull CompletableFuture<Map<UUID, Map<Currency, Long>>> fetchPlayerBalances(@NotNull Collection<UUID> uuids) {
        return CompletableFuture.supplyAsync(() -> userRepository.fetchPlayerBalances(uuids), databaseExecutor).exceptionally(e -> {
            logger.log(Level.SEVERE, "Failed to fetch player balances", e);
            return null;
        });
    }

    @Override
    public @NotNull CompletableFuture<QueryUserResult> setCurrency(@NotNull UUID uuid, @NotNull Currency currency, long cents) {
        return CompletableFuture.supplyAsync(() -> userRepository.setCurrency(uuid, currency, cents), databaseExecutor).exceptionally(e -> {
            logger.log(Level.SEVERE, "Failed to set currency data", e);
            return new QueryUserResult.Error(ErrorType.EXCEPTION);
        });
    }

    @Override
    public @NotNull CompletableFuture<QueryUserResult> setCurrency(@NotNull String nickname, @NotNull Currency currency, long cents) {
        return CompletableFuture.supplyAsync(() -> userRepository.setCurrency(nickname, currency, cents), databaseExecutor).exceptionally(e -> {
            logger.log(Level.SEVERE, "Failed to set currency data", e);
            return new QueryUserResult.Error(ErrorType.EXCEPTION);
        });
    }

    @Override
    public @NotNull CompletableFuture<QueryUserResult> addCurrency(@NotNull UUID uuid, @NotNull Currency currency, long cents) {
        return CompletableFuture.supplyAsync(() -> userRepository.addCurrency(uuid, currency, cents), databaseExecutor).exceptionally(e -> {
            logger.log(Level.SEVERE, "Failed to add currency data", e);
            return new QueryUserResult.Error(ErrorType.EXCEPTION);
        });
    }

    @Override
    public @NotNull CompletableFuture<QueryUserResult> addCurrency(@NotNull String nickname, @NotNull Currency currency, long cents) {
        return CompletableFuture.supplyAsync(() -> userRepository.addCurrency(nickname, currency, cents), databaseExecutor).exceptionally(e -> {
            logger.log(Level.SEVERE, "Failed to add currency data", e);
            return new QueryUserResult.Error(ErrorType.EXCEPTION);
        });
    }

    @Override
    public @NotNull CompletableFuture<QueryUserResult> removeCurrency(@NotNull UUID uuid, @NotNull Currency currency, long cents) {
        return CompletableFuture.supplyAsync(() -> userRepository.removeCurrency(uuid, currency, cents), databaseExecutor).exceptionally(e -> {
            logger.log(Level.SEVERE, "Failed to remove currency data", e);
            return new QueryUserResult.Error(ErrorType.EXCEPTION);
        });
    }

    @Override
    public @NotNull CompletableFuture<QueryUserResult> removeCurrency(@NotNull String nickname, @NotNull Currency currency, long cents) {
        return CompletableFuture.supplyAsync(() -> userRepository.removeCurrency(nickname, currency, cents), databaseExecutor).exceptionally(e -> {
            logger.log(Level.SEVERE, "Failed to add currency data", e);
            return new QueryUserResult.Error(ErrorType.EXCEPTION);
        });
    }

    @Override
    public @NotNull CompletableFuture<QueryUserResult> withdrawCurrency(@NotNull UUID uuid, @NotNull Currency currency, long cents) {
        return CompletableFuture.supplyAsync(() -> userRepository.withdrawCurrency(uuid, currency, cents), databaseExecutor).exceptionally(e -> {
            logger.log(Level.SEVERE, "Failed to withdraw currency data", e);
            return new QueryUserResult.Error(ErrorType.EXCEPTION);
        });
    }

    @Override
    public @NotNull CompletableFuture<QueryUserResult> withdrawCurrency(@NotNull String nickname, @NotNull Currency currency, long cents) {
        return CompletableFuture.supplyAsync(() -> userRepository.withdrawCurrency(nickname, currency, cents), databaseExecutor).exceptionally(e -> {
            logger.log(Level.SEVERE, "Failed to withdraw currency data", e);
            return new QueryUserResult.Error(ErrorType.EXCEPTION);
        });
    }

    @Override
    @ApiStatus.Internal
    public User remove(@NotNull UUID uuid) {
        return cache.remove(uuid);
    }

    @Override
    @ApiStatus.Internal
    public void put(@NotNull User user) {
        cache.put(user.getUuid(), user);
    }

    @Override
    public @Nullable User get(@NotNull UUID uuid) {
        return cache.get(uuid);
    }

    @Override
    public @Nullable User getOrCreateDataAndUpdate(@NotNull UUID uuid, @NotNull String nickname) {
        return userRepository.findOrCreateAndUpdate(uuid, nickname);
    }

    @Override
    public @NotNull CompletableFuture<QueryUserResult> getCurrency(@NotNull String nickname, @NotNull Currency currency) {
        return CompletableFuture.supplyAsync(() -> userRepository.getCurrency(nickname, currency), databaseExecutor).exceptionally(e -> {
            logger.log(Level.SEVERE, "Failed to get currency for nickname: " + nickname, e);
            return new QueryUserResult.Error(ErrorType.EXCEPTION);
        });
    }

    @Override
    public @NotNull CompletableFuture<@Nullable List<RankingUser>> getTop(@NotNull Currency currency, int page, int pageSize) {
        return CompletableFuture.supplyAsync(() -> userRepository.findTop(currency, page, pageSize), databaseExecutor)
                .exceptionally(e -> {
                    logger.log(Level.SEVERE, "Failed to retrieve top users for page " + page + " and currency " + currency.name(), e);
                    return null;
                });
    }

    @Override
    public @NotNull CompletableFuture<@Nullable List<RankingUser>> getTopSeek(@NotNull Currency currency, @NotNull Long lastAmount, @NotNull LocalDateTime lastLogin, @NotNull UUID lastUuid, int pageSize) {
        return CompletableFuture.supplyAsync(() -> userRepository.findTopSeek(currency, lastAmount, lastLogin, lastUuid, pageSize), databaseExecutor)
                .exceptionally(e -> {
                    logger.log(Level.SEVERE, "Failed to retrieve top users using seek pagination for currency " + currency.name() + " (lastAmount=" + lastAmount + ", lastLogin=" + lastLogin + ")", e);
                    return null;
                });
    }

    @Override
    public @NotNull CompletableFuture<@Nullable List<RankingUser>> getTopSeekBackward(@NotNull Currency currency, @NotNull Long firstAmount, @NotNull LocalDateTime firstLogin, @NotNull UUID firstUuid, int pageSize) {
        return CompletableFuture.supplyAsync(() -> {
                    List<RankingUser> reversed = userRepository.findTopSeekBackward(currency, firstAmount, firstLogin, firstUuid, pageSize);
                    Collections.reverse(reversed);
                    return reversed;
                }, databaseExecutor)
                .exceptionally(e -> {
                    logger.log(Level.SEVERE, "Failed to retrieve top users using seek pagination for currency " + currency.name() + " (firstAmount=" + firstAmount + ")", e);
                    return null;
                });
    }

}
