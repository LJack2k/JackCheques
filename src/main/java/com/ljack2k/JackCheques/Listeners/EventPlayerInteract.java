package com.ljack2k.JackCheques.Listeners;

import com.ljack2k.JackCheques.JackCheques;
import com.ljack2k.JackCheques.Utils.LangUtil;
import net.ess3.api.MaxMoneyException;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.persistence.PersistentDataType;

import java.math.BigDecimal;

public class EventPlayerInteract implements Listener {
	private JackCheques plugin;

	private NamespacedKey keyPlugin;
	private NamespacedKey keyOwner;
	private NamespacedKey keyAmount;

	public EventPlayerInteract(JackCheques pl) {
		this.plugin = pl;

		keyPlugin = new NamespacedKey(plugin, "plugin");
		keyOwner = new NamespacedKey(plugin, "owner");
		keyAmount = new NamespacedKey(plugin, "amount");

		JackCheques.debug("PlayerInteractEvent Registered");
	}
	
	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		if (event.hasItem()) {
			ItemStack item = event.getItem();
			ItemMeta im = item.getItemMeta();

			if (im != null) {
				if (im.getPersistentDataContainer().has(keyPlugin, PersistentDataType.STRING) &&
					im.getPersistentDataContainer().has(keyAmount, PersistentDataType.DOUBLE)) {
					if (im.getPersistentDataContainer().get(keyPlugin, PersistentDataType.STRING).equals(plugin.getName())) {
						double amount = im.getPersistentDataContainer().get(keyAmount, PersistentDataType.DOUBLE);
						try {
							plugin.essentials.getUser(event.getPlayer()).giveMoney(BigDecimal.valueOf(amount));
							item.setAmount(item.getAmount() - 1);
						} catch (MaxMoneyException e) {
							plugin.sendChatMessage(event.getPlayer(), ChatColor.RED + "" + LangUtil.Message.SOMETHING_WRONG);
							plugin.debug(e.toString());
						}
						event.setCancelled(true);
					}
				}
			}
		}
	}
	
}
