package com.twins.core.economy.model.user.adapter;

import com.minecraftsolutions.database.adapter.DatabaseAdapter;
import com.minecraftsolutions.database.executor.DatabaseQuery;
import com.twins.core.economy.model.currency.Currency;
import com.twins.core.economy.model.currency.service.CurrencyFoundationService;
import com.twins.core.economy.model.user.User;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class UserAdapter implements DatabaseAdapter<User> {

    private final CurrencyFoundationService currencyService;

    public UserAdapter(CurrencyFoundationService currencyService) {
        this.currencyService = currencyService;
    }

    @Override
    public User adapt(DatabaseQuery databaseQuery) throws SQLException {

        final String nickname = (String) databaseQuery.get("nickname");
        final Map<Currency, Double> currencies = new HashMap<>();

        do {
            currencyService.get((String) databaseQuery.get("currency")).ifPresent(currency -> currencies.put(currency, (Double) databaseQuery.get("amount")));
        }while (databaseQuery.next());

        return new User(nickname, currencies);
    }

}