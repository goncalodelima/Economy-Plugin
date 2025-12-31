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

package pt.gongas.economy.platforms.bukkit.view;

import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.ResolvableProfile;
import io.papermc.paper.datacomponent.item.TooltipDisplay;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import pt.gongas.economy.platforms.bukkit.BukkitEconomyPlugin;
import pt.gongas.economy.platforms.bukkit.util.config.Configuration;
import pt.gongas.economy.shared.currency.Currency;
import pt.gongas.economy.shared.user.RankingUser;
import pt.gongas.economy.shared.user.service.UserFoundationService;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public class RankingView implements Listener {

    private final UserFoundationService userService;

    private final int usersPerPage;

    private final int size;

    private final String title;

    private final String itemName;

    private final List<String> itemLore;

    private final int backSlot;

    private final String backName;

    private final List<String> backLore;

    private final int nextSlot;

    private final String nextName;

    private final List<String> nextLore;

    private final int seek;

    public RankingView(Configuration inventory, UserFoundationService userService) {
        this.userService = userService;
        this.usersPerPage = inventory.getInt("ranking.usersPerPage", 45);
        this.size = inventory.getInt("ranking.size", 54);
        this.title = inventory.getString("ranking.title", "Most <currency> (Page <page>)");
        this.itemName = inventory.getString("ranking.item.name", "<color:#4D5DD7><playerName>");
        this.itemLore = inventory.getStringList("ranking.item.lore");
        this.backSlot = inventory.getInt("ranking.back.slot", 45);
        this.backName = inventory.getString("ranking.back.name", "<red>Back");
        this.backLore = inventory.getStringList("ranking.back.lore");
        this.nextSlot = inventory.getInt("ranking.next.slot", 53);
        this.nextName = inventory.getString("ranking.next.name", "<green>Next");
        this.nextLore = inventory.getStringList("ranking.next.lore");
        this.seek = inventory.getInt("ranking.seek", 100);
    }

    public void open(Player player, Currency currency) {

        GuiHolder gui = new GuiHolder(currency, size, title);
        Inventory inventory = gui.getInventory();

        player.openInventory(inventory);

        // compute items
        userService.getTop(currency, 1).thenAcceptAsync(list -> {

            if (!inventory.getViewers().contains(player)) {
                return;
            }

            if (list == null || list.isEmpty()) {
                player.closeInventory();
                return;
            }

            boolean hasNextPage = populateInventoryPage(inventory, currency, list, 1);

            if (hasNextPage) {
                setPageAndDisplayNextItem(gui, inventory, currency, 1);
            }

        }, Bukkit.getScheduler().getMainThreadExecutor(BukkitEconomyPlugin.plugin));

    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {

        if (!(event.getInventory().getHolder() instanceof GuiHolder gui)) {
            return;
        }

        event.setCancelled(true);

        // gui is currently loading a page
        if (gui.isLoading()) {
            return;
        }

        ItemStack clickedItem = event.getCurrentItem();

        // verify current item is not back/next page
        if (clickedItem == null || clickedItem.getType() != Material.ARROW) {
            return;
        }

        if (gui.isNextPage() && event.getRawSlot() == nextSlot) {

            Inventory inventory = gui.getInventory();

            gui.setLoading(true);

            int nextPage = gui.getCurrentPage() + 1;

            for (int i = 0; i < usersPerPage; i++) {
                inventory.setItem(i, null);
            }

            Currency currency = gui.getCurrency();
            RankingUser lastCursor = gui.getLastCursor();

            if (lastCursor == null || nextPage < seek) {
                fetchNextTop(userService.getTop(currency, nextPage), currency, event, inventory, gui, nextPage);
            } else {
                fetchNextTop(userService.getTopSeek(currency, lastCursor.cents(), lastCursor.lastLoginDate(), lastCursor.uuid()), currency, event, inventory, gui, nextPage);
            }

            return;
        }

        if (gui.isBackPage() && event.getRawSlot() == backSlot) {

            Inventory inventory = gui.getInventory();

            gui.setLoading(true);

            int previousPage = gui.getCurrentPage() - 1;

            for (int i = 0; i < usersPerPage; i++) {
                inventory.setItem(i, null);
            }

            Currency currency = gui.getCurrency();
            RankingUser previousCursor = gui.getPreviousCursor();

            if (previousCursor == null || previousPage < seek) {
                fetchPreviousTop(userService.getTop(currency, previousPage), currency, event, inventory, gui, previousPage);
            } else {
                fetchPreviousTop(userService.getTopSeekBackward(currency, previousCursor.cents(), previousCursor.lastLoginDate(), previousCursor.uuid()), currency, event, inventory, gui, previousPage);
            }

        }

    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {

        if (event.getInventory().getHolder() instanceof GuiHolder) {
            event.setCancelled(true);
        }

    }

    private void fetchNextTop(CompletableFuture<List<RankingUser>> future, Currency currency, InventoryClickEvent event, Inventory inventory, GuiHolder gui, int nextPage) {
        future.thenAcceptAsync(list -> {

            Player player = (Player) event.getWhoClicked();

            if (!inventory.getViewers().contains(player)) {
                return;
            }

            if (list == null || list.isEmpty()) { // keep on the same page
                inventory.setItem(nextSlot, null);
                gui.setNextPage(false);
                return;
            }

            boolean hasNextPage = populateInventoryPage(inventory, currency, list, nextPage);

            RankingUser firstInPage = list.getFirst();
            RankingUser lastInPage = list.get(Math.min(usersPerPage - 1, list.size() - 1));

            gui.setPreviousCursor(firstInPage);
            gui.setLastCursor(lastInPage);

            gui.setCurrentPage(nextPage);
            setTitle(player.getOpenInventory(), currency, nextPage);

            setPageAndDisplayBackItem(gui, inventory, currency, nextPage);

            if (hasNextPage) {
                setPageAndDisplayNextItem(gui, inventory, currency, nextPage);
            } else {
                inventory.setItem(nextSlot, null);
                gui.setNextPage(false);
            }

            gui.setLoading(false);

        }, Bukkit.getScheduler().getMainThreadExecutor(BukkitEconomyPlugin.plugin));
    }

    private void fetchPreviousTop(CompletableFuture<List<RankingUser>> future, Currency currency, InventoryClickEvent event, Inventory inventory, GuiHolder gui, int previousPage) {

        future.thenAcceptAsync(list -> {

            Player player = (Player) event.getWhoClicked();

            if (!inventory.getViewers().contains(player)) {
                return;
            }

            if (list == null || list.isEmpty()) { // keep on the same page

                inventory.setItem(backSlot, null);
                gui.setBackPage(false);

                int newPage = 1;

                gui.setCurrentPage(newPage);
                setTitle(player.getOpenInventory(), currency, newPage);
                return;
            }

            int newPreviousPage = populateInventoryPageOnBack(inventory, currency, list, previousPage);

            if (newPreviousPage != 1) {
                setPageAndDisplayBackItem(gui, inventory, currency, newPreviousPage);
            } else {
                inventory.setItem(backSlot, null);
                gui.setBackPage(false);
            }

            RankingUser firstInPage = list.getFirst();
            RankingUser lastInPage = list.get(Math.min(usersPerPage - 1, list.size() - 1));

            gui.setPreviousCursor(firstInPage);
            gui.setLastCursor(lastInPage);

            gui.setCurrentPage(newPreviousPage);
            setTitle(player.getOpenInventory(), currency, newPreviousPage);

            setPageAndDisplayNextItem(gui, inventory, currency, newPreviousPage);

            gui.setLoading(false);

        }, Bukkit.getScheduler().getMainThreadExecutor(BukkitEconomyPlugin.plugin));

    }

    private void setPageAndDisplayBackItem(GuiHolder gui, Inventory inventory, Currency currency, int page) {

        gui.setBackPage(true);

        ItemStack back = new ItemStack(Material.ARROW);
        ItemMeta backMeta = back.getItemMeta();

        backMeta.displayName(MiniMessage.miniMessage().deserialize(backName,
                Placeholder.unparsed("currency", currency.name()),
                Placeholder.unparsed("page", String.valueOf(page)))
        );

        List<Component> lore = new ArrayList<>();

        for (String string : backLore) {
            lore.add(MiniMessage.miniMessage().deserialize(string,
                    Placeholder.unparsed("currency", currency.name()),
                    Placeholder.unparsed("page", String.valueOf(page))));
        }

        backMeta.lore(lore);

        back.setItemMeta(backMeta);

        inventory.setItem(backSlot, back);
    }

    private void setPageAndDisplayNextItem(GuiHolder gui, Inventory inventory, Currency currency, int page) {

        gui.setNextPage(true);

        ItemStack next = new ItemStack(Material.ARROW);
        ItemMeta nextMeta = next.getItemMeta();

        nextMeta.displayName(MiniMessage.miniMessage().deserialize(nextName,
                Placeholder.unparsed("currency", currency.name()),
                Placeholder.unparsed("page", String.valueOf(page)))
        );

        List<Component> lore = new ArrayList<>();

        for (String string : nextLore) {
            lore.add(MiniMessage.miniMessage().deserialize(string,
                    Placeholder.unparsed("currency", currency.name()),
                    Placeholder.unparsed("page", String.valueOf(page))));
        }

        nextMeta.lore(lore);

        next.setItemMeta(nextMeta);

        inventory.setItem(nextSlot, next);
    }

    private void setTitle(InventoryView view, Currency currency, int page) {

        Component component = MiniMessage.miniMessage().deserialize(title,
                Placeholder.unparsed("currency", currency.name()),
                Placeholder.unparsed("page", String.valueOf(page)));

        view.setTitle(MiniMessage.miniMessage().serialize(component));
    }

    private boolean populateInventoryPage(Inventory inventory, Currency currency, List<RankingUser> users, int page) {

        boolean hasNextPage = false;
        int slot = 0;

        long current = System.currentTimeMillis();

        for (RankingUser user : users) {

            if (slot == usersPerPage) {
                hasNextPage = true;
                break;
            }

            int position = (page - 1) * usersPerPage + slot + 1;
            inventory.setItem(slot, createUserItem(user, currency, position));
            slot++;
        }

        System.out.println(System.currentTimeMillis() - current + " ms");
        return hasNextPage;
    }

    private int populateInventoryPageOnBack(Inventory inventory, Currency currency, List<RankingUser> users, int page) {

        boolean hasPreviousPage = users.size() == usersPerPage + 1;
        int slot = 0;

        if (hasPreviousPage) {
            users.removeFirst(); // remove the richest player from the list
        } else if (users.size() < usersPerPage) {
            page = 1;
        }

        for (RankingUser user : users) {
            int position = (page - 1) * usersPerPage + slot + 1;
            inventory.setItem(slot, createUserItem(user, currency, position));
            slot++;
        }

        return page;
    }

    private ItemStack createUserItem(RankingUser user, Currency currency, int position) {

        ItemStack item = new ItemStack(Material.PLAYER_HEAD);
        ItemMeta meta = item.getItemMeta();

        List<Component> loreComponents = new ArrayList<>();

        for (String line : itemLore) {
            loreComponents.add(MiniMessage.miniMessage().deserialize(line,
                    Placeholder.unparsed("name", user.nickname()),
                    Placeholder.unparsed("currency", currency.name()),
                    Placeholder.unparsed("balance", getFormatted(user.cents())),
                    Placeholder.unparsed("position", String.valueOf(position)),
                    Placeholder.parsed("icon", currency.icon())
            ));
        }

        meta.customName(MiniMessage.miniMessage().deserialize(itemName,
                Placeholder.unparsed("name", user.nickname()),
                Placeholder.unparsed("currency", currency.name()),
                Placeholder.unparsed("balance", getFormatted(user.cents())),
                Placeholder.unparsed("position", String.valueOf(position)),
                Placeholder.parsed("icon", currency.icon())
        ));

        meta.lore(loreComponents);
        item.setItemMeta(meta);

        item.setData(DataComponentTypes.PROFILE, ResolvableProfile.resolvableProfile().name(user.nickname()).build());
        item.setData(DataComponentTypes.TOOLTIP_DISPLAY, TooltipDisplay.tooltipDisplay().addHiddenComponents(DataComponentTypes.PROFILE));

        return item;
    }

    private String getFormatted(double amount) {
        return BukkitEconomyPlugin.plugin.formatter.formatNumber(amount / 100D);
    }

}
