package com.ljack2k.JackCheques.Commands;

import com.earth2me.essentials.User;
import com.ljack2k.JackCheques.JackCheques;
import com.ljack2k.JackCheques.Utils.LangUtil;
import org.bukkit.Bukkit;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class CommandVoucher  implements CommandExecutor {
    private JackCheques plugin;
    private NamespacedKey keyPlugin;
    private NamespacedKey keyOwner;
    private NamespacedKey keyAmount;

    public CommandVoucher(JackCheques pl) {
        plugin = pl;

        keyPlugin = new NamespacedKey(plugin, "plugin");
        keyOwner = new NamespacedKey(plugin, "owner");
        keyAmount = new NamespacedKey(plugin, "amount");

        JackCheques.debug("CommandVoucher Registered");
    }

    public boolean onCommand(CommandSender sender, Command command, String alias, String[] args) {
        // Send help to who needs it
        if (args.length > 0) {
            if (args[0].equals("?") || args[0].equals("help")) {
                if (sender.hasPermission(plugin.getBasePermissionNode() + ".voucher")) {
                    sendCommandHelp(sender);
                }
                return true;
            }
        }

            // Does the player have permissions to use this command
            if (sender.hasPermission(plugin.getBasePermissionNode() + ".voucher")) {
                // Check if arguments where given
                if (args.length > 0) {

                    // Get player from argument
                    Player player = Bukkit.getPlayerExact(args[0]);

                    // Make sure player is found
                    if (player != null) {

                        // Check for more arguments for amount
                        if (args.length > 1) {

                            // Get given amount and check if it is actually a number
                            double amount;
                            try {
                                amount = Double.parseDouble(args[1]);
                            } catch (NumberFormatException | NullPointerException nfe) {
                                plugin.sendChatMessage(sender, ChatColor.RED + "" + LangUtil.Message.VOUCHER_AMOUNT_ERROR);
                                return false;
                            }

                            // Negative amounts are not allowed
                            if (amount > 0) {

                                String description = "";

                                // Has a custom message been set? This only happens if the player has permissions
                                if (args.length > 2) {

                                    // Does the player have permissions for setting a custom message?
                                    if (sender.hasPermission(plugin.getBasePermissionNode() + ".voucher.description")) {

                                        // Use arguments list as message
                                        ArrayList<String> customMessageList = new ArrayList<String>(Arrays.asList(args));

                                        // Remove command arguments that aren't part of the message
                                        customMessageList.remove(1);
                                        customMessageList.remove(0);

                                        // Apply styling
                                        description = ChatColor.AQUA + "" + LangUtil.Message.DESCRIPTION + ": " + ChatColor.GRAY + "" + ChatColor.ITALIC + String.join(" ", customMessageList);

                                    } else {

                                        // No permission to use custom messages
                                        plugin.sendChatMessage(sender, "" + LangUtil.Message.NO_PERMISSION_DESCRIPTION);
                                    }
                                }

                                ItemStack itemStack = createBankNote(player, amount, description);

                                // Attempt to give the item
                                if (!givePlayerBankNote(player, itemStack)) {
                                    // Can't give the item
                                    plugin.sendChatMessage(sender, ChatColor.RED + "" + LangUtil.Message.PLAYER_INVENTORY_FULL);
                                }
                                plugin.sendChatMessage(player, ChatColor.GREEN + "" + LangUtil.Message.VOUCHER_RECEIVED);
                            } else {
                                // No negative amounts
                                plugin.sendChatMessage(sender, ChatColor.RED + "" + LangUtil.Message.NEGATIVE_AMOUNT);
                            }
                        } else {
                            // No amount given
                            plugin.sendChatMessage(sender, ChatColor.RED + "" + LangUtil.Message.NO_AMOUNT);
                        }
                    } else {
                        plugin.sendChatMessage(sender, ChatColor.RED + String.format("" + LangUtil.Message.VOUCHER_CANT_FIND_PLAYER, args[0]));
                    }
                } else {
                    // No amount given
                    plugin.sendChatMessage(sender, ChatColor.RED + "" + LangUtil.Message.VOUCHER_NO_PLAYER_GIVEN);
                }

            } else {
                // Console can't do this, needs to be a player
                plugin.sendChatMessage(sender, "" + LangUtil.Message.NO_COMMAND_PERMISSION);
            }
        return true;
    }

    public void sendCommandHelp(CommandSender sender) {
        plugin.sendChatHeader(sender, "" + LangUtil.Message.HELP_VOUCHER_HEADER);
        plugin.sendChatMessage(sender, "" + LangUtil.Message.HELP_WITHDRAW);
        plugin.sendChatMessage(sender, ChatColor.YELLOW + "/voucher " + ChatColor.GREEN + "<playername> <amount> " + ChatColor.LIGHT_PURPLE + "<description>");
        if (((Player) sender).getPlayer().hasPermission(plugin.getBasePermissionNode() + ".voucher.description")) {
            plugin.sendChatMessage(sender, ChatColor.DARK_GREEN + "" + LangUtil.Message.HELP_VOUCHER_DESCRIPTION_HAS_PERMISSION);
        } else {
            plugin.sendChatMessage(sender, ChatColor.GOLD + "" + LangUtil.Message.HELP_VOUCHER_DESCRIPTION_NO_PERMISSION);
        }
    }

    public ItemStack createBankNote(Player player, double amount, String description) {
// Create new item
        ItemStack itemStack = new ItemStack(plugin.getVoucherMaterial());

        // Get meta data
        ItemMeta itemMeta = itemStack.getItemMeta();

        // Set display name
        itemMeta.setDisplayName(ChatColor.GREEN + "" + LangUtil.Message.VOUCHER);

        // Create lore list
        List<String> loreList = new ArrayList<>();
        loreList.add(ChatColor.AQUA + "" + LangUtil.Message.AMOUNT + ": " + ChatColor.GOLD  + plugin.getConfig().getString("CurrencyPrefix") + String.format("%.2f", amount) + plugin.getConfig().getString("CurrencySuffix"));
        loreList.add(ChatColor.AQUA + "" + LangUtil.Message.ISSUED_TO + ": " + player.getDisplayName());

        // Add to lore
        if (!description.isEmpty()) {
            loreList.add(description);
        }

        // Empty line
        loreList.add("");

        // Use lore last
        loreList.add(ChatColor.YELLOW + "" + LangUtil.Message.USE_VOUCHER);

        // Set lore to meta data
        itemMeta.setLore(loreList);

        // Set NBT data
        itemMeta.getPersistentDataContainer().set(keyPlugin, PersistentDataType.STRING, plugin.getName());
        itemMeta.getPersistentDataContainer().set(keyOwner, PersistentDataType.STRING, player.getUniqueId().toString());
        itemMeta.getPersistentDataContainer().set(keyAmount, PersistentDataType.DOUBLE, amount);

        // Apply lore and NBT to item
        itemStack.setItemMeta(itemMeta);
        return itemStack;
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
