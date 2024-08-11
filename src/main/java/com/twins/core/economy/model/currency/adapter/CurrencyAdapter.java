package com.twins.core.economy.model.currency.adapter;

import com.twins.core.economy.model.currency.Currency;
import com.twins.core.utils.ConfigurationAdapter;
import org.bukkit.configuration.ConfigurationSection;

public class CurrencyAdapter implements ConfigurationAdapter<Currency> {

    @Override
    public Currency adapt(ConfigurationSection section) {
        return new Currency(section.getString("name"), section.getString("command"), section.getString("icon").replace("&", "§"));
    }

}
