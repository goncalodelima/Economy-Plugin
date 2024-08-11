package com.twins.core.economy.model.user.service;

import com.twins.core.economy.model.currency.Currency;
import com.twins.core.economy.model.user.User;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface UserFoundationService {

    void put(User user);

    void update(User user);

    CompletableFuture<Boolean> updateCurrencies(String senderNickname, String receiverNickname, Currency currency, double amount);

    CompletableFuture<Boolean> incrementCurrency(String nickname, Currency currency, double amount);

    CompletableFuture<Boolean> decrementCurrency(String nickname, Currency currency, double amount);

    void remove(String nickname);

    CompletableFuture<User> get(String nickname);

    List<User> getTop(Currency currency);

}