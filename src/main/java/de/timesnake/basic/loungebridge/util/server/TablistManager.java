/*
 * basic-lounge-bridge.main
 * Copyright (C) 2022 timesnake
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; If not, see <http://www.gnu.org/licenses/>.
 */

package de.timesnake.basic.loungebridge.util.server;

import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.chat.ChatColor;
import de.timesnake.basic.bukkit.util.chat.DisplayGroup;
import de.timesnake.basic.bukkit.util.user.User;
import de.timesnake.basic.bukkit.util.user.scoreboard.*;
import de.timesnake.basic.game.util.Map;
import de.timesnake.basic.game.util.Team;
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
                SPECTATOR_TABLIST_CHAT_COLOR, SPECTATOR_TABLIST_PREFIX_CHAT_COLOR);

        TablistUserJoin tablistJoin = (e, tablist) -> {
            User user = e.getUser();
            String task = user.getTask();

            if (task == null) {
                ((TeamTablist) tablist).addRemainEntry(e.getUser());
                return;
            }

            if (task.equalsIgnoreCase(LoungeBridgeServer.getGame().getName())) {
                if (e.getUser().getStatus().equals(Status.User.PRE_GAME)
                        || e.getUser().getStatus().equals(Status.User.IN_GAME)) {
                    tablist.addEntry(e.getUser());
                } else {
                    ((TeamTablist) tablist).addRemainEntry(e.getUser());
                }
            }
        };

        TablistUserQuit tablistQuit = (e, tablist) -> tablist.removeEntry(e.getUser());

        if (LoungeBridgeServer.getServerTeamAmount() > 0 && !LoungeBridgeServer.getGame().hideTeams()) {
            if (LoungeBridgeServer.getMaxPlayersPerTeam() == null) {
                this.gameTablist = Server.getScoreboardManager().registerNewTeamTablist("game", type,
                        TeamTablist.ColorType.TEAM, LoungeBridgeServer.getGame().getTeams(),
                        de.timesnake.basic.game.util.TablistGroupType.GAME_TEAM,
                        types, this.spectatorTeam, types, tablistJoin, tablistQuit);

                for (Team team : LoungeBridgeServer.getGame().getTeamsSortedByRank(LoungeBridgeServer.getServerTeamAmount()).values()) {

                    this.gameTablist.addTeamHeader(team.getTablistRank(), "0",
                            team.getTablistChatColor() + "§l" + team.getTablistName());
                }
            } else {
                LinkedList<TablistGroupType> gameTeamTypes = new LinkedList<>(types);
                gameTeamTypes.addFirst(de.timesnake.basic.game.util.TablistGroupType.GAME_TEAM);
                this.tablistGameTeam = new TablistTeam("0", "game", "", ChatColor.WHITE, ChatColor.WHITE);

                this.gameTablist = Server.getScoreboardManager().registerNewTeamTablist("game", type,
                        TeamTablist.ColorType.FIRST_GROUP, List.of(this.tablistGameTeam),
                        this.tablistGameTeam.getTeamType(), gameTeamTypes, this.spectatorTeam, types,
                        tablistJoin, tablistQuit);
            }
        } else {
            this.tablistGameTeam = new TablistTeam("0", "game", "", ChatColor.WHITE, ChatColor.WHITE);

            this.gameTablist = Server.getScoreboardManager().registerNewTeamTablist("game", type,
                    TeamTablist.ColorType.WHITE, List.of(this.tablistGameTeam), this.tablistGameTeam.getTeamType(),
                    types, this.spectatorTeam, types, tablistJoin, tablistQuit);
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
