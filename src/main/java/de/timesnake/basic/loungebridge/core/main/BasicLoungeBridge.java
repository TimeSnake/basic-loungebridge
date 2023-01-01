/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.basic.loungebridge.core.main;

import org.bukkit.plugin.java.JavaPlugin;

public class BasicLoungeBridge extends JavaPlugin {

    public static BasicLoungeBridge getPlugin() {
        return plugin;
    }

    private static BasicLoungeBridge plugin;

    @Override
    public void onEnable() {
        plugin = this;
    }
}
