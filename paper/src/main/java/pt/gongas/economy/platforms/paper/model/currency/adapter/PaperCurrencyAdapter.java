/*
 *
 *  * This file is part of Economy-Plugin - https://github.com/goncalodelima/Economy-Plugin
 *  * Copyright (c) 2026 goncalodelima and contributors
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

package pt.gongas.economy.platforms.paper.model.currency.adapter;

import org.bukkit.configuration.ConfigurationSection;
import pt.gongas.economy.shared.currency.Currency;
import pt.gongas.economy.shared.util.config.ConfigurationAdapter;

import java.util.Objects;

public class PaperCurrencyAdapter implements ConfigurationAdapter<Currency, ConfigurationSection> {

    @Override
    public Currency adapt(ConfigurationSection section) {
        return new Currency(Objects.requireNonNull(section.getString("name")), Objects.requireNonNull(section.getString("command")), Objects.requireNonNull(section.getString("icon")));
    }

}
