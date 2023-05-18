/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.basic.loungebridge.core;

import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.chat.Argument;
import de.timesnake.basic.bukkit.util.chat.CommandListener;
import de.timesnake.basic.bukkit.util.chat.Sender;
import de.timesnake.basic.bukkit.util.user.User;
import de.timesnake.basic.game.util.user.Plugin;
import de.timesnake.basic.loungebridge.core.main.BasicLoungeBridge;
import de.timesnake.basic.loungebridge.util.server.LoungeBridgeServer;
import de.timesnake.basic.loungebridge.util.server.TmpGameServerManager;
import de.timesnake.basic.loungebridge.util.tool.GameTool;
import de.timesnake.basic.loungebridge.util.tool.scheduler.PreCloseableTool;
import de.timesnake.basic.loungebridge.util.tool.scheduler.ResetableTool;
import de.timesnake.basic.loungebridge.util.tool.scheduler.StopableTool;
import de.timesnake.basic.loungebridge.util.user.GameUser;
import de.timesnake.channel.util.message.ChannelServerMessage;
import de.timesnake.channel.util.message.MessageType;
import de.timesnake.database.util.game.DbGame;
import de.timesnake.library.basic.util.Loggers;
import de.timesnake.library.basic.util.statistics.StatType;
import de.timesnake.library.chat.ExTextColor;
import de.timesnake.library.extension.util.chat.Code;
import de.timesnake.library.extension.util.cmd.Arguments;
import de.timesnake.library.extension.util.cmd.ExCommand;
import java.util.List;
import java.util.Set;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;

public class StatsManager implements GameTool, ResetableTool, PreCloseableTool, StopableTool {

  public static final Set<StatType<?>> BASE_STATS = Set.of(TmpGameServerManager.GAMES_PLAYED);

  private boolean saveStats;

  public StatsManager() {
    this.reset();

    Server.getCommandManager().addCommand(BasicLoungeBridge.getPlugin(), "stats_discard",
        new StatsDiscardCmd(), Plugin.GAME);
  }

  @Override
  public void reset() {
    this.saveStats = true;
  }

  @Override
  public void preClose() {
    this.saveGameStats();
  }

  @Override
  public void stop() {
    this.sendStatSaveRequest();
  }

  public boolean isSaveStats() {
    return saveStats;
  }

  public void setSaveStats(boolean saveStats) {
    this.saveStats = saveStats;
  }

  public void loadStatTypesIntoDatabase() {
    if (LoungeBridgeServer.getStats() == null) {
      return;
    }

    DbGame game = LoungeBridgeServer.getGame().getDatabase();

    for (StatType<?> stat : BASE_STATS) {
      game.removeStat(stat);
      game.addStat(stat);
    }

    for (StatType<?> stat : LoungeBridgeServer.getStats()) {
      game.removeStat(stat);
      game.addStat(stat);
    }
  }

  public void sendStatSaveRequest() {
    for (User user : Server.getUsers()) {
      if (user.hasPermission("game.stats.discard")) {
        user.sendClickablePluginMessage(Plugin.GAME,
            Component.text("Discard game ", ExTextColor.WARNING)
                .append(Component.text("stats?", ExTextColor.BLUE)),
            "/stats_discard",
            Component.text("Click to discard all stats"),
            ClickEvent.Action.RUN_COMMAND);
      }
    }
  }

  public void saveGameStats() {
    if (this.isSaveStats()) {
      Loggers.LOUNGE_BRIDGE.info("Saved game stats");
      for (User user : LoungeBridgeServer.getGameUsers()) {
        if (((GameUser) user).hasPlayedGame()) {
          LoungeBridgeServer.saveGameUserStats(((GameUser) user));
        }

        if (user.hasPermission("game.stats.info")) {
          user.sendPluginMessage(Plugin.GAME,
              Component.text("Saved all stats", ExTextColor.WARNING));
        }
      }

      Server.getChannel().sendMessage(
          new ChannelServerMessage<>(Server.getName(), MessageType.Server.USER_STATS,
              LoungeBridgeServer.getGame().getName()));
    } else {
      for (User user : LoungeBridgeServer.getGameUsers()) {
        if (user.hasPermission("game.stats.info")) {
          user.sendPluginMessage(Plugin.GAME,
              Component.text("Discarded all stats", ExTextColor.WARNING));
        }
      }

      Loggers.LOUNGE_BRIDGE.info("Discarded game stats");
    }

  }

  public class StatsDiscardCmd implements CommandListener {

    private Code statsDiscardPerm;

    @Override
    public void onCommand(Sender sender, ExCommand<Sender, Argument> cmd,
        Arguments<Argument> args) {
      if (!sender.hasPermission(this.statsDiscardPerm)) {
        return;
      }

      StatsManager.this.saveStats = false;
      sender.sendPluginMessage(
          Component.text("All stats will be discarded", ExTextColor.PERSONAL));
    }

    @Override
    public List<String> getTabCompletion(ExCommand<Sender, Argument> cmd,
        Arguments<Argument> args) {
      return List.of();
    }

    @Override
    public void loadCodes(de.timesnake.library.extension.util.chat.Plugin plugin) {
      this.statsDiscardPerm = plugin.createPermssionCode("game.stats.discard");
    }
  }
}
