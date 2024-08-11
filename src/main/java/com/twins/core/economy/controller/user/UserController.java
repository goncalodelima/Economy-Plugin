package com.twins.core.economy.controller.user;

import com.twins.core.CorePlugin;
import com.twins.core.economy.model.currency.Currency;
import com.twins.core.economy.model.currency.service.CurrencyFoundationService;
import com.twins.core.economy.model.user.User;
import com.twins.core.economy.model.user.service.UserFoundationService;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserController {

    private final Map<Currency, List<User>> ranking = new HashMap<>();

    public UserController(CurrencyFoundationService currencyService, UserFoundationService userService) {

        new BukkitRunnable() {
            @Override
            public void run() {
                currencyService.getAll().forEach(currency -> ranking.put(currency, userService.getTop(currency)));
            }
        }.runTaskTimerAsynchronously(CorePlugin.INSTANCE, 0, 20 * 60 * 10);

    }

    public List<User> getRanking(Currency currency){
        return ranking.getOrDefault(currency, Collections.emptyList());
    }

}