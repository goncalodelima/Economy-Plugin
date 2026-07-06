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

package pt.gongas.economy.platforms.paper.hook;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import pt.gongas.economy.platforms.paper.PaperEconomyPlugin;
import pt.gongas.economy.shared.currency.Currency;
import pt.gongas.economy.shared.currency.service.CurrencyFoundationService;
import pt.gongas.economy.shared.user.User;
import pt.gongas.economy.shared.user.service.UserFoundationService;

public class EconomyPlaceholderExpansion extends PlaceholderExpansion {

    private final CurrencyFoundationService currencyService;

    private final UserFoundationService userService;

    public EconomyPlaceholderExpansion(CurrencyFoundationService currencyService, UserFoundationService userService) {
        this.currencyService = currencyService;
        this.userService = userService;
    }

    @Override
    @NotNull
    public String getAuthor() {
        return "ReeachyZ";
    }

    @Override
    @NotNull
    public String getIdentifier() {
        return "economy-plugin";
    }

    @Override
    @NotNull
    public String getVersion() {
        return "1.3.1";
    }

    @Override
    public String onPlaceholderRequest(Player player, @NotNull String params) {

        String lowercase = params.toLowerCase();
        String prefix = "player_formatted_";

        if (lowercase.startsWith(prefix)) {

            User user = userService.get(player.getUniqueId());

            if (user == null) {
                return null;
            }

            String currencyName = params.substring(prefix.length());
            Currency currency = currencyService.get(currencyName);

            if (currency == null) {
                return "Invalid currency";
            }

            long cents = user.get(currency);
            double amount = cents / 100D;

            return getFormatted(amount);
        }

        return null;
    }

    private String getFormatted(double amount) {
        return PaperEconomyPlugin.plugin.formatter.formatNumber(amount);
    }

}
