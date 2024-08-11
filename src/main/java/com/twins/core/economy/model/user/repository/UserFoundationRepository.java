package com.twins.core.economy.model.user.repository;

import com.twins.core.economy.model.currency.Currency;
import com.twins.core.economy.model.user.User;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface UserFoundationRepository {

    void setup();

    void insertOrUpdate(User user);

    CompletableFuture<Boolean> updateCurrencies(String senderNickname, String receiverNickname, Currency currency, double amount);

    CompletableFuture<Boolean> incrementCurrency(String nickname, Currency currency, double amount);

    CompletableFuture<Boolean> decrementCurrency(String nickname, Currency currency, double amount);

    User findOne(String nickname);

    List<User> findTop(Currency currency);

}
