package com.nuclyon.technicallycoded.inventoryrollback.commands.inventoryrollback;

import com.nuclyon.technicallycoded.inventoryrollback.InventoryRollbackPlus;
import com.nuclyon.technicallycoded.inventoryrollback.commands.IRPCommand;
import me.danjono.inventoryrollback.InventoryRollback;
import me.danjono.inventoryrollback.config.MessageData;
import me.danjono.inventoryrollback.gui.menu.ClaimMenu;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ClaimSubCmd extends IRPCommand {

    public ClaimSubCmd(InventoryRollbackPlus mainIn) {
        super(mainIn);
    }

    @Override
    public void onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender instanceof Player) {
            if (sender.hasPermission("inventoryrollbackplus.claim")) {
                Player player = (Player) sender;
                openClaimMenu(player);
            } else {
                sender.sendMessage(MessageData.getPluginPrefix() + MessageData.getNoPermission());
            }
        } else {
            sender.sendMessage(MessageData.getPluginPrefix() + MessageData.getPlayerOnlyError());
        }
    }

    private void openClaimMenu(Player player) {
            ClaimMenu menu = new ClaimMenu(player, 1);
            player.openInventory(menu.getInventory());
    }
}
