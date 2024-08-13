package com.twins.core.economy.model.user.service;

import com.minecraftsolutions.database.Database;
import com.twins.core.CorePlugin;
import com.twins.core.economy.EconomyPlugin;
import com.twins.core.economy.model.currency.Currency;
import com.twins.core.economy.model.user.User;
import com.twins.core.economy.model.user.adapter.RankingAdapter;
import com.twins.core.economy.model.user.adapter.UserAdapter;
import com.twins.core.economy.model.user.repository.UserFoundationRepository;
import com.twins.core.economy.model.user.repository.UserRepository;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

public class UserService implements UserFoundationService {

    private final Map<String, User> cache = new ConcurrentHashMap<>();

    private final Set<String> temporaryCache = new HashSet<>();

    private final UserFoundationRepository userRepository;

    public UserService(EconomyPlugin plugin, Database database) {
        this.userRepository = new UserRepository(database, new UserAdapter(plugin.getCurrencyService()), new RankingAdapter(plugin.getCurrencyService()));
        this.userRepository.setup();
    }

    @Override
    public void update(User user) {
        userRepository.insertOrUpdate(user);
    }

    @Override
    public CompletableFuture<Boolean> updateCurrencies(String senderNickname, String receiverNickname, Currency currency, double amount) {
        return userRepository.updateCurrencies(senderNickname, receiverNickname, currency, amount);
    }

    @Override
    public CompletableFuture<Boolean> setCurrency(String nickname, Currency currency, double amount) {
        return userRepository.setCurrency(nickname, currency, amount);
    }

    @Override
    public void remove(String nickname) {
        cache.remove(nickname);
    }

    @Override
    public void addTemporaryCache(String nickname) {
        temporaryCache.add(nickname);
    }

    @Override
    public void removeTemporaryCache(String nickname) {
        temporaryCache.remove(nickname);
    }

    @Override
    public boolean containsTemporaryCache(String nickname) {
        return temporaryCache.contains(nickname);
    }

    @Override
    public CompletableFuture<User> get(String nickname) {

        User user = cache.get(nickname);

        if (user != null) {
            return CompletableFuture.completedFuture(user);
        }

        return CompletableFuture.supplyAsync(() -> {

                    User userRepositoryOne = userRepository.findOne(nickname);

                    if (userRepositoryOne != null) {
                        cache.put(userRepositoryOne.nickname(), userRepositoryOne);
                        return userRepositoryOne;
                    } else {
                        User newUser = new User(nickname, new HashMap<>());
                        cache.put(newUser.nickname(), newUser);
                        return newUser;
                    }

                }, CorePlugin.INSTANCE.getAsyncExecutor())
                .exceptionally(e -> {
                    CorePlugin.INSTANCE.getLogger().log(Level.SEVERE, "Failed to retrieve economy user data", e);
                    User newUser = new User(nickname, new HashMap<>());
                    cache.put(newUser.nickname(), newUser);
                    return newUser;
                });

    }

    @Override
    public CompletableFuture<List<User>> getTop(Currency currency) {
        return CompletableFuture.supplyAsync(() -> userRepository.findTop(currency), CorePlugin.INSTANCE.getAsyncExecutor())
                .exceptionally(e -> {
                    CorePlugin.INSTANCE.getLogger().log(Level.SEVERE, "Failed to retrieve all economy users data", e);
                    return null;
                });
    }

}
