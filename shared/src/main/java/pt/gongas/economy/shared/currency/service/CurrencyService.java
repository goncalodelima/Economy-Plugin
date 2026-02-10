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

package pt.gongas.economy.shared.currency.service;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pt.gongas.economy.shared.currency.Currency;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class CurrencyService implements CurrencyFoundationService {

    private final Map<String, Currency> cache = new ConcurrentHashMap<>();

    @Override
    public void put(@NotNull Currency currency) {
        cache.put(currency.name().toLowerCase(), currency);
    }

    @Override
    @Nullable
    public Currency get(String name) {
        return cache.get(name.toLowerCase());
    }

    @Override
    public Set<Currency> getAll() {
        return new HashSet<>(cache.values());
    }

}
