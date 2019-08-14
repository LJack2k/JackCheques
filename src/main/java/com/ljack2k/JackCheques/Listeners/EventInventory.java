package com.ljack2k.JackCheques.Listeners;

import com.ljack2k.JackCheques.JackCheques;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EventInventory implements Listener {
    private JackCheques plugin;

    private NamespacedKey keyPlugin;
    private NamespacedKey keyOwner;
    private NamespacedKey keyAmount;

    private List<InventoryType> inventoryList = new ArrayList<>();

    public EventInventory(JackCheques pl) {
        this.plugin = pl;

        keyPlugin = new NamespacedKey(plugin, "plugin");
        keyOwner = new NamespacedKey(plugin, "owner");
        keyAmount = new NamespacedKey(plugin, "amount");

        inventoryList.add(InventoryType.FURNACE);
        inventoryList.add(InventoryType.GRINDSTONE);
        inventoryList.add(InventoryType.BLAST_FURNACE);
        inventoryList.add(InventoryType.ANVIL);
        inventoryList.add(InventoryType.BEACON);
        inventoryList.add(InventoryType.BREWING);
        inventoryList.add(InventoryType.CARTOGRAPHY);
        inventoryList.add(InventoryType.CRAFTING);
        inventoryList.add(InventoryType.ENCHANTING);
        inventoryList.add(InventoryType.LECTERN);
        inventoryList.add(InventoryType.LOOM);
        inventoryList.add(InventoryType.MERCHANT);
        inventoryList.add(InventoryType.SMOKER);
        inventoryList.add(InventoryType.STONECUTTER);
        inventoryList.add(InventoryType.WORKBENCH);

        JackCheques.debug("EventInventory Registered");
    }

    @EventHandler (priority = EventPriority.HIGHEST)
    private void onInventoryUpdateListener( InventoryClickEvent event ) {
        if (event.getSlot() != event.getRawSlot()) {
            return;
        }

        if (inventoryList.contains(event.getClickedInventory().getType())) {
            if (event.getClickedInventory() != null) {
                ItemMeta itemMeta = event.getCursor().getItemMeta();
                if (itemMeta != null) {
                    if (itemMeta.getPersistentDataContainer().has(keyPlugin, PersistentDataType.STRING)) {
                        if (itemMeta.getPersistentDataContainer().get(keyPlugin, PersistentDataType.STRING).equals(plugin.getName())) {
                            event.setCancelled(true);
                        }
                    }
                }
            }
        }

        plugin.info(String.format("RawSlot: %s | Slot: %s | Cursor: %s | ClickedInventory: %s | Inventory: %s"
                                , event.getRawSlot()
                                , event.getSlot()
                                , event.getCursor().getType()
                                , event.getClickedInventory().getType().toString()
                                , event.getView().getType().toString()
        ));
    }

}
