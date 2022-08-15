package de.timesnake.basic.loungebridge.core;

import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.chat.Argument;
import de.timesnake.basic.bukkit.util.chat.CommandListener;
import de.timesnake.basic.bukkit.util.chat.Sender;
import de.timesnake.basic.bukkit.util.user.User;
import de.timesnake.basic.loungebridge.core.main.BasicLoungeBridge;
import de.timesnake.basic.loungebridge.util.chat.Plugin;
import de.timesnake.basic.loungebridge.util.server.LoungeBridgeServer;
import de.timesnake.basic.loungebridge.util.tool.PreCloseableTool;
import de.timesnake.basic.loungebridge.util.tool.ResetableTool;
import de.timesnake.basic.loungebridge.util.tool.StopableTool;
import de.timesnake.basic.loungebridge.util.user.GameUser;
import de.timesnake.library.basic.util.chat.ExTextColor;
import de.timesnake.library.extension.util.cmd.Arguments;
import de.timesnake.library.extension.util.cmd.ExCommand;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;

import java.util.List;

public class CoinsManager implements ResetableTool, PreCloseableTool, StopableTool {

    private boolean saveCoins;

    public CoinsManager() {
        this.reset();

        Server.getCommandManager().addCommand(BasicLoungeBridge.getPlugin(), "coins_discard",
                new CoinsDiscardCmd(), Plugin.GAME);
    }

    @Override
    public void reset() {
        this.saveCoins = true;
    }

    @Override
    public void stop() {
        this.sendCoinsSaveRequest();
    }

    @Override
    public void preClose() {
        this.saveGameCoins();
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
                user.sendClickablePluginMessage(Plugin.GAME, Component.text("Discard game", ExTextColor.WARNING)
                                .append(Component.text(" coins?", ExTextColor.GOLD)), "/coins_discard",
                        Component.text("Click to discard all coins"), ClickEvent.Action.RUN_COMMAND);
            }
        }
    }

    public void saveGameCoins() {
        if (this.isSaveCoins()) {
            Server.printText(Plugin.GAME, "Saved game coins", "Coins");
            for (User user : LoungeBridgeServer.getGameUsers()) {
                if (user.hasPermission("game.coins.info")) {
                    user.sendPluginMessage(Plugin.GAME, Component.text("Saved all coins", ExTextColor.WARNING));
                }
            }
        } else {
            Server.printText(Plugin.GAME, "Discarded game coins", "Coins");
            for (User user : LoungeBridgeServer.getGameUsers()) {
                user.removeCoins(((GameUser) user).getGameCoins(), false);
                if (user.hasPermission("game.coins.info")) {
                    user.sendPluginMessage(Plugin.GAME, Component.text("Discarded all coins", ExTextColor.WARNING));
                }
            }
        }

    }

    public class CoinsDiscardCmd implements CommandListener {

        @Override
        public void onCommand(Sender sender, ExCommand<Sender, Argument> cmd, Arguments<Argument> args) {
            if (!sender.hasPermission("game.coins.discard", 1506)) {
                return;
            }

            CoinsManager.this.saveCoins = false;
            sender.sendPluginMessage(Component.text("All coins will be discarded", ExTextColor.PERSONAL));
        }

        @Override
        public List<String> getTabCompletion(ExCommand<Sender, Argument> cmd, Arguments<Argument> args) {
            return List.of();
        }
    }
}
