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
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.DoubleChest;
import org.bukkit.entity.Player;
import org.bukkit.inventory.DoubleChestInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
//success player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 0.5f, 0.5f);
//fail player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_IRON_XYLOPHONE, 0.5f, 0.5f);
//storage player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BIT, 0.5f, 0.5f);

public class OwnerShopGUI {
    public OwnerShopGUI() {}


    public void showGUI(Player player, PersistentDataContainer data, Block containerBlock, boolean isAdmin) {

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
        //double sellPrice = data.get(new NamespacedKey(EzChestShop.getPlugin(), "sell"), PersistentDataType.DOUBLE);
        double buyPrice = data.get(new NamespacedKey(EzChestShop.getPlugin(), "buy"), PersistentDataType.DOUBLE);
        boolean disabledBuy = data.get(new NamespacedKey(EzChestShop.getPlugin(), "dbuy"), PersistentDataType.INTEGER) == 1;
        boolean disabledSell = data.get(new NamespacedKey(EzChestShop.getPlugin(), "dsell"), PersistentDataType.INTEGER) == 1;

        ContainerGui container = GuiData.getOwnerShop();

        Gui gui = new Gui(container.getRows(), lm.guiOwnerTitle(shopOwner));
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
            if (key.startsWith("decorative-")) {

                ContainerGuiItem decorativeItemStack = container.getItem(key).setName(Utils.colorify("&d"));

                GuiItem buyItem = new GuiItem(decorativeItemStack.getItem(), event -> {
                    event.setCancelled(true);
                });

                Utils.addItemIfEnoughSlots(gui, decorativeItemStack.getSlot(), buyItem);
            }
        });

        if (container.hasItem("storage")) {
            ContainerGuiItem guiStorageItem = container.getItem("storage")
                    .setName(lm.buttonStorage())
                    .setLore(lm.buttonStorageLore());

            GuiItem storageGUI = new GuiItem(guiStorageItem.getItem(), event -> {
                event.setCancelled(true);
                Inventory lastinv = Utils.getBlockInventory(containerBlock);
                if (lastinv instanceof DoubleChestInventory) {
                    DoubleChest doubleChest = (DoubleChest) lastinv.getHolder();
                    lastinv = doubleChest.getInventory();
                }
                player.openInventory(lastinv);
            });

            //containerBlock storage
            Utils.addItemIfEnoughSlots(gui, guiStorageItem.getSlot(), storageGUI);
        }

        //settings item
        if (container.hasItem("settings")) {
            ContainerGuiItem settingsItemStack = container.getItem("settings");
            settingsItemStack.setName(lm.settingsButton());
            settingsItemStack.setLore(lm.settingsButtonLore());
            GuiItem settingsGui = new GuiItem(settingsItemStack.getItem(), event -> {
               event.setCancelled(true);
               //opening the settigns menu
                SettingsGUI settingsGUI = new SettingsGUI();
                settingsGUI.showGUI(player, containerBlock, isAdmin);
            });
            //settings item
            Utils.addItemIfEnoughSlots(gui, settingsItemStack.getSlot(), settingsGui);
        }
        gui.open(player);
    }

    private ItemStack disablingCheck(ItemStack mainItem, boolean disabling) {
        if (disabling){
            //disabled Item
            LanguageManager lm = new LanguageManager();
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
