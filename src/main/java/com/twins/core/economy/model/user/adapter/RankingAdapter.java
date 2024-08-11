package com.twins.core.economy.model.user.adapter;

import com.minecraftsolutions.database.adapter.DatabaseAdapter;
import com.minecraftsolutions.database.executor.DatabaseQuery;
import com.twins.core.economy.model.currency.Currency;
import com.twins.core.economy.model.currency.service.CurrencyFoundationService;
import com.twins.core.economy.model.user.User;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class RankingAdapter implements DatabaseAdapter<User> {

    private final CurrencyFoundationService currencyService;

    public RankingAdapter(CurrencyFoundationService currencyService) {
        this.currencyService = currencyService;
    }

    @Override
    public User adapt(DatabaseQuery databaseQuery) throws SQLException {

        String nickname = (String) databaseQuery.get("nickname");
        Map<Currency, Double> currencies = new HashMap<>();

        currencyService.get((String) databaseQuery.get("currency")).ifPresent(currency -> currencies.put(currency, (Double) databaseQuery.get("amount")));

        return new User(nickname, currencies);
    }

}
