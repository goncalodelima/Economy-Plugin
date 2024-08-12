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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

public class UserService implements UserFoundationService {

    private final Map<String, User> cache = new ConcurrentHashMap<>();

    private final UserFoundationRepository userRepository;
    
    public UserService(EconomyPlugin plugin, Database database) {
        this.userRepository = new UserRepository(database, new UserAdapter(plugin.getCurrencyService()), new RankingAdapter(plugin.getCurrencyService()));
        this.userRepository.setup();
    }

    @Override
    public void put(User user) {
        userRepository.insertOrUpdate(user);
        cache.put(user.nickname(), user);
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
    public CompletableFuture<Boolean> incrementCurrency(String nickname, Currency currency, double amount) {
        return userRepository.incrementCurrency(nickname, currency, amount);
    }

    @Override
    public CompletableFuture<Boolean> decrementCurrency(String nickname, Currency currency, double amount) {
        return userRepository.decrementCurrency(nickname, currency, amount);
    }

    @Override
    public void remove(String nickname) {
        cache.remove(nickname);
    }

    @Override
    public CompletableFuture<User> get(String nickname) {

        User user = cache.get(nickname);

        if (user != null)
            return CompletableFuture.completedFuture(user);

        return CompletableFuture.supplyAsync(() -> {

            User userRepositoryOne = userRepository.findOne(nickname);

            if (userRepositoryOne != null) {
                cache.put(userRepositoryOne.nickname(), userRepositoryOne);
                return userRepositoryOne;
            } else {
                User newUser = new User(nickname, new HashMap<>());
                put(newUser);
                return newUser;
            }

        }).exceptionally(e -> {
            CorePlugin.INSTANCE.getLogger().log(Level.SEVERE, "Failed to retrieve global user data", e);
            User newUser = new User(nickname, new HashMap<>());
            put(newUser);
            return newUser;
        });

    }

    @Override
    public List<User> getTop(Currency currency) {
        return userRepository.findTop(currency);
    }

}
