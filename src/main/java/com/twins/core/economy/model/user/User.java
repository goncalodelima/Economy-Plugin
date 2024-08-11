package com.twins.core.economy.model.user;

import com.twins.core.CorePlugin;
import com.twins.core.economy.model.currency.Currency;

import java.util.Map;
import java.util.Objects;

public record User(String nickname, Map<Currency, Double> currencies) {

    public double get(Currency currency) {
        return this.currencies.getOrDefault(currency, 0.0);
    }

    public String getFormatted(Currency currency) {
        return CorePlugin.INSTANCE.getFormatter().formatNumber(this.currencies.getOrDefault(currency, 0.0));
    }

    public void set(Currency currency, double amount) {
        this.currencies.put(currency, amount);
    }

    public void add(Currency currency, double amount) {
        set(currency, this.currencies.getOrDefault(currency, 0.0) + amount);
    }

    public void remove(Currency currency, double amount) {
        set(currency, this.currencies.getOrDefault(currency, 0.0) - amount);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User user)) return false;
        return Objects.equals(nickname, user.nickname) && Objects.equals(currencies, user.currencies);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nickname, currencies);
    }

    @Override
    public String toString() {
        return "User{" +
                "nickname='" + nickname + '\'' +
                ", currencies=" + currencies +
                '}';
    }

}
