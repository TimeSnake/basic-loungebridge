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
import de.timesnake.basic.loungebridge.util.user.GameUser;
import de.timesnake.channel.util.message.ChannelServerMessage;
import de.timesnake.channel.util.message.MessageType;
import de.timesnake.database.util.game.DbGame;
import de.timesnake.library.basic.util.chat.ChatColor;
import de.timesnake.library.basic.util.statistics.StatType;
import de.timesnake.library.extension.util.cmd.Arguments;
import de.timesnake.library.extension.util.cmd.ExCommand;
import net.md_5.bungee.api.chat.ClickEvent;

import java.util.List;
import java.util.Set;

public class StatsManager {

    public static final Set<StatType<?>> BASE_STATS = Set.of(TempGameServerManager.GAMES_PLAYED);

    private boolean saveStats;

    public StatsManager() {
        this.reset();

        Server.getCommandManager().addCommand(BasicLoungeBridge.getPlugin(), "stats_discard",
                new StatsDiscardCmd(), Plugin.GAME);
    }

    public void reset() {
        this.saveStats = true;
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
                user.sendClickablePluginMessage(Plugin.GAME, ChatColor.WARNING + ChatColor.UNDERLINE +
                                "Discard game stats?", "/stats_discard", "Click to discard all stats",
                        ClickEvent.Action.RUN_COMMAND);
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
                    user.sendPluginMessage(Plugin.GAME, ChatColor.WARNING + "Saved all stats");
                }
            }

            Server.getChannel().sendMessage(new ChannelServerMessage<>(Server.getPort(), MessageType.Server.USER_STATS,
                    LoungeBridgeServer.getGame().getName()));
        } else {
            for (User user : LoungeBridgeServer.getGameUsers()) {
                if (user.hasPermission("game.stats.info")) {
                    user.sendPluginMessage(Plugin.GAME, ChatColor.WARNING + "Discarded all stats");
                }
            }

            Server.printText(Plugin.GAME, "Discarded game stats", "Stats");
        }

    }

    public static class StatsDiscardCmd implements CommandListener {

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
}
