package com.twins.core.economy.model.currency.loader;

import com.twins.core.economy.model.currency.Currency;
import com.twins.core.economy.model.currency.adapter.CurrencyAdapter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;
import java.util.stream.Collectors;

public class CurrencyLoader {

    private final FileConfiguration config;

    private final CurrencyAdapter adapter = new CurrencyAdapter();

    public CurrencyLoader(FileConfiguration config) {
        this.config = config;
    }

    public List<Currency> setup(){

        ConfigurationSection section = config.getConfigurationSection("");

        if (section == null)
            return List.of();

        return section.getKeys(false)
                .stream()
                .map(key -> this.adapter.adapt(section.getConfigurationSection(key)))
                .collect(Collectors.toList());
    }

}
