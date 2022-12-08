/*
 * workspace.basic-loungebridge.main
 * Copyright (C) 2022 timesnake
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; If not, see <http://www.gnu.org/licenses/>.
 */

package de.timesnake.basic.loungebridge.util.user;

import de.timesnake.basic.game.util.game.Team;
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
