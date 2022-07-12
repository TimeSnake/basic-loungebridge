package de.timesnake.basic.loungebridge.core.main;

import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.loungebridge.core.StatsDiscardCmd;
import de.timesnake.basic.loungebridge.util.chat.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public class BasicLoungeBridge extends JavaPlugin {

    public static BasicLoungeBridge getPlugin() {
        return plugin;
    }

    private static BasicLoungeBridge plugin;

    @Override
    public void onEnable() {
        plugin = this;

        Server.getCommandManager().addCommand(this, "stats_discard", new StatsDiscardCmd(), Plugin.GAME);
    }
}
