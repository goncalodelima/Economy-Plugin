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
    - For a **Paper server**, place the main `Economy-Plugin.jar` in `plugins`.
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

### 📦 Using Economy-Plugin as a dependency

<details>
<summary><strong>Gradle (Kotlin DSL)</strong></summary>

```kotlin
repositories {
    maven("https://repo.codemc.io/repository/goncalodelima/")
}

dependencies {
    compileOnly("pt.gongas:EconomyPlugin-paper:1.2.4")
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
    compileOnly "pt.gongas:EconomyPlugin-paper:1.2.4"
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
        <artifactId>EconomyPlugin-paper</artifactId>
        <version>1.2.4</version>
        <scope>provided</scope>
    </dependency>
</dependencies>
```

</details>

### Withdraw money securely using the API (Paper)

The Economy-Plugin provides **two distinct ways** to interact with the economy system. Choosing the correct one is important to avoid data inconsistencies and to match your plugin's architecture.

### 1️⃣ High-Level Economy API (Default / Cached)

This is the **simplest** way to interact with the economy system.

#### When to use
- Your plugin **does not require atomic synchronization** with its own database.
- You only need to withdraw, deposit, or transfer money safely.
- Temporary desynchronization between your plugin and the economy is acceptable. (e.g. pending updates)

#### Characteristics
- Uses internal caching.
- Automatically handles notifications and cross-server synchronization.
- Safe for most plugins.
- **Do NOT use inside database transactions.**

#### Main.class:
```java

public static Pair<EconomyApi<Player>, Currency> economyApi;

@Override
public void onEnable() {

   Currency currency = PaperEconomyPlugin.plugin.getEconomyApi().getCurrencyService().get(getConfig().getString("currency", "money"));

   if (currency == null) {
      throw new RuntimeException("The currency provided do not exist in the Economy-Plugin plugin.");
   }

   economyApi = new Pair<>(PaperEconomyPlugin.plugin.getEconomyApi(), currency);

}
```

#### Upgrade.class:
```java
// Simple lock to prevent duplicate operations
private final Set<UUID> upgradeCache = new HashSet<>();

public void upgradeIsland(Island island, Player player) {
   
   User economyUser = Main.economyApi.key().getUserService().get(player.getUniqueId());
   
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

### 2️⃣ Low-Level + EconomyTransactionalApi (Atomic / Database-Safe)

This approach is advanced and should only be used when strictly necessary.

#### When to use
- Your plugin must update its own database atomically with economy changes.
- Economy updates must be committed in the same SQL transaction.
- You need full control over database execution order.
Examples:
- Shops or auctions with SQL persistence
- Marketplaces
- Cross-table consistency requirements

#### Characteristics
- No caching
- No deferred updates
- Uses low-level UserService methods
- Must be combined with EconomyTransactionalApi

#### Important rules

❌ Do NOT use high-level EconomyApi methods
❌ Do NOT rely on economy cache
✅ Use only addCurrencyLowLevel, withdrawCurrencyLowLevel, updateCurrenciesLowLevel, etc.

#### Cache & Messaging handling

When using EconomyTransactionalApi, you are responsible for:
- Updating your plugin cache
- Updating economy cache (if needed)
- Publishing transaction messages for offline users

This is intentionally manual to ensure correctness.

⚠️ Incorrect cache usage may cause visual inconsistencies only, not data corruption.

#### Main.class:
```java

public static Pair<EconomyApi<Player>, Currency> economyApi;

@Override
public void onEnable() {

   Currency currency = PaperEconomyPlugin.plugin.getEconomyApi().getCurrencyService().get(getConfig().getString("currency", "money"));

   if (currency == null) {
      throw new RuntimeException("The currency provided does not exist in the Economy-Plugin plugin.");
   }

   economyApi = new Pair<>(PaperEconomyPlugin.plugin.getEconomyApi(), currency);

   auctionItemService = new AuctionItemService(auctionItemRepository, ..., economyApi.key().getUserService(), economyApi.key().getEconomyTransactionalApi(), economyApi.value());
}
```

#### AuctionItemService.class: (Implements: `AuctionItemFoundationItemService`)
```java

@Override
public CompletableFuture<QueryUserResult> purchaseItem(UUID buyerUuid, AuctionItem auctionItem) {
    return CompletableFuture.supplyAsync(() -> auctionItemRepository.purchaseItem(buyerUuid, auctionItem), databaseExecutor)
            .exceptionally(e -> {
                logger.log(Level.SEVERE, "Failed to purchase auction item with ID " + auctionItem.getId() + " by buyer " + buyerUuid, e);
                return false;
            });
}
```

#### AuctionItemRepository.class: (Implements: `AuctionItemFoundationItemRepository`)
```java

@Override
public QueryUserResult purchaseItem(UUID buyerUuid, AuctionItem auctionItem) {

    try {

        return transactionalApi.executeInEconomyTransaction(database, (userService, executor, connection) -> {
            
            int rows = executor.query("""
                            UPDATE auction_house_items
                            SET auctionEnded = TRUE
                            WHERE id = ?
                            AND auctionEnded = FALSE
                            """)
                    .writeAndReturnRowCount(statement -> statement.set(1, auctionItem.getId()), connection);
            
            if (rows != 1) {
                return new QueryUserResult.Error(ErrorType.EXTERNAL_PLUGIN);
            }

            QueryUserResult result = userService.updateCurrenciesLowLevel(buyerUuid, auctionItem.getSellerUuid(), currency, auctionItem.getCentsPrice(), executor, connection);

            if (result instanceof QueryUserResult.Error error) {
                return error;
            }

            return new QueryUserResult.SuccessNoData();
            
        });

    } catch (SQLException e) {
        throw new RuntimeException(e);
    }
    
}
```

#### AuctionHousePurchaseView
```java

// Simple lock to prevent duplicate operations
private final Map<UUID, Integer> pendingCache = new HashMap<>();

@EventHandler
public void onInventoryClick(InventoryClickEvent event) {

        if (!((event.getInventory().getHolder(false)) instanceof AuctionHousePurchaseGuiHolder gui)) {
            return;
        }

        event.setCancelled(true);

        ItemStack clickedItem = event.getCurrentItem();

        if (clickedItem == null) {
            return;
        }

        if (event.getRawSlot() == confirmSlot) {

           HumanEntity humanEntity = event.getWhoClicked();
           UUID buyerUuid = humanEntity.getUniqueId();

           // Helps reduce load by avoiding duplicate purchase attempts from the same user
           // in this JVM. It is not a real prevention—at worst, it just results in
           // an extra lookup in the map. The actual protection is enforced by the SQL query.
           if (pendingCache.containsKey(buyerUuid)) {
              // Already processing
              humanEntity.sendMessage(Component.translatable("lang.wait"));
              UISoundUtil.playErrorSound(humanEntity);
              return;
           }
           
           EconomyApi<Player> api = Main.economyApi.key();
           User buyerUser = api.getUserService().get(buyerUuid);

           if (buyerUser == null) {
              // Something strange happened and the player needs to log back into the server
              humanEntity.sendMessage(Component.translatable("lang.error-relog"));
              UISoundUtil.playErrorSound(humanEntity);
              return;
           }
           
           long balance = buyerUser.get(Main.economyApi.value());
           AuctionItem auctionItem = gui.getAuctionItem();
           
           // The cache is not strictly necessary, but recommended
           // because it prevents users from spamming clicks in the menu—for example,
           // trying to upgrade the island without enough balance.
           // If the player has a balance in the database but not yet in the cache,
           // the cache will be updated within moments, allowing the MySQL query to succeed.
           // In the worst case, an extra lookup in the Map is performed.
           if (balance < auctionItem.getCentsPrice()) {
              // Not enough money
              humanEntity.sendMessage(Component.translatable("not-enough-money"));
              UISoundUtil.playErrorSound(humanEntity);
              return;
           }

           // Adds the auctionItem ID to the lock for the same user in this process
           // to prevent multiple purchase attempts at the same time.
           pendingCache.put(buyerUuid, auctionItem.getId());

            auctionItemService.purchaseItem(buyerUuid, auctionItem).thenAcceptAsync(query -> {

               // Always release the lock
               pendingCache.remove(buyerUuid);
                
               if (result instanceof QueryUserResult.Error(ErrorType type)) {

                  Component errorMessage = switch (type) {
                     case EXTERNAL_PLUGIN -> Component.translatable("auction-item-expired-or-purchased");
                     case NOT_ENOUGH_BALANCE -> Component.translatable("not-enough-money");
                     case NOT_FOUND -> Component.translatable("seller-user-not-found");
                     default -> Component.translatable("lang.error"); // Exception error
                  };

                  humanEntity.sendMessage(errorMessage);
                  UISoundUtil.playErrorSound(humanEntity);
                  return;
               }
                
                // update economy cache here
                
                UUID sellerUuid = auctionItem.getSellerUuid();
                User sellerUser = api.getUserService().get(sellerUuid);

                boolean isBuyerOnline = buyerUser.isOnline();
                boolean isSellerOnline = sellerUser != null;

                if (isBuyerOnline) {
                    api.getTrackedUuids().add(buyerUser.getUuid());
                }

                if (isSellerOnline) {
                    api.getTrackedUuids().add(sellerUser.getUuid());
                }

                RTopic transactions = api.getTransactions();

                if (transactions != null && (!isBuyerOnline || !isSellerOnline)) {
                    UUID senderUuid = isBuyerOnline ? null : buyerUuid;
                    UUID receiverUuid = isSellerOnline ? null : sellerUuid;
                    transactions.publishAsync(new TransactionMessage(senderUuid, receiverUuid));
                }
                
                // update your plugin cache here if needed
                // (...)
               
               
               // It is necessary to specify the main Bukkit thread to avoid concurrency issues.
            }, Bukkit.getScheduler().getMainThreadExecutor(YourPlugin.INSTANCE));

        }

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
