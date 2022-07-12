package de.timesnake.basic.loungebridge.util.user;

import de.timesnake.basic.game.util.Team;
import de.timesnake.library.basic.util.Status;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class OfflineUser {

    private final UUID uuid;

    private final ItemStack[] inventoryItems;
    private final float exp;
    private final int level;

    private final Team team;
    private final Kit kit;

    private final Status.User status;

    public OfflineUser(GameUser user) {
        this.uuid = user.getUniqueId();
        this.inventoryItems = user.getInventory().getContents();
        this.exp = user.getPlayer().getExp();
        this.level = user.getPlayer().getLevel();
        this.team = user.getTeam();
        this.status = user.getStatus();
        this.kit = user.getKit();
    }

    public UUID getUniqueId() {
        return uuid;
    }

    public ItemStack[] getInventoryItems() {
        return inventoryItems;
    }

    public float getExp() {
        return exp;
    }

    public int getLevel() {
        return level;
    }

    public Team getTeam() {
        return team;
    }

    public Kit getKit() {
        return kit;
    }

    public void loadInto(GameUser user) {
        user.getInventory().setContents(this.inventoryItems);
        user.setExp(this.exp);
        user.setLevel(this.level);
        user.setTeam(this.team);
        user.setStatus(this.status);
        user.setKit(this.kit);
    }
}
