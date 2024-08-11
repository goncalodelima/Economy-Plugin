package com.twins.core.economy;

import com.twins.core.CorePlugin;
import com.twins.core.economy.command.EconomyCommand;
import com.twins.core.economy.controller.user.UserController;
import com.twins.core.economy.inventory.RankingInventory;
import com.twins.core.economy.listener.PlayerListener;
import com.twins.core.economy.model.currency.loader.CurrencyLoader;
import com.twins.core.economy.model.currency.service.CurrencyFoundationService;
import com.twins.core.economy.model.currency.service.CurrencyService;
import com.twins.core.economy.model.user.service.UserFoundationService;
import com.twins.core.economy.model.user.service.UserService;
import com.twins.core.utils.CCommand;
import com.twins.core.utils.Configuration;
import me.devnatan.inventoryframework.ViewFrame;
import org.bukkit.Bukkit;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.plugin.SimplePluginManager;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.logging.Level;

public class EconomyPlugin {

    private SimpleCommandMap simplePluginManager;

    private Configuration messages;

    private Configuration economy;

    private Configuration inventory;

    private final CurrencyFoundationService currencyService;

    private final UserFoundationService userService;

    private final ViewFrame viewFrame;

    public static EconomyPlugin INSTANCE;

    public EconomyPlugin() {

        INSTANCE = this;

        setupConfigs();

        currencyService = new CurrencyService();
        setupCurrencies();

        userService = new UserService(this, CorePlugin.INSTANCE.getDatacenter());
        UserController userController = new UserController(currencyService, userService);

        setupSimpleCommandMap();
        currencyService.getAll().forEach(currency -> registerCommands(new EconomyCommand(this, currency)));

        viewFrame = ViewFrame
                .create(CorePlugin.INSTANCE)
                .with(new RankingInventory(userController, inventory))
                .register();

    }

    public void register() {
        registerListener();
    }

    private void registerListener() {
        Bukkit.getPluginManager().registerEvents(new PlayerListener(userService), CorePlugin.INSTANCE);
    }

    private void setupConfigs(){

        messages = new Configuration(CorePlugin.INSTANCE, "economy", "messages.yml");
        messages.saveDefaultConfig();

        economy = new Configuration(CorePlugin.INSTANCE, "economy", "economy.yml");
        economy.saveDefaultConfig();

        inventory = new Configuration(CorePlugin.INSTANCE, "economy", "inventory.yml");
        inventory.saveDefaultConfig();

    }

    private void registerCommands(CCommand... commands) {
        Arrays.stream(commands).forEach(command -> simplePluginManager.register("twins-core", command));
    }

    private void setupSimpleCommandMap() {

        SimplePluginManager simplePluginManager = (SimplePluginManager) Bukkit.getPluginManager();

        try {

            Field field = SimplePluginManager.class.getDeclaredField("commandMap");
            field.setAccessible(true);

            try {
                this.simplePluginManager = (SimpleCommandMap) field.get(simplePluginManager);
            } catch (Exception e) {
                CorePlugin.INSTANCE.getLogger().log(Level.SEVERE, "Failed to retrieve simplePLuginManager on field.", e);
            }

        } catch (Exception e) {
            CorePlugin.INSTANCE.getLogger().log(Level.SEVERE, "Failed to retrieve commandMap field.", e);
        }

    }

    private void setupCurrencies(){
        new CurrencyLoader(economy).setup().forEach(currencyService::put);
    }

    public Configuration getMessages() {
        return messages;
    }

    public Configuration getEconomy() {
        return economy;
    }

    public Configuration getInventory() {
        return inventory;
    }

    public CurrencyFoundationService getCurrencyService() {
        return currencyService;
    }

    public UserFoundationService getUserService() {
        return userService;
    }

    public ViewFrame getViewFrame() {
        return viewFrame;
    }

}
