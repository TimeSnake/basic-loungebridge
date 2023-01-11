/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.basic.loungebridge.util.server;

import static de.timesnake.basic.loungebridge.util.server.LoungeBridgeServerManager.SPECTATOR_NAME;
import static de.timesnake.basic.loungebridge.util.server.LoungeBridgeServerManager.SPECTATOR_TABLIST_CHAT_COLOR;
import static de.timesnake.basic.loungebridge.util.server.LoungeBridgeServerManager.SPECTATOR_TABLIST_PREFIX;
import static de.timesnake.basic.loungebridge.util.server.LoungeBridgeServerManager.SPECTATOR_TABLIST_PREFIX_CHAT_COLOR;

import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.chat.ChatColor;
import de.timesnake.basic.bukkit.util.group.DisplayGroup;
import de.timesnake.basic.bukkit.util.user.scoreboard.NameTagVisibility;
import de.timesnake.basic.bukkit.util.user.scoreboard.Tablist;
import de.timesnake.basic.bukkit.util.user.scoreboard.TablistGroupType;
import de.timesnake.basic.bukkit.util.user.scoreboard.TablistableGroup;
import de.timesnake.basic.bukkit.util.user.scoreboard.TablistablePlayer;
import de.timesnake.basic.bukkit.util.user.scoreboard.TeamTablist;
import de.timesnake.basic.bukkit.util.user.scoreboard.TeamTablistBuilder;
import de.timesnake.basic.game.util.game.Map;
import de.timesnake.basic.game.util.game.Team;
import de.timesnake.basic.loungebridge.util.user.TablistTeam;
import de.timesnake.library.basic.util.Status;
import java.util.LinkedList;
import java.util.List;

public class TablistManager {

    protected TeamTablist gameTablist;

    protected TablistTeam tablistGameTeam;
    protected TablistTeam spectatorTeam;

    public void loadTablist(Tablist.Type type) {
        this.spectatorTeam = new TablistTeam("0", SPECTATOR_NAME, SPECTATOR_TABLIST_PREFIX,
                SPECTATOR_TABLIST_CHAT_COLOR, SPECTATOR_TABLIST_PREFIX_CHAT_COLOR) {
            @Override
            public NameTagVisibility isNameTagVisibleBy(TablistablePlayer player,
                    TablistableGroup otherGroup) {
                if (otherGroup.equals(this)) {
                    return NameTagVisibility.ALWAYS;
                }
                return NameTagVisibility.NEVER;
            }

            @Override
            public NameTagVisibility isNameTagVisible(TablistablePlayer player) {
                return NameTagVisibility.NEVER;
            }
        };

        TeamTablistBuilder builder = new TeamTablistBuilder("game")
                .type(type)
                .groupTypes(DisplayGroup.MAIN_TABLIST_GROUPS)
                .remainTeam(this.spectatorTeam)
                .userJoin((e, tablist) -> {
                    if (e.getUser().getTask() != null
                            && e.getUser().getTask()
                            .equalsIgnoreCase(LoungeBridgeServer.getGame().getName())
                            && (e.getUser().getStatus().equals(Status.User.PRE_GAME)
                            || e.getUser().getStatus().equals(Status.User.IN_GAME))) {
                        tablist.addEntry(e.getUser());
                    } else {
                        ((TeamTablist) tablist).addRemainEntry(e.getUser());
                    }
                })
                .userQuit((e, tablist) -> tablist.removeEntry(e.getUser()));

        if (LoungeBridgeServer.getServerTeamAmount() > 0 && !LoungeBridgeServer.getGame()
                .hideTeams()) {
            if (LoungeBridgeServer.getMaxPlayersPerTeam() == null) {
                this.gameTablist = Server.getScoreboardManager().registerTagTeamTablist(builder
                        .colorType(TeamTablist.ColorType.TEAM)
                        .teams(LoungeBridgeServer.getGame().getTeams())
                        .teamType(de.timesnake.basic.game.util.game.TablistGroupType.GAME_TEAM));

                for (Team team : LoungeBridgeServer.getGame()
                        .getTeamsSortedByRank(LoungeBridgeServer.getServerTeamAmount())) {
                    this.gameTablist.addTeamHeader(team.getTablistRank(), "0",
                            team.getTablistChatColor() + "§l" + team.getTablistName());
                }
            } else {
                LinkedList<TablistGroupType> gameTeamTypes = new LinkedList<>(
                        DisplayGroup.MAIN_TABLIST_GROUPS);
                gameTeamTypes.addFirst(
                        de.timesnake.basic.game.util.game.TablistGroupType.GAME_TEAM);
                this.tablistGameTeam = this.loadGameTeam();

                this.gameTablist = Server.getScoreboardManager().registerTagTeamTablist(builder
                        .colorType(TeamTablist.ColorType.FIRST_GROUP)
                        .teams(List.of(this.tablistGameTeam))
                        .teamType(this.tablistGameTeam.getTeamType())
                        .groupTypes(gameTeamTypes));
            }
        } else {
            this.tablistGameTeam = this.loadGameTeam();

            this.gameTablist = Server.getScoreboardManager().registerTagTeamTablist(builder
                    .colorType(TeamTablist.ColorType.WHITE)
                    .teams(List.of(this.tablistGameTeam))
                    .teamType(this.tablistGameTeam.getTeamType()));
        }

        this.gameTablist.setHeader("§6" + LoungeBridgeServer.getGame().getDisplayName());
        this.gameTablist.setFooter(
                "§7Server: " + Server.getName() + "\n§cSupport: /ticket or \nsupport@timesnake.de");
    }

    public void updateMapFooter(Map map) {
        if (this.gameTablist != null) {
            if (map != null) {
                StringBuilder authors = new StringBuilder();
                for (String author : map.getAuthors(18)) {
                    if (authors.length() != 0) {
                        authors.append("\n");
                    }
                    authors.append(author);

                }

                this.gameTablist.setHeader(
                        ChatColor.GOLD + LoungeBridgeServer.getGame().getDisplayName()
                                + ChatColor.GRAY + ": " +
                                ChatColor.DARK_GREEN + map.getDisplayName() +
                                (!authors.isEmpty() ? "\n" + ChatColor.GRAY + " by "
                                        + ChatColor.BLUE + authors : ""));
            } else {
                this.gameTablist.setHeader(
                        ChatColor.GOLD + LoungeBridgeServer.getGame().getDisplayName());
            }
        }
    }

    protected TablistTeam loadGameTeam() {
        return new TablistTeam("0", "game", "", ChatColor.WHITE, ChatColor.WHITE);
    }

    public TablistTeam getTablistGameTeam() {
        return tablistGameTeam;
    }

    public TablistTeam getSpectatorTeam() {
        return spectatorTeam;
    }

    public TeamTablist getGameTablist() {
        return gameTablist;
    }
}
