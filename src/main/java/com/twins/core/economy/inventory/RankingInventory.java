package com.twins.core.economy.inventory;

import com.minecraftsolutions.utils.ItemBuilder;
import com.twins.core.CorePlugin;
import com.twins.core.economy.controller.user.UserController;
import com.twins.core.economy.model.currency.Currency;
import com.twins.core.economy.model.user.User;
import com.twins.core.global.GlobalPlugin;
import com.twins.core.utils.Configuration;
import me.devnatan.inventoryframework.View;
import me.devnatan.inventoryframework.ViewConfigBuilder;
import org.bukkit.Material;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class RankingInventory extends View {

    private final Configuration inventory;

    public RankingInventory(UserController userController, Configuration inventory) {

        this.inventory = inventory;

        computedPaginationState(context -> {

                    if (!(context.getInitialData() instanceof Currency currency)) {
                        return Collections.emptyList();
                        }

                    List<User> users = userController.getRanking(currency);

                    while (users.size() < 10) {
                        users.add(new User("nobody-" + users.size(), new HashMap<>()));
                    }

                    return users;
                }, (context, builder, index, value) -> {

                    if (!(context.getInitialData() instanceof Currency currency)) {
                        return;
                    }

                    GlobalPlugin.INSTANCE.getUserService().get(context.getPlayer().getName()).thenAcceptAsync(globalUser -> {

                        if (value.nickname().contains("nobody-")) {
                            builder.withItem(new ItemBuilder(Material.BARRIER).setDisplayName(inventory.getString(globalUser.getLanguageType(), "ranking.nobody").replace("&", "§")).build());
                            return;
                        }

                        builder.withItem(new ItemBuilder(Material.SKULL_ITEM, 1, (byte) 3)
                                .changeSkull(meta -> meta.setOwner(value.nickname()))
                                .setDisplayName(inventory.getString(globalUser.getLanguageType(), "ranking.item.name").replace("&", "§").replace("%position%", String.valueOf(index)).replace("%player%", value.nickname()).replace("%amount%", CorePlugin.INSTANCE.getFormatter().formatNumber(value.get(currency))).replace("%currency_lowercase%", currency.name().toLowerCase()).replace("%currency_uppercase%", currency.name().toUpperCase()))
                                .setLore(Collections.singletonList(inventory.getString(globalUser.getLanguageType(), "ranking.item.lore").replace("&", "§").replace("%position%", String.valueOf(index)).replace("%player%", value.nickname()).replace("%amount%", CorePlugin.INSTANCE.getFormatter().formatNumber(value.get(currency))).replace("%currency_lowercase%", currency.name().toLowerCase()).replace("%currency_uppercase%", currency.name().toUpperCase())))
                                .build());

                    }, CorePlugin.INSTANCE.getMainExecutor());

                }
        );

    }

    @Override
    public void onInit(ViewConfigBuilder config) {

        config
                .title(inventory.getString("PT.ranking.title").replace("&", "§"))
                .size(inventory.getInt("PT.ranking.size"))
                .layout(inventory.getStringList("PT.ranking.layout").toArray(new String[0]))
                .cancelOnClick()
                .cancelOnDrag()
                .cancelOnDrop()
                .cancelOnPickup()
                .build();

    }

}
