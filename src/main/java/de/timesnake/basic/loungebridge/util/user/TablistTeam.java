/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.basic.loungebridge.util.user;

import de.timesnake.basic.bukkit.util.user.scoreboard.TablistGroupType;
import de.timesnake.basic.bukkit.util.user.scoreboard.*;
import de.timesnake.basic.game.util.game.Team;
import org.bukkit.ChatColor;

/**
 * Only for default team and spectator team. For all real teams, the {@link Team} class is used.
 */
public class TablistTeam implements TagTablistableGroup, TagTablistableRemainTeam {

  private final String rank;
  private final String name;
  private final String prefix;
  private final ChatColor prefixChatColor;
  private final ChatColor chatColor;

  public TablistTeam(String rank, String name, String prefix, ChatColor prefixChatColor,
                     ChatColor chatColor) {
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
  public NameTagVisibility isNameTagVisibleBy(TablistablePlayer player,
                                              TablistableGroup otherGroup) {
    return NameTagVisibility.ALWAYS;
  }

  @Override
  public NameTagVisibility isNameTagVisible(TablistablePlayer player) {
    return NameTagVisibility.ALWAYS;
  }
}
