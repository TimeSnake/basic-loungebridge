package de.timesnake.basic.loungebridge.core;

import de.timesnake.basic.bukkit.util.chat.Argument;
import de.timesnake.basic.bukkit.util.chat.CommandListener;
import de.timesnake.basic.bukkit.util.chat.Sender;
import de.timesnake.basic.loungebridge.util.server.LoungeBridgeServer;
import de.timesnake.library.basic.util.chat.ChatColor;
import de.timesnake.library.extension.util.cmd.Arguments;
import de.timesnake.library.extension.util.cmd.ExCommand;

import java.util.List;

public class StatsDiscardCmd implements CommandListener {

    @Override
    public void onCommand(Sender sender, ExCommand<Sender, Argument> cmd, Arguments<Argument> args) {
        if (!sender.hasPermission("game.stats.discard", 1504)) {
            return;
        }

        LoungeBridgeServer.getStatsManager().setSaveStats(!LoungeBridgeServer.getStatsManager().isSaveStats());

        if (LoungeBridgeServer.getStatsManager().isSaveStats()) {
            sender.sendPluginMessage(ChatColor.PERSONAL + "All stats will be saved");
        } else {
            sender.sendPluginMessage(ChatColor.PERSONAL + "All stats will be discarded");
        }
    }

    @Override
    public List<String> getTabCompletion(ExCommand<Sender, Argument> cmd, Arguments<Argument> args) {
        return List.of();
    }
}
