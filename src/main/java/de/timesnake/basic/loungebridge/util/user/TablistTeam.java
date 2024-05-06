/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.basic.loungebridge.util.user;

import de.timesnake.basic.bukkit.util.user.scoreboard.TablistGroup;
import de.timesnake.basic.game.util.game.Team;
import de.timesnake.library.chat.ExTextColor;
import org.jetbrains.annotations.NotNull;

/**
 * Only for default team and spectator team. For all real teams, the {@link Team} class is used.
 */
public class TablistTeam implements TablistGroup {

  private final int rank;
  private final String name;
  private final String prefix;
  private final ExTextColor prefixChatColor;
  private final ExTextColor chatColor;

  public TablistTeam(int rank, String name, String prefix, ExTextColor prefixChatColor, ExTextColor chatColor) {
    this.rank = rank;
    this.name = name;
    this.prefix = prefix;
    this.prefixChatColor = prefixChatColor;
    this.chatColor = chatColor;
  }

  @Override
  public int getTablistRank() {
    return this.rank;
  }

  @Override
  public @NotNull String getTablistName() {
    return this.name;
  }

  @Override
  public String getTablistPrefix() {
    return this.prefix;
  }

  @Override
  public ExTextColor getTablistPrefixColor() {
    return this.prefixChatColor;
  }

  @Override
  public ExTextColor getTablistColor() {
    return this.chatColor;
  }

}
