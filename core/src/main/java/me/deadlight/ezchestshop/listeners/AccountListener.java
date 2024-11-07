package me.deadlight.ezchestshop.listeners;

import io.zentae.account.provider.event.AccountProviderLoadEvent;
import me.deadlight.ezchestshop.EzChestShop;
import me.deadlight.ezchestshop.data.Config;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class AccountListener implements Listener {

    // the plugin.
    private final EzChestShop plugin;

    public AccountListener() {
        this.plugin = EzChestShop.getPlugin();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onAccountLoad(AccountProviderLoadEvent event) {
        // run synchronously.
        Bukkit.getScheduler().runTask(plugin, () -> {
            // check if we found the economy.
            boolean found = plugin.setupEconomy();
            if (!found) {
                // switch on xp based economy.
                Config.useXP = true;
                // log.
                EzChestShop.logConsole(
                        "&c[&eEzChestShop&c] &4Cannot find vault or economy plugin. Switching to XP based economy... " +
                                "&ePlease note that you need vault and at least one economy plugin installed to use a money based system.");
            }
        });
    }
}
