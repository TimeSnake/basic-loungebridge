/*
 * Copyright (C) 2022 timesnake
 */

package de.timesnake.basic.loungebridge.util.server;

import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.chat.ChatColor;
import de.timesnake.basic.bukkit.util.group.DisplayGroup;
import de.timesnake.basic.bukkit.util.user.scoreboard.*;
import de.timesnake.basic.game.util.game.Map;
import de.timesnake.basic.game.util.game.Team;
import de.timesnake.basic.loungebridge.util.user.TablistTeam;
import de.timesnake.library.basic.util.Status;

import java.util.LinkedList;
import java.util.List;

import static de.timesnake.basic.loungebridge.util.server.LoungeBridgeServerManager.*;

public class TablistManager {
    protected TeamTablist gameTablist;

    protected TablistTeam tablistGameTeam;
    protected TablistTeam spectatorTeam;

    public void loadTablist(Tablist.Type type) {
        // create team tablist
        LinkedList<TablistGroupType> types = DisplayGroup.MAIN_TABLIST_GROUPS;

        // create spectatorTeam
        this.spectatorTeam = new TablistTeam("0", SPECTATOR_NAME, SPECTATOR_TABLIST_PREFIX,
                SPECTATOR_TABLIST_CHAT_COLOR, SPECTATOR_TABLIST_PREFIX_CHAT_COLOR) {
            @Override
            public NameTagVisibility isNameTagVisibleBy(TablistablePlayer player, TablistableGroup otherGroup) {
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
                .groupTypes(types)
                .remainTeam(this.spectatorTeam)
                .userJoin((e, tablist) -> {
                    if (e.getUser().getTask() != null
                            && e.getUser().getTask().equalsIgnoreCase(LoungeBridgeServer.getGame().getName())
                            && (e.getUser().getStatus().equals(Status.User.PRE_GAME)
                            || e.getUser().getStatus().equals(Status.User.IN_GAME))) {
                        tablist.addEntry(e.getUser());
                    } else {
                        ((TeamTablist) tablist).addRemainEntry(e.getUser());
                    }
                })
                .userQuit((e, tablist) -> tablist.removeEntry(e.getUser()));

        if (LoungeBridgeServer.getServerTeamAmount() > 0 && !LoungeBridgeServer.getGame().hideTeams()) {
            if (LoungeBridgeServer.getMaxPlayersPerTeam() == null) {
                this.gameTablist = Server.getScoreboardManager().registerTagTeamTablist(builder
                        .colorType(TeamTablist.ColorType.TEAM)
                        .teams(LoungeBridgeServer.getGame().getTeams())
                        .teamType(de.timesnake.basic.game.util.game.TablistGroupType.GAME_TEAM));

                for (Team team : LoungeBridgeServer.getGame().getTeamsSortedByRank(LoungeBridgeServer.getServerTeamAmount()).values()) {
                    this.gameTablist.addTeamHeader(team.getTablistRank(), "0",
                            team.getTablistChatColor() + "§l" + team.getTablistName());
                }
            } else {
                LinkedList<TablistGroupType> gameTeamTypes = new LinkedList<>(types);
                gameTeamTypes.addFirst(de.timesnake.basic.game.util.game.TablistGroupType.GAME_TEAM);
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
        this.gameTablist.setFooter("§7Server: " + Server.getName() + "\n§cSupport: /ticket or \nsupport@timesnake.de");
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

                this.gameTablist.setHeader(ChatColor.GOLD + LoungeBridgeServer.getGame().getDisplayName() + ChatColor.GRAY + ": " +
                        ChatColor.DARK_GREEN + map.getDisplayName() +
                        (!authors.isEmpty() ? "\n" + ChatColor.GRAY + " by " + ChatColor.BLUE + authors : ""));
            } else {
                this.gameTablist.setHeader(ChatColor.GOLD + LoungeBridgeServer.getGame().getDisplayName());
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
