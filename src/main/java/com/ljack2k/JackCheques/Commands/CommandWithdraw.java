package com.ljack2k.JackCheques.Commands;

import com.earth2me.essentials.User;
import com.ljack2k.JackCheques.JackCheques;
import com.ljack2k.JackCheques.Utils.LangUtil;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class CommandWithdraw implements CommandExecutor {
    private JackCheques plugin;
    private NamespacedKey keyPlugin;
    private NamespacedKey keyOwner;
    private NamespacedKey keyAmount;

    public CommandWithdraw(JackCheques pl) {
        plugin = pl;

        keyPlugin = new NamespacedKey(plugin, "plugin");
        keyOwner = new NamespacedKey(plugin, "owner");
        keyAmount = new NamespacedKey(plugin, "amount");

        JackCheques.debug("CommandWithdraw Registered");
    }

    public boolean onCommand(CommandSender sender, Command command, String alias, String[] args) {
        // Send help to who needs it
        if (args.length > 0) {
            if (args[0].equals("?") || args[0].equals("help")) {
                sendCommandHelp(sender);
                return true;
            }
        }

        // Only players can do this command
        if (sender instanceof Player) {
            // Get player
            Player player = ((Player) sender).getPlayer();

            // Does the player have permissions to use this command
            if (player.hasPermission(plugin.getBasePermissionNode() + ".withdraw")) {
                // Check if arguments where given
                if (args.length > 0) {
                    // Get given amount and check if it is actually a number
                    double amount;
                    try {
                        amount = Double.parseDouble(args[0]);
                    } catch (NumberFormatException | NullPointerException nfe) {
                        plugin.sendChatMessage(sender, ChatColor.RED + "" + LangUtil.Message.CHEQUE_AMOUNT_ERROR);
                        return false;
                    }

                    // Negative amounts are not allowed
                    if (amount >= plugin.getConfig().getInt("MinimumChequeAmount")) {

                        // Create new item
                        ItemStack itemStack = new ItemStack(plugin.getChequeMaterial());

                        // Get meta data
                        ItemMeta itemMeta = itemStack.getItemMeta();

                        // Set display name
                        itemMeta.setDisplayName(ChatColor.GREEN + "" + LangUtil.Message.CHEQUE);

                        // Create lore list
                        List<String> loreList = new ArrayList<>();
                        loreList.add(ChatColor.AQUA + "Value: " + ChatColor.GOLD  + plugin.getConfig().getString("CurrencyPrefix") + String.format("%.2f", amount) + plugin.getConfig().getString("CurrencySuffix"));
                        loreList.add(ChatColor.AQUA + "Signed by: " + player.getDisplayName());

                        // Has a custom message been set? This only happens if the player has permissions
                        if (args.length > 1) {

                            // Does the player have permissions for setting a custom message?
                            if (player.hasPermission(plugin.getBasePermissionNode() + ".withdraw.description")) {

                                // Use arguments list as message
                                ArrayList<String> customMessageList = new ArrayList<String>(Arrays.asList(args));

                                // Add quotation marks
                                customMessageList.remove(0);

                                // Apply styling
                                String customMessage = ChatColor.AQUA + "" + LangUtil.Message.DESCRIPTION +": " + ChatColor.GRAY + "" + ChatColor.ITALIC + String.join(" ", customMessageList);

                                // Add to lore
                                loreList.add(customMessage);
                            } else {

                                // No permission to use custom messages
                                plugin.sendChatMessage(player, ChatColor.RED + "" + LangUtil.Message.NO_PERMISSION_DESCRIPTION);
                                return false;
                            }
                        }

                        // Empty line
                        loreList.add("");

                        // Use lore last
                        loreList.add(ChatColor.YELLOW + "" + LangUtil.Message.USE_CHEQUE);

                        // Set lore to meta data
                        itemMeta.setLore(loreList);

                        // Set NBT data
                        itemMeta.getPersistentDataContainer().set(keyPlugin, PersistentDataType.STRING, plugin.getName());
                        itemMeta.getPersistentDataContainer().set(keyOwner, PersistentDataType.STRING, player.getUniqueId().toString());
                        itemMeta.getPersistentDataContainer().set(keyAmount, PersistentDataType.DOUBLE, amount);

                        // Apply lore and NBT to item
                        itemStack.setItemMeta(itemMeta);

                        // Get essentials player for transaction
                        User essPlayer = plugin.essentials.getUser(player);

                        // Check if player can afford it
                        if (essPlayer.canAfford(BigDecimal.valueOf(amount))) {
                            // Attempt to give the item
                            if (givePlayerBankNote(player, itemStack)) {
                                // Successfully given the item, now do the money transaction
                                essPlayer.takeMoney(BigDecimal.valueOf(amount));
                            } else {
                                // Can't give the item
                                plugin.sendChatMessage(player, ChatColor.RED + "" + LangUtil.Message.YOUR_INVENTORY_FULL);
                            }
                        } else {
                            // Not enough money
                            plugin.sendChatMessage(sender, ChatColor.RED + "" + LangUtil.Message.NOT_ENOUGH_MONEY + " " + String.format("%.2f", amount) + plugin.getConfig().getString("CurrencySuffix"));
                        }
                    } else {
                        // No negative amounts
                        plugin.sendChatMessage(player, ChatColor.RED + "" + String.format("" + LangUtil.Message.NEGATIVE_AMOUNT, plugin.getConfig().getInt("MinimumChequeAmount")));
                    }

                } else {
                    // No amount given
                    plugin.sendChatMessage(sender, ChatColor.RED + "" + LangUtil.Message.NO_AMOUNT);
                }

            } else {
                // Console can't do this, needs to be a player
                plugin.sendChatMessage(sender, ChatColor.RED + "" + LangUtil.Message.NOT_INGAME_PLAYER);
            }
        }
        return true;
    }

    public void sendCommandHelp(CommandSender sender) {
        plugin.sendChatHeader(sender, "" + LangUtil.Message.HELP_WITHDRAW_HEADER);
        plugin.sendChatMessage(sender, "" + LangUtil.Message.HELP_WITHDRAW);
        plugin.sendChatMessage(sender, ChatColor.YELLOW + "/withdraw " + ChatColor.GREEN + "<amount> " + ChatColor.LIGHT_PURPLE + "<description>");
        if (sender instanceof Player) {
            if (((Player) sender).getPlayer().hasPermission(plugin.getBasePermissionNode() + ".withdraw.description")) {
                plugin.sendChatMessage(sender, ChatColor.DARK_GREEN + "" + LangUtil.Message.HELP_CHEQUE_DESCRIPTION_HAS_PERMISSION);
            } else {
                plugin.sendChatMessage(sender, ChatColor.GOLD + "" + LangUtil.Message.HELP_CHEQUE_DESCRIPTION_NO_PERMISSION);
            }
        } else {
            plugin.sendChatMessage(sender, ChatColor.RED + "" + LangUtil.Message.NOT_INGAME_PLAYER);
        }
    }

    /**
     * Give Player the item only if it can stack or if there space in the inventory and returns true. This will
     * not drop the item. When it can't give the item, the plugin will return false;
     *
     * @param player Player to give the item to
     * @param item The item to give
     * @return boolean Success or not
     *
     */
    public boolean givePlayerBankNote(Player player, ItemStack item)
    {
        // If there are not free inventory slots
        if(player.getInventory().firstEmpty() == -1)
        {
            // Attempt to add items to the inventory, if it fails then return the items it could not place
            Map<Integer,ItemStack> couldNotStore = player.getInventory().addItem(item);

            // Check the map of items it could not place is empty
            if(couldNotStore.isEmpty()) {
                // Item has been stacked
                return true;
            }
            // Can't stack item either
            return false;
        }
        else
        {
            // There is space so give the item
            player.getInventory().addItem(item);
            return true;
        }
    }

}
