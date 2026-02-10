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

package pt.gongas.economy.shared.user;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import pt.gongas.economy.shared.currency.Currency;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class User {

    @NotNull
    private final UUID uuid;
    @NotNull
    private String nickname;
    @NotNull
    private final Map<Currency, Long> currencies;
    private boolean online;

    public User(@NotNull UUID uuid, @NotNull String nickname) {
        this.uuid = uuid;
        this.nickname = nickname;
        this.currencies = new HashMap<>();
    }

    public User(@NotNull UUID uuid, @NotNull String nickname, @NotNull Map<Currency, Long> currencies) {
        this.uuid = uuid;
        this.nickname = nickname;
        this.currencies = currencies;
    }

    public long get(Currency currency) {
        return currencies.getOrDefault(currency, 0L);
    }

    public void set(Currency currency, long cents) {
        currencies.put(currency, cents);
    }

    public void add(Currency currency, long cents) {
        set(currency, currencies.getOrDefault(currency, 0L) + cents);
    }

    public void remove(Currency currency, long cents) {
        set(currency, currencies.getOrDefault(currency, 0L) - cents);
    }

    public @NotNull UUID getUuid() {
        return uuid;
    }

    public @NotNull String getNickname() {
        return nickname;
    }

    public void setNickname(@NotNull String nickname) {
        this.nickname = nickname;
    }

    public @NotNull Map<Currency, Long> getCurrencies() {
        return currencies;
    }

    public boolean isOnline() {
        return online;
    }

    public @ApiStatus.Internal void setOnline(boolean online) {
        this.online = online;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User user)) return false;
        return Objects.equals(uuid, user.uuid);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(uuid);
    }

}