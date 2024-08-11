package com.twins.core.economy.model.currency.service;

import com.twins.core.economy.model.currency.Currency;

import java.util.List;
import java.util.Optional;

public interface CurrencyFoundationService {

    void put(Currency currency);

    Optional<Currency> get(String name);

    List<Currency> getAll();

}
