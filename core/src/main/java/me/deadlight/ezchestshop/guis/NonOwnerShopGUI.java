package me.deadlight.ezchestshop.guis;

import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import me.deadlight.ezchestshop.EzChestShop;
import me.deadlight.ezchestshop.data.LanguageManager;
import me.deadlight.ezchestshop.data.ShopContainer;
import me.deadlight.ezchestshop.data.gui.ContainerGui;
import me.deadlight.ezchestshop.data.gui.ContainerGuiItem;
import me.deadlight.ezchestshop.data.gui.GuiData;
import me.deadlight.ezchestshop.utils.Utils;
import me.deadlight.ezchestshop.utils.objects.EzShop;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
//mojodi
//balance

public class NonOwnerShopGUI {

    public NonOwnerShopGUI() {}

    public void showGUI(Player player, PersistentDataContainer data, Block containerBlock) {
        LanguageManager lm = new LanguageManager();
        OfflinePlayer offlinePlayerOwner = Bukkit.getOfflinePlayer(UUID.fromString(data.get(new NamespacedKey(EzChestShop.getPlugin(), "owner"), PersistentDataType.STRING)));
        String shopOwner = offlinePlayerOwner.getName();
        if (shopOwner == null) {
            boolean result = Utils.reInstallNamespacedKeyValues(data, containerBlock.getLocation());
            if (!result) {
                player.sendMessage(lm.chestShopProblem());
                return;
            }
            containerBlock.getState().update();
            EzShop shop = ShopContainer.getShop(containerBlock.getLocation());
            shopOwner = Bukkit.getOfflinePlayer(shop.getOwnerID()).getName();
            if (shopOwner == null) {
                player.sendMessage(lm.chestShopProblem());
                System.out.println("EzChestShop ERROR: Shop owner is STILL null. Please report this to the EzChestShop developer for furthur investigation.");
                return;
            }
        }
        double sellPrice = data.get(new NamespacedKey(EzChestShop.getPlugin(), "sell"), PersistentDataType.DOUBLE);
        double buyPrice = data.get(new NamespacedKey(EzChestShop.getPlugin(), "buy"), PersistentDataType.DOUBLE);
        boolean disabledBuy = data.get(new NamespacedKey(EzChestShop.getPlugin(), "dbuy"), PersistentDataType.INTEGER) == 1;
        boolean disabledSell = data.get(new NamespacedKey(EzChestShop.getPlugin(), "dsell"), PersistentDataType.INTEGER) == 1;

        ContainerGui container = GuiData.getShop();

        Gui gui = new Gui(container.getRows(), lm.guiNonOwnerTitle(shopOwner));
        gui.getFiller().fill(container.getBackground());

        ItemStack mainitem = Utils.decodeItem(data.get(new NamespacedKey(EzChestShop.getPlugin(), "item"), PersistentDataType.STRING));
        if (container.hasItem("shop-item")) {
            ItemStack guiMainItem = mainitem.clone();
            ItemMeta mainmeta = guiMainItem.getItemMeta();
            // Set the lore and keep the old one if available
            if (mainmeta.hasLore()) {
                List<String> prevLore = mainmeta.getLore();
                prevLore.add("");
                List<String> mainItemLore = Collections.singletonList(lm.initialBuyPrice(buyPrice));
                prevLore.addAll(mainItemLore);
                mainmeta.setLore(prevLore);
            } else {
                // retrieve the initial buy price string.
                String initialBuyPrice = lm.initialBuyPrice(buyPrice);
                // check if they are empty.
                if(!initialBuyPrice.isEmpty()) {
                    List<String> mainItemLore = Collections.singletonList(initialBuyPrice);
                    mainmeta.setLore(mainItemLore);
                }
            }
            guiMainItem.setItemMeta(mainmeta);
            GuiItem guiitem = new GuiItem(guiMainItem, event -> {
                event.setCancelled(true);
            });

            Utils.addItemIfEnoughSlots(gui, container.getItem("shop-item").getSlot(), guiitem);
        }

        container.getItemKeys().forEach(key -> {
            if (key.startsWith("buy-")) {
                String amountString = key.split("-")[1];
                int amount = 1;
                if (amountString.equals("all")) {
                    amount = Integer.parseInt(Utils.calculateBuyPossibleAmount(Bukkit.getOfflinePlayer(player.getUniqueId()), player.getInventory().getStorageContents(), Utils.getBlockInventory(containerBlock).getStorageContents(), buyPrice, mainitem));
                } else if (amountString.equals("maxStackSize")) {
                    amount = mainitem.getMaxStackSize();
                    container.getItem(key).setAmount(amount);
                } else {
                    try {
                        amount = Integer.parseInt(amountString);
                    } catch (NumberFormatException e) {}
                }
                // the item name.
                String name = amountString.equalsIgnoreCase("all")
                        ? lm.buyAllTitle()
                        : lm.buttonBuyXTitle(amount);
                // the item lore.
                List<String> lore = amountString.equalsIgnoreCase("all")
                        ? lm.buyAllLore(buyPrice * amount)
                        : lm.buttonBuyXLore(buyPrice * amount, amount);

                ContainerGuiItem buyItemStack = container.getItem(key)
                        .setLore(lore)
                        .setName(name);

                final int finalAmount = amount;
                GuiItem buyItem = new GuiItem(disablingCheck(buyItemStack.getItem(), disabledBuy), event -> {
                    // buy things
                    event.setCancelled(true);
                    if (disabledBuy) {
                        return;
                    }
                    ShopContainer.buyItem(containerBlock, buyPrice * finalAmount, finalAmount, mainitem, player, offlinePlayerOwner, data);
                    showGUI(player, data, containerBlock);
                });

                Utils.addItemIfEnoughSlots(gui, buyItemStack.getSlot(), buyItem);
            } else if (key.startsWith("decorative-")) {

                ContainerGuiItem decorativeItemStack = container.getItem(key).setName(Utils.colorify("&d"));

                GuiItem buyItem = new GuiItem(decorativeItemStack.getItem(), event -> {
                    event.setCancelled(true);
                });

                Utils.addItemIfEnoughSlots(gui, decorativeItemStack.getSlot(), buyItem);
            }
        });
        gui.open(player);
    }

    

    private ItemStack disablingCheck(ItemStack mainItem, boolean disabling) {
        LanguageManager lm = new LanguageManager();
        if (disabling){
            //disabled Item
            ItemStack disabledItemStack = new ItemStack(Material.BARRIER, mainItem.getAmount());
            ItemMeta disabledItemMeta = disabledItemStack.getItemMeta();
            disabledItemMeta.setDisplayName(lm.disabledButtonTitle());
            disabledItemMeta.setLore(lm.disabledButtonLore());
            disabledItemStack.setItemMeta(disabledItemMeta);

            return disabledItemStack;
        } else {
            return mainItem;
        }
    }
}
