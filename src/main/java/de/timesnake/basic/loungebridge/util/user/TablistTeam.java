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

package de.timesnake.basic.loungebridge.util.user;

import de.timesnake.basic.bukkit.util.user.scoreboard.TablistGroupType;
import de.timesnake.basic.bukkit.util.user.scoreboard.*;
import org.bukkit.ChatColor;

/**
 * Only for default team and spectator team. For all real teams, the {@link de.timesnake.basic.game.util.Team} class is used.
 */
public class TablistTeam implements TagTablistableGroup, TagTablistableRemainTeam {

    private final String rank;
    private final String name;
    private final String prefix;
    private final ChatColor prefixChatColor;
    private final ChatColor chatColor;

    public TablistTeam(String rank, String name, String prefix, ChatColor prefixChatColor, ChatColor chatColor) {
        this.rank = rank;
        this.name = name;
        this.prefix = prefix;
        this.prefixChatColor = prefixChatColor;
        this.chatColor = chatColor;
    }

    public TablistGroupType getTeamType() {
        return de.timesnake.basic.loungebridge.util.user.TablistGroupType.GAME;
    }

    @Override
    public String getTablistRank() {
        return this.rank;
    }

    @Override
    public String getTablistName() {
        return this.name;
    }

    @Override
    public String getTablistPrefix() {
        return this.prefix;
    }

    @Override
    public ChatColor getTablistPrefixChatColor() {
        return this.prefixChatColor;
    }

    @Override
    public ChatColor getTablistChatColor() {
        return this.chatColor;
    }

    @Override
    public NameTagVisibility isNameTagVisibleBy(TablistablePlayer player, TablistableGroup otherGroup) {
        return NameTagVisibility.ALWAYS;
    }

    @Override
    public NameTagVisibility isNameTagVisible(TablistablePlayer player) {
        return NameTagVisibility.ALWAYS;
    }
}
