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

package pt.gongas.economy.platforms.bukkit.listener;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import pt.gongas.economy.platforms.bukkit.util.config.Configuration;
import pt.gongas.economy.shared.user.User;
import pt.gongas.economy.shared.user.service.UserFoundationService;

public class PlayerListener implements Listener {

    private final Configuration lang;

    private final UserFoundationService userService;

    public PlayerListener(Configuration lang, UserFoundationService userService) {
        this.lang = lang;
        this.userService = userService;
    }

    @EventHandler
    public void onAsyncPlayerLogin(AsyncPlayerPreLoginEvent event) {

        User user = userService.getOrCreateDataAndUpdate(event.getUniqueId(), event.getName());

        if (user == null) {
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, MiniMessage.miniMessage().deserialize(lang.getString("error3", "<red>Something unexpected happened. Please try again.")));
            return;
        }

        userService.put(user);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {

        User user = userService.get(event.getPlayer().getUniqueId());

        if (user != null) {
            user.setOnline(true);
        }

    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {

        User user = userService.remove(event.getPlayer().getUniqueId());

        if (user != null) {
            user.setOnline(false);
        }

    }

}
