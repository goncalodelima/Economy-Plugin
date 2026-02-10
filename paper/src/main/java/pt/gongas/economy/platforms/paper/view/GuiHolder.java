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

package pt.gongas.economy.platforms.paper.view;

import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pt.gongas.economy.shared.currency.Currency;
import pt.gongas.economy.shared.user.RankingUser;

public class GuiHolder implements InventoryHolder {

    @NotNull
    private final Inventory inventory;

    @NotNull
    private final Currency currency;

    private int currentPage = 1;

    private boolean backPage;

    private boolean nextPage;

    private boolean loading;

    @Nullable
    private RankingUser lastCursor;

    @Nullable
    private RankingUser previousCursor;

    public GuiHolder(Currency currency, int size, String title) {
        this.inventory = Bukkit.createInventory(this, size, MiniMessage.miniMessage().deserialize(title,
                Placeholder.unparsed("currency", currency.name()),
                Placeholder.unparsed("page", "1"))
        );
        this.currency = currency;
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }

    public @NotNull Currency getCurrency() {
        return currency;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }

    public boolean isBackPage() {
        return backPage;
    }

    public void setBackPage(boolean backPage) {
        this.backPage = backPage;
    }

    public boolean isNextPage() {
        return nextPage;
    }

    public void setNextPage(boolean nextPage) {
        this.nextPage = nextPage;
    }

    public boolean isLoading() {
        return loading;
    }

    public void setLoading(boolean loading) {
        this.loading = loading;
    }

    public @Nullable RankingUser getLastCursor() {
        return lastCursor;
    }

    public void setLastCursor(@NotNull RankingUser lastCursor) {
        this.lastCursor = lastCursor;
    }

    public @Nullable RankingUser getPreviousCursor() {
        return previousCursor;
    }

    public void setPreviousCursor(@NotNull RankingUser previousCursor) {
        this.previousCursor = previousCursor;
    }

}
