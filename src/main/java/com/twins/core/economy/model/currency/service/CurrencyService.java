package com.twins.core.economy.model.currency.service;

import com.twins.core.economy.model.currency.Currency;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class CurrencyService implements CurrencyFoundationService{

    private final Map<String, Currency> cache = new HashMap<>();

    @Override
    public void put(Currency currency) {
        this.cache.put(currency.name().toUpperCase(), currency);
    }

    @Override
    public Optional<Currency> get(String name) {
        return Optional.ofNullable(this.cache.get(name.toUpperCase()));
    }

    @Override
    public List<Currency> getAll() {
        return this.cache
                .keySet()
                .stream()
                .map(this::get)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

}
