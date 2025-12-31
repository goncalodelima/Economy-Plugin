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

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pt.gongas.database.Database;
import pt.gongas.economy.shared.currency.Currency;
import pt.gongas.economy.shared.currency.service.CurrencyFoundationService;
import pt.gongas.economy.shared.user.QueryUserResult;
import pt.gongas.economy.shared.user.RankingUser;
import pt.gongas.economy.shared.user.User;
import pt.gongas.economy.shared.user.adapter.RankingAdapter;
import pt.gongas.economy.shared.user.adapter.UserAdapter;
import pt.gongas.economy.shared.user.repository.UserFoundationRepository;
import pt.gongas.economy.shared.util.Result;
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
        this.userRepository = new UserRepository(logger, databaseExecutor, database, currencyService, new UserAdapter(currencyService), new RankingAdapter());
        this.userRepository.setup();
    }

    @Override
    public CompletableFuture<Result<Boolean>> updateCurrencies(UUID senderUuid, UUID receiverUuid, Currency currency, long cents) {
        return userRepository.updateCurrencies(senderUuid, receiverUuid, currency, cents);
    }

    @Override
    public CompletableFuture<QueryUserResult> updateCurrencies(UUID senderUuid, String receiverNickname, Currency currency, long cents) {
        return userRepository.updateCurrencies(senderUuid, receiverNickname, currency, cents);
    }

    @Override
    public CompletableFuture<Map<UUID, Map<Currency, Long>>> fetchPlayerBalances(Collection<UUID> uuids) {
        return userRepository.fetchPlayerBalances(uuids);
    }

    @Override
    public CompletableFuture<Result<Boolean>> setCurrency(UUID uuid, Currency currency, long cents) {
        return userRepository.setCurrency(uuid, currency, cents);
    }

    @Override
    public CompletableFuture<QueryUserResult> setCurrency(String nickname, Currency currency, long cents) {
        return userRepository.setCurrency(nickname, currency, cents);
    }

    @Override
    public CompletableFuture<Result<Boolean>> addCurrency(UUID uuid, Currency currency, long cents) {
        return userRepository.addCurrency(uuid, currency, cents);
    }

    @Override
    public CompletableFuture<QueryUserResult> addCurrency(String nickname, Currency currency, long cents) {
        return userRepository.addCurrency(nickname, currency, cents);
    }

    @Override
    public CompletableFuture<Result<Boolean>> removeCurrency(UUID uuid, Currency currency, long cents) {
        return userRepository.removeCurrency(uuid, currency, cents);
    }

    @Override
    public CompletableFuture<QueryUserResult> removeCurrency(String nickname, Currency currency, long cents) {
        return userRepository.removeCurrency(nickname, currency, cents);
    }

    @Override
    public User remove(UUID uuid) {
        return cache.remove(uuid);
    }

    @Override
    public void put(User user) {
        cache.put(user.getUuid(), user);
    }

    @Override
    @Nullable
    public User get(@NotNull UUID uuid) {
        return cache.get(uuid);
    }

    @Override
    public User getOrCreateDataAndUpdate(UUID uuid, String nickname) {

        Result<User> result = userRepository.findOrCreateAndUpdate(uuid, nickname);

        if (result.success()) {
            User userRepositoryOne = result.value();
            return Objects.requireNonNullElseGet(userRepositoryOne, () -> new User(uuid, nickname));
        }

        return null;
    }

    @Override
    public CompletableFuture<QueryUserResult> getCurrency(String nickname, Currency currency) {
        return userRepository.getCurrency(nickname, currency);
    }

    @Override
    public CompletableFuture<List<RankingUser>> getTop(Currency currency, int page) {
        return CompletableFuture.supplyAsync(() -> userRepository.findTop(currency, page), databaseExecutor)
                .exceptionally(e -> {
                    logger.log(Level.SEVERE, "Failed to retrieve top users for page " + page + " and currency " + currency.name(), e);
                    return null;
                });
    }

    @Override
    public CompletableFuture<List<RankingUser>> getTopSeek(Currency currency, Long lastAmount, LocalDateTime lastLogin, UUID lastUuid) {
        return CompletableFuture.supplyAsync(() -> userRepository.findTopSeek(currency, lastAmount, lastLogin, lastUuid), databaseExecutor)
                .exceptionally(e -> {
                    logger.log(Level.SEVERE, "Failed to retrieve top users using seek pagination for currency " + currency.name() + " (lastAmount=" + lastAmount + ", lastLogin=" + lastLogin + ")", e);
                    return null;
                });
    }

    @Override
    public CompletableFuture<List<RankingUser>> getTopSeekBackward(Currency currency, Long firstAmount, LocalDateTime firstLogin, UUID firstUuid) {
        return CompletableFuture.supplyAsync(() -> {
                    List<RankingUser> reversed = userRepository.findTopSeekBackward(currency, firstAmount, firstLogin, firstUuid);
                    Collections.reverse(reversed);
                    return reversed;
                }, databaseExecutor)
                .exceptionally(e -> {
                    logger.log(Level.SEVERE, "Failed to retrieve top users using seek pagination for currency " + currency.name() + " (firstAmount=" + firstAmount + ")", e);
                    return null;
                });
    }

}
