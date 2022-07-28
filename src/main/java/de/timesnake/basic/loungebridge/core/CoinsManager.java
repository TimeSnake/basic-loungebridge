package de.timesnake.basic.loungebridge.core;

import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.chat.Argument;
import de.timesnake.basic.bukkit.util.chat.CommandListener;
import de.timesnake.basic.bukkit.util.chat.Sender;
import de.timesnake.basic.bukkit.util.user.User;
import de.timesnake.basic.loungebridge.core.main.BasicLoungeBridge;
import de.timesnake.basic.loungebridge.util.chat.Plugin;
import de.timesnake.basic.loungebridge.util.server.LoungeBridgeServer;
import de.timesnake.basic.loungebridge.util.user.GameUser;
import de.timesnake.library.basic.util.chat.ChatColor;
import de.timesnake.library.extension.util.cmd.Arguments;
import de.timesnake.library.extension.util.cmd.ExCommand;
import net.md_5.bungee.api.chat.ClickEvent;

import java.util.List;

public class CoinsManager {

    private boolean saveCoins;

    public CoinsManager() {
        this.reset();

        Server.getCommandManager().addCommand(BasicLoungeBridge.getPlugin(), "coins_discard",
                new CoinsDiscardCmd(), Plugin.GAME);
    }

    public void reset() {
        this.saveCoins = true;
    }

    public boolean isSaveCoins() {
        return saveCoins;
    }

    public void setSaveCoins(boolean saveCoins) {
        this.saveCoins = saveCoins;
    }

    public void sendCoinsSaveRequest() {
        for (User user : Server.getUsers()) {
            if (user.hasPermission("game.coins.discard")) {
                user.sendClickablePluginMessage(Plugin.GAME, ChatColor.WARNING + ChatColor.UNDERLINE +
                                "Discard game" + ChatColor.GOLD + " coins?", "/coins_discard",
                        "Click to discard all coins", ClickEvent.Action.RUN_COMMAND);
            }
        }
    }

    public void saveGameCoins() {
        if (this.isSaveCoins()) {
            Server.printText(Plugin.GAME, "Saved game coins", "Coins");
            for (User user : LoungeBridgeServer.getGameUsers()) {
                if (user.hasPermission("game.coins.info")) {
                    user.sendPluginMessage(Plugin.GAME, ChatColor.WARNING + "Saved all coins");
                }
            }
        } else {
            Server.printText(Plugin.GAME, "Discarded game coins", "Coins");
            for (User user : LoungeBridgeServer.getGameUsers()) {
                user.removeCoins(((GameUser) user).getGameCoins(), false);
                if (user.hasPermission("game.coins.info")) {
                    user.sendPluginMessage(Plugin.GAME, ChatColor.WARNING + "Discarded all coins");
                }
            }
        }

    }

    public static class CoinsDiscardCmd implements CommandListener {

        @Override
        public void onCommand(Sender sender, ExCommand<Sender, Argument> cmd, Arguments<Argument> args) {
            if (!sender.hasPermission("game.coins.discard", 1506)) {
                return;
            }

            LoungeBridgeServer.getStatsManager().setSaveStats(!LoungeBridgeServer.getStatsManager().isSaveStats());

            if (LoungeBridgeServer.getStatsManager().isSaveStats()) {
                sender.sendPluginMessage(ChatColor.PERSONAL + "All coins will be saved");
            } else {
                sender.sendPluginMessage(ChatColor.PERSONAL + "All coins will be discarded");
            }
        }

        @Override
        public List<String> getTabCompletion(ExCommand<Sender, Argument> cmd, Arguments<Argument> args) {
            return List.of();
        }
    }
}
