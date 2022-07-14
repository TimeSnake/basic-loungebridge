package de.timesnake.basic.loungebridge.core;

import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.user.User;
import de.timesnake.basic.loungebridge.util.chat.Plugin;
import de.timesnake.basic.loungebridge.util.server.LoungeBridgeServer;
import de.timesnake.basic.loungebridge.util.server.TempGameServerManager;
import de.timesnake.basic.loungebridge.util.user.GameUser;
import de.timesnake.channel.util.message.ChannelServerMessage;
import de.timesnake.channel.util.message.MessageType;
import de.timesnake.database.util.game.DbGame;
import de.timesnake.library.basic.util.chat.ChatColor;
import de.timesnake.library.basic.util.statistics.StatType;
import net.md_5.bungee.api.chat.ClickEvent;

import java.util.Set;

public class StatsManager {

    public static final Set<StatType<?>> BASE_STATS = Set.of(TempGameServerManager.GAMES_PLAYED);

    private boolean saveStats;

    public StatsManager() {
        this.reset();
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
                    System.out.println(user.getName());
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
}
