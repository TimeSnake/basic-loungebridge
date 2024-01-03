/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.basic.loungebridge.core;

import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.chat.cmd.Argument;
import de.timesnake.basic.bukkit.util.chat.cmd.CommandListener;
import de.timesnake.basic.bukkit.util.chat.cmd.Completion;
import de.timesnake.basic.bukkit.util.chat.cmd.Sender;
import de.timesnake.basic.bukkit.util.user.User;
import de.timesnake.basic.game.util.user.Plugin;
import de.timesnake.basic.loungebridge.core.main.BasicLoungeBridge;
import de.timesnake.basic.loungebridge.util.server.LoungeBridgeServer;
import de.timesnake.basic.loungebridge.util.tool.scheduler.PreCloseableTool;
import de.timesnake.basic.loungebridge.util.tool.scheduler.ResetableTool;
import de.timesnake.basic.loungebridge.util.tool.scheduler.StopableTool;
import de.timesnake.basic.loungebridge.util.user.GameUser;
import de.timesnake.library.basic.util.Loggers;
import de.timesnake.library.chat.ExTextColor;
import de.timesnake.library.commands.PluginCommand;
import de.timesnake.library.commands.simple.Arguments;
import de.timesnake.library.extension.util.chat.Code;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;

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
        user.sendClickablePluginMessage(Plugin.GAME,
            Component.text("Discard game", ExTextColor.WARNING)
                .append(Component.text(" coins?", ExTextColor.GOLD)),
            "/coins_discard",
            Component.text("Click to discard all coins"),
            ClickEvent.Action.RUN_COMMAND);
      }
    }
  }

  public void saveGameCoins() {
    if (this.isSaveCoins()) {
      Loggers.LOUNGE_BRIDGE.info("Saved game coins");
      for (User user : LoungeBridgeServer.getGameUsers()) {
        if (user.hasPermission("game.coins.info")) {
          user.sendPluginMessage(Plugin.GAME,
              Component.text("Saved all coins", ExTextColor.WARNING));
        }
      }
    } else {
      Loggers.LOUNGE_BRIDGE.info("Discarded game coins");
      for (User user : LoungeBridgeServer.getGameUsers()) {
        user.removeCoins(((GameUser) user).getGameCoins(), false);
        if (user.hasPermission("game.coins.info")) {
          user.sendPluginMessage(Plugin.GAME,
              Component.text("Discarded all coins", ExTextColor.WARNING));
        }
      }
    }

  }

  public class CoinsDiscardCmd implements CommandListener {

    private final Code perm = Plugin.GAME.createPermssionCode("game.coins.discard");

    @Override
    public void onCommand(Sender sender, PluginCommand cmd, Arguments<Argument> args) {
      sender.hasPermissionElseExit(this.perm);
      CoinsManager.this.saveCoins = false;
      sender.sendPluginTDMessage("§cAll coins will be discarded");
    }

    @Override
    public Completion getTabCompletion() {
      return new Completion(this.perm);
    }

    @Override
    public String getPermission() {
      return this.perm.getPermission();
    }
  }
}
