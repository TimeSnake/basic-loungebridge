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

import de.timesnake.database.util.game.DbKit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class Kit extends de.timesnake.basic.game.util.game.Kit {

    private final List<ItemStack> items;

    public Kit(DbKit kit, List<ItemStack> items) {
        super(kit);
        this.items = items;
    }

    public Kit(Integer id, String name, Material material, List<String> description, List<ItemStack> items) {
        super(id, name, material, description);
        this.items = items;
    }

    public List<ItemStack> getItems() {
        return items;
    }


}
