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

package pt.gongas.economy.platforms.bukkit.runnable;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import pt.gongas.economy.platforms.bukkit.BukkitEconomyPlugin;
import pt.gongas.economy.shared.currency.Currency;
import pt.gongas.economy.shared.user.User;
import pt.gongas.economy.shared.user.service.UserFoundationService;

import java.util.*;

public class PlayerBalanceRunnable extends BukkitRunnable {

    private final UserFoundationService userService;

    private final Set<UUID> uuidsToUpdate;

    private static final int UPDATE_INTERVAL = 60 * 60;

    private int i = 0;

    public PlayerBalanceRunnable(UserFoundationService userService, Set<UUID> uuidsToUpdate) {
        this.userService = userService;
        this.uuidsToUpdate = uuidsToUpdate;
    }

    @Override
    public void run() {

        if (i++ == UPDATE_INTERVAL) { // Every hour, the cache is forcibly updated from the database for all players.

            for (Player player : Bukkit.getOnlinePlayers()) {
                uuidsToUpdate.add(player.getUniqueId());
            }

            i = 0;
        }

        if (uuidsToUpdate.isEmpty()) {
            return;
        }

        Set<UUID> snapshot = new HashSet<>(uuidsToUpdate);

        userService.fetchPlayerBalances(snapshot).thenAcceptAsync(map -> {

            if (map != null) {

                for (Map.Entry<UUID, Map<Currency, Long>> entry : map.entrySet()) {

                    UUID uuid = entry.getKey();
                    User user = userService.get(uuid);

                    if (user != null) {

                        Map<Currency, Long> currencies = entry.getValue();

                        for (Map.Entry<Currency, Long> currencyEntry : currencies.entrySet()) {
                            user.set(currencyEntry.getKey(), currencyEntry.getValue());
                        }

                    }

                }

            } else {
                uuidsToUpdate.addAll(snapshot); // The reason I'm doing this is below
            }

        }, Bukkit.getScheduler().getMainThreadExecutor(BukkitEconomyPlugin.plugin));

        // I add the snapshot elements back to the set in case of an error,
        // instead of simply removing the snapshot elements if there is no error,
        // to ensure that the snapshot size is not empty if the set is not empty.
        // Furthermore, it's to avoid updating the same UUIDs on the next runnable call.
        uuidsToUpdate.removeAll(snapshot);
    }

}
