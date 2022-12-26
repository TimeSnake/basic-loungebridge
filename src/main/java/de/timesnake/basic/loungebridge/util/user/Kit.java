/*
 * Copyright (C) 2022 timesnake
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
