/*
 *
 *  * This file is part of Economy-Plugin - https://github.com/goncalodelima/Economy-Plugin
 *  * Copyright (c) 2025 goncalodelima and contributors
 *  *
 *  * This program is free software: you can redistribute it and/or modify
 *  * it under the terms of the GNU General Public License as published by
 *  * the Free Software Foundation, either version 3 of the License, or
 *  * (at your option) any later version.
 *  *
 *  * This program is distributed in the hope that it will be useful,
 *  * but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  * GNU General Public License for more details.
 *  *
 *  * You should have received a copy of the GNU General Public License
 *  * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 *
 */

package pt.gongas.economy.platforms.bukkit.model.currency.loader;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import pt.gongas.economy.shared.currency.Currency;
import pt.gongas.economy.platforms.bukkit.model.currency.adapter.BukkitCurrencyAdapter;
import pt.gongas.economy.shared.currency.loader.CurrencyLoader;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class BukkitCurrencyLoader implements CurrencyLoader {

    private final FileConfiguration config;

    private final BukkitCurrencyAdapter adapter = new BukkitCurrencyAdapter();

    public BukkitCurrencyLoader(FileConfiguration config) {
        this.config = config;
    }

    public List<Currency> setup() {

        ConfigurationSection section = config.getConfigurationSection("");

        if (section == null) {
            return List.of();
        }

        return section.getKeys(false)
                .stream()
                .map(key -> this.adapter.adapt(Objects.requireNonNull(section.getConfigurationSection(key))))
                .collect(Collectors.toList());
    }

}
