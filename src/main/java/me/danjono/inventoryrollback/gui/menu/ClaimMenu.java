package me.danjono.inventoryrollback.gui.menu;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import me.danjono.inventoryrollback.data.PlayerData;
import me.danjono.inventoryrollback.gui.Buttons;
import me.danjono.inventoryrollback.gui.InventoryName;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import me.danjono.inventoryrollback.data.YAML;
import me.danjono.inventoryrollback.data.LogType;
import me.danjono.inventoryrollback.reflections.NBTWrapper;

public class ClaimMenu {
    private static final int ITEMS_PER_PAGE = InventoryName.CLAIM_BACKUP.getSize() - 9;

    private final Player player;
    private final List<YAML> backups;
    private int currentPage;
    private Inventory inventory;
    private int pagesRequired;
    private SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm");

    public ClaimMenu(Player player, int currentPage) {
        this.player = player;
        this.backups = YAML.getBackupDataByUUID(player.getUniqueId());

        this.currentPage = Math.max(1, currentPage);
        calculatePagination();
        buildInventory();
    }

    private void calculatePagination() {
        int totalBackups = backups.size();
        pagesRequired = (int) Math.ceil(totalBackups / (double) ITEMS_PER_PAGE);
        currentPage = Math.min(Math.max(currentPage, 1), pagesRequired);
    }

    private void buildInventory() {
        inventory = Bukkit.createInventory(player, InventoryName.CLAIM_BACKUP.getSize(),
                InventoryName.CLAIM_BACKUP.getName());

        // Fill last row with white stained glass
        ItemStack filler = createFillerGlass();
        int inventorySize = InventoryName.CLAIM_BACKUP.getSize();
        int lastRowStart = inventorySize - 9;

        for (int i = lastRowStart; i < inventorySize; i++) {
            inventory.setItem(i, filler);
        }

        // Add backup items
        int startIndex = ITEMS_PER_PAGE * (currentPage - 1);
        for (int i = 0; i < ITEMS_PER_PAGE && startIndex + i < backups.size(); i++) {
            YAML backup = backups.get(startIndex + i);
            inventory.setItem(i, createBackupItem(backup));
        }

        // Add navigation buttons
        addNavigationButtons();
    }

    private ItemStack createFillerGlass() {
        ItemStack glass = new ItemStack(Material.WHITE_STAINED_GLASS_PANE);
        ItemMeta meta = glass.getItemMeta();
        meta.setDisplayName(" ");
        glass.setItemMeta(meta);
        return glass;
    }

    private ItemStack createBackupItem(YAML backup) {
        boolean canBeClaimed = backup.isApproved && !backup.isClaimed;

        Material material = canBeClaimed ? Material.GREEN_TERRACOTTA : Material.RED_TERRACOTTA;
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        String statusPrefix = canBeClaimed ? "§a§lApproved" : "§c§lUnavailable";
        meta.setDisplayName(statusPrefix + " Backup §7(" + sdf.format(backup.getTimestamp()) + ")");

        List<String> lore = new ArrayList<>();
        if (canBeClaimed) {
            lore.add("§eStatus: §a✔ Ready to Claim");
            lore.add("§aClick to restore items!");
            lore.add("§7Includes inventory and equipment");
        } else {
            if (backup.isClaimed) {
                lore.add("§eStatus: §c✖ Already Claimed");
                lore.add("§7This backup has already been restored.");
                lore.add("§eClaimed On: §7" + sdf.format(backup.ClaimedIn));
            } else if (!backup.isApproved) {
                lore.add("§eStatus: §c✖ Not Approved");
                lore.add("§7Reason: Awaiting administrative approval");
            }
        }

        lore.add("§eLog Type: §a" + backup.getLogType());
        meta.setLore(lore);
        item.setItemMeta(meta);

        // Add NBT
        NBTWrapper nbt = new NBTWrapper(item);
        nbt.setString("uuid", backup.getUUID().toString());
        nbt.setLong("timestamp", backup.getTimestamp());
        nbt.setString("logType", backup.getLogType().name());
        nbt.setInt("backup", 1);
        nbt.setInt("IsClaimed", backup.isClaimed ? 1 : 0);
        nbt.setInt("IsApproved", backup.isApproved ? 1 : 0);
        nbt.setLong("ClaimedIn", backup.ClaimedIn);

        return nbt.setItemData();
    }


    private void addNavigationButtons() {
        UUID uuid = player.getUniqueId();
        int inventorySize = InventoryName.CLAIM_BACKUP.getSize();

        // Previous Page Button
        if (currentPage > 1) {
            ItemStack prevButton = new Buttons(uuid).backButton(
                    "§aPrevious Page",
                    LogType.CLAIM,
                    currentPage - 1,
                    List.of("§7Go to the previous page")
            );
            inventory.setItem(inventorySize - 8, prevButton);
        }

        // Next Page Button
        if (currentPage < pagesRequired) {
            ItemStack nextButton = new Buttons(uuid).nextButton(
                    "§aNext Page",
                    LogType.CLAIM,
                    currentPage + 1,
                    List.of("§7Go to the next page")
            );
            inventory.setItem(inventorySize - 2, nextButton);
        }
    }

    public Inventory getInventory() {
        return inventory;
    }
}