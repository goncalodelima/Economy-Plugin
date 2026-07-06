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

package pt.gongas.economy.platforms.velocity;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Dependency;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ProxyServer;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bstats.velocity.Metrics;
import pt.gongas.economy.shared.messaging.EconomyMessage;
import pt.gongas.redis.redis.RedisManager;

@Plugin(id = "economy-plugin", name = "Economy Plugin", version = "1.3.1",
        url = "https://github.com/goncalodelima/Economy-Plugin", description = "Free Multi-economy plugin with multi-server support", authors = {"ReeachyZ_"},
        dependencies = {@Dependency(id = "redisplugin")})
public class VelocityEconomyPlugin {

    private final ProxyServer server;

    private final Metrics.Factory metricsFactory;

    private Metrics metrics;

    @Inject
    public VelocityEconomyPlugin(ProxyServer server, Metrics.Factory metricsFactory) {
        this.server = server;
        this.metricsFactory = metricsFactory;
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {

        RedisManager.getClient().getTopic("economy-plugin")
                .addListener(EconomyMessage.class, (channel, msg) ->
                        server.getPlayer(msg.getTarget()).ifPresent(player ->
                                player.sendMessage(MiniMessage.miniMessage().deserialize(msg.getMessage())))
                );

        // BStats Metrics
        metrics = metricsFactory.make(this, 28596);
    }

    @Subscribe
    public void onProxyShutdown(ProxyShutdownEvent event) {

        if (metrics != null) {
            metrics.shutdown();
        }

    }

}
