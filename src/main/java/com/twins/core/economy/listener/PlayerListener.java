package com.twins.core.economy.listener;

import com.twins.core.economy.model.user.service.UserFoundationService;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerListener implements Listener {

    private final UserFoundationService userService;

    public PlayerListener(UserFoundationService userService) {
        this.userService = userService;
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event){
        Player player = event.getPlayer();
        userService.remove(player.getName());
    }

}
