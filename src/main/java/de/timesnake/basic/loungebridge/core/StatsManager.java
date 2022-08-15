package de.timesnake.basic.loungebridge.core;

import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.chat.Argument;
import de.timesnake.basic.bukkit.util.chat.CommandListener;
import de.timesnake.basic.bukkit.util.chat.Sender;
import de.timesnake.basic.bukkit.util.user.User;
import de.timesnake.basic.loungebridge.core.main.BasicLoungeBridge;
import de.timesnake.basic.loungebridge.util.chat.Plugin;
import de.timesnake.basic.loungebridge.util.server.LoungeBridgeServer;
import de.timesnake.basic.loungebridge.util.server.TempGameServerManager;
import de.timesnake.basic.loungebridge.util.tool.GameTool;
import de.timesnake.basic.loungebridge.util.tool.PreCloseableTool;
import de.timesnake.basic.loungebridge.util.tool.ResetableTool;
import de.timesnake.basic.loungebridge.util.tool.StopableTool;
import de.timesnake.basic.loungebridge.util.user.GameUser;
import de.timesnake.channel.util.message.ChannelServerMessage;
import de.timesnake.channel.util.message.MessageType;
import de.timesnake.database.util.game.DbGame;
import de.timesnake.library.basic.util.chat.ExTextColor;
import de.timesnake.library.basic.util.statistics.StatType;
import de.timesnake.library.extension.util.cmd.Arguments;
import de.timesnake.library.extension.util.cmd.ExCommand;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;

import java.util.List;
import java.util.Set;

public class StatsManager implements GameTool, ResetableTool, PreCloseableTool, StopableTool {

    public static final Set<StatType<?>> BASE_STATS = Set.of(TempGameServerManager.GAMES_PLAYED);

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
                user.sendClickablePluginMessage(Plugin.GAME, Component.text("Discard game ", ExTextColor.WARNING)
                                .append(Component.text("stats?", ExTextColor.BLUE)), "/stats_discard",
                        Component.text("Click to discard all stats"), ClickEvent.Action.RUN_COMMAND);
            }
        }
    }

    public void saveGameStats() {
        if (this.isSaveStats()) {
            Server.printText(Plugin.GAME, "Saved game stats", "Stats");
            for (User user : LoungeBridgeServer.getGameUsers()) {
                if (((GameUser) user).hasPlayedGame()) {
                    LoungeBridgeServer.saveGameUserStats(((GameUser) user));
                }

                if (user.hasPermission("game.stats.info")) {
                    user.sendPluginMessage(Plugin.GAME, Component.text("Saved all stats", ExTextColor.WARNING));
                }
            }

            Server.getChannel().sendMessage(new ChannelServerMessage<>(Server.getPort(), MessageType.Server.USER_STATS,
                    LoungeBridgeServer.getGame().getName()));
        } else {
            for (User user : LoungeBridgeServer.getGameUsers()) {
                if (user.hasPermission("game.stats.info")) {
                    user.sendPluginMessage(Plugin.GAME, Component.text("Discarded all stats", ExTextColor.WARNING));
                }
            }

            Server.printText(Plugin.GAME, "Discarded game stats", "Stats");
        }

    }

    public class StatsDiscardCmd implements CommandListener {

        @Override
        public void onCommand(Sender sender, ExCommand<Sender, Argument> cmd, Arguments<Argument> args) {
            if (!sender.hasPermission("game.stats.discard", 1504)) {
                return;
            }

            StatsManager.this.saveStats = false;
            sender.sendPluginMessage(Component.text("All stats will be discarded", ExTextColor.PERSONAL));
        }

        @Override
        public List<String> getTabCompletion(ExCommand<Sender, Argument> cmd, Arguments<Argument> args) {
            return List.of();
        }
    }
}
