# Economy-Plugin

[![License: GPL v3](https://img.shields.io/badge/License-GPLv3-blue.svg)](https://www.gnu.org/licenses/gpl-3.0)

Economy-Plugin is an advanced economy plugin for Minecraft servers, providing **multi-currency support**, **multi-server synchronization**, secure transactions, and high performance. Ideal for servers of any size.

---

## Main Features

### 💰 Multi-currency
- Create as many currencies as you want.
- Support for different currencies simultaneously with custom icons and formatting.

### 🌐 Multi-server
- Full support for servers connected via **BungeeCord / Redis Messaging Service**.
- Transactions between players on different servers are synchronized in real time.

### 📊 Rankings and Balance Top
- Complete ranking system with support for **infinite pages** in the balance top.
- Advanced strategies like **seek pagination** allow accessing the 1st or 300 trillionth user **with the same speed**.
- Real-time updates from the database, without data loss.

### ⚡ Performance and Security
- All transactions are updated instantly in the database.
- Data security and integrity are prioritized.
- Uses caching to quickly retrieve the balances of players on the same server as the player executing the command, improving performance.
- Redis is used to update the cache and handle pending transactions across servers.
- Designed for high performance even on large servers.

### 🔄 Database Synchronization
- Real-time updates.
- Database always synchronized, ensuring **no data loss**.

### 🛠 Available Commands
- `/currency` - View your own balance.
- `/currency view <player>` - View another player's balance.
- `/currency pay <player> <amount>` - Pay another player.
- `/currency set <player> <amount>` - Set another player's balance (**admin**).
- `/currency add <player> <amount>` - Add balance to another player (**admin**).
- `/currency remove <player> <amount>` - Remove balance from another player (**admin**).
- `/currency top` - Open the richest players ranking.

> All commands support placeholders, customizable messages, and instant balance updates.

---

## 🎬 Demo / Video

Watch a full demonstration of **Economy-Plugin** in action:

[![Economy-Plugin Demo](https://img.youtube.com/vi/662k9vZTZAY/0.jpg)](https://youtu.be/662k9vZTZAY)

Click the image to watch the video on YouTube.

---

## Installation

1. Download the latest version of Economy-Plugin from one of the following sources:
    - [GitHub Releases](https://github.com/goncalodelima/Economy-Plugin/releases)
    - [SpigotMC](https://www.spigotmc.org/resources/free-multi-economy-plugin-with-multi-server-support.131260/)

2. Install required dependencies (libs.zip)

3. Place the `.jar` files in your server's `plugins` folder:
    - For a **Bukkit/Spigot/Paper server**, place the main `Economy-Plugin.jar` in `plugins`.
    - **Velocity server with Redis as messaging service**:
        - Place the `EconomyPlugin-Velocity.jar` in the `plugins` folder of your Velocity server.
        - This jar only works on Velocity.
        - Even when using Velocity, it is recommended to use Redis for better security.
        - Even if you don’t use Redis as a messaging service, it is required for synchronizing pending transactions with the cache in a multi-server setup. Make sure Redis is installed and running.
    - **Without Velocity**:
        - You can use **BungeeCord messaging service**.
        - No extra plugin is required.
        - Using Redis as the messaging service is still recommended for optimal security.

4. Database setup:
    - All servers must connect to the same MySQL database to ensure proper synchronization.
    - MySQL is mandatory (the only officially supported database), but compatible variants like MariaDB can also be used.

5. Configure the plugin:
    - After placing the plugin and starting your server once, open the config.yml.
    - Set your MySQL credentials and select which messaging service to use: Redis, BungeeCord, or none. 
    - If "none" is selected, Redis is not required, but cross-server transactions and synchronization will be disabled.
    - Restart the server to save and apply these changes.

---

## 📚 Documentation (Developer API)

#### 📦 Using Economy-Plugin as a dependency

<details>
<summary><strong>Gradle (Kotlin DSL)</strong></summary>

```kotlin
repositories {
    maven("https://repo.codemc.io/repository/goncalodelima/")
}

dependencies {
    implementation("pt.gongas:EconomyPlugin-bukkit:1.0.6")
}
```

</details> 

<details> <summary><strong>Gradle (Groovy DSL)</strong></summary>

```groovy
repositories {
    maven {
        url "https://repo.codemc.io/repository/goncalodelima/"
    }
}

dependencies {
    implementation "pt.gongas:EconomyPlugin-bukkit:1.0.6"
}
```

</details> 

<details> <summary><strong>Maven</strong></summary>

```maven
<repositories>
    <repository>
        <id>codemc-goncalodelima</id>
        <url>https://repo.codemc.io/repository/goncalodelima/</url>
    </repository>
</repositories>

<dependencies>
    <dependency>
        <groupId>pt.gongas</groupId>
        <artifactId>EconomyPlugin-bukkit</artifactId>
        <version>1.0.6</version>
    </dependency>
</dependencies>
```

</details>

#### Withdraw money securely using the API (Bukkit)

```java
Main.class:

public static Pair<EconomyApi<Player>, Currency> economyApi;

@Override
public void onEnable() {

   Currency currency = BukkitEconomyPlugin.plugin.getEconomyApi().getCurrencyService().get(getConfig().getString("currency", "money"));

   if (currency == null) {
      throw new RuntimeException("The currency provided do not exist in the Economy-Plugin plugin.");
   }

   economyApi = new Pair<>(BukkitEconomyPlugin.plugin.getEconomyApi(), currency);

}

Upgrade.class:

// Simple lock to prevent duplicate operations
private final Set<UUID> upgradeCache = new HashMap<>();

public void upgradeIsland(Island island, Player player) {
   
   EconomyUser economyUser = Main.economyApi.key().getUserService().get(player.getUniqueId());
   
   if (economyUser == null) {
       // Something strange happened. The player will have to log in again.
       return;
   }

   // Prevents ugprades at the same time for the same island
   if (upgradeCache.contains(island.getId())) {
      return; // Already processing
   }

   long cents = economyUser.get(Main.economyApi.value());
   double balance = cents / 100D;
   
   int requiredBalance = island.getRequiredBalanceForUpgrade();

   // The cache is not strictly necessary, but recommended
   // because it prevents users from spamming clicks in the menu—for example,
   // trying to upgrade the island without enough balance.
   // If the player has a balance in the database but not yet in the cache,
   // the cache will be updated within moments, allowing the MySQL query to succeed.
   // In the worst case, an extra lookup in the Map is performed.
   if (balance < requiredBalance) {
       // Not enough money
      return;
   }

   // Add island to lock to prevent multiple upgrade attempts at the same time
   upgradeCache.add(island.getId());

   Main.economyApi
           .key()
           .withdrawCurrencyAndNotifyIfNeeded(
                   null,               // Who removes it? In this case, the console, so null.
                   player,             // Player to withdraw the balance
                   economyUser,        // Economy user
                   Main.economyApi.value(), // Currency
                   requiredBalance     // Amount (not in cents)
           )
           .thenAcceptAsync(result -> {

              // Always release the lock
              upgradeCache.remove(island.getId());

              switch (result) {

                 case QueryUserResult.SuccessNoData -> {
                     
                    // Withdrawal successful!
                    // Perform island upgrade logic
                    
                 }

                 case QueryUserResult.Error -> {
                    
                    // An error occurred, such as MySQL being offline, 
                    // insufficient balance, etc. It's normal to report 
                    // 'insufficient balance' even if the cache doesn't show it, 
                    // because the plugin runs across multiple processes.
                     
                 }

                 
                 // The default block is necessary because, in theory, it could still return Success.
                 // Methods like this one will never actually return Success. You can always check 
                 // the implementation to be sure what values can be returned.
                 // Success is only returned by methods that expect a value—like removeCurrency. 
                 // For example, if you try to remove 100 coins but the user only has 70, 
                 // the method returns Success with information from the query showing how much was actually removed.
                 // This method, however, only removes currency if the balance is sufficient and requires a UUID. 
                 // Therefore, it will only ever return SuccessNoData or Error.
                 // In other methods (e.g., removeCurrency(...)), Success is returned, but SuccessNoData is not.
                 
                 default -> throw new IllegalStateException(
                         "Unexpected result: " + result
                 );
                 
              }

              // It is necessary to specify the main Bukkit thread to avoid concurrency issues.
           }, Bukkit.getScheduler().getMainThreadExecutor(YourPlugin.INSTANCE));
}

```

---

## License

Economy-Plugin is licensed under **GNU General Public License v3**.  
You can redistribute and/or modify it under the terms of the GPL.  
For full license details, see [GNU GPL v3](https://www.gnu.org/licenses/gpl-3.0).

---

## Contribution

- Open an **issue** to report bugs or suggest features.
- Fork the project, create a branch, and submit a **pull request**.

---

## Support / Sponsorship

If you enjoy using **Economy-Plugin** and want to support its development, you can sponsor me via GitHub Sponsors:

[![Sponsor @goncalodelima](https://img.shields.io/badge/Sponsor-Goncalodelima-ff69b4?style=flat&logo=github)](https://github.com/sponsors/goncalodelima)

Your support helps maintain the plugin, fix bugs, and add new features.
