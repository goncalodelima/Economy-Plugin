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

package pt.gongas.economy.platforms.paper.listener;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import pt.gongas.economy.platforms.paper.PaperEconomyPlugin;
import pt.gongas.economy.platforms.paper.lang.LangMessages;
import pt.gongas.economy.shared.user.User;
import pt.gongas.economy.shared.user.service.UserFoundationService;

public class PlayerListener implements Listener {

    private final LangMessages messages;

    private final UserFoundationService userService;

    public PlayerListener(LangMessages messages, UserFoundationService userService) {
        this.messages = messages;
        this.userService = userService;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {

        Player player = event.getPlayer();

        userService.getOrCreateDataAndUpdate(player.getUniqueId(), player.getName())
                .thenAcceptAsync(user -> {

                    if (!player.isConnected()) {
                        return;
                    }

                    if (user == null) {
                        // kick the player
                        player.kick(MiniMessage.miniMessage().deserialize(messages.error));
                        return;
                    }

                    userService.put(user);
                    user.setOnline(true);

                }, Bukkit.getScheduler().getMainThreadExecutor(PaperEconomyPlugin.plugin));

    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {

        User user = userService.remove(event.getPlayer().getUniqueId());

        if (user != null) {
            user.setOnline(false);
        }

    }

}
