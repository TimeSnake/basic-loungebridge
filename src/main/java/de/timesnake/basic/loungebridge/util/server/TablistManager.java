/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.basic.loungebridge.util.server;

import de.timesnake.basic.bukkit.core.user.scoreboard.tablist.Tablist2;
import de.timesnake.basic.bukkit.core.user.scoreboard.tablist.TablistTextEntry;
import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.chat.ChatColor;
import de.timesnake.basic.bukkit.util.group.DisplayGroup;
import de.timesnake.basic.bukkit.util.user.scoreboard.ScoreboardManager;
import de.timesnake.basic.bukkit.util.user.scoreboard.Tablist;
import de.timesnake.basic.bukkit.util.user.scoreboard.TablistGroup;
import de.timesnake.basic.game.util.game.Map;
import de.timesnake.basic.game.util.game.TablistGroupType;
import de.timesnake.basic.loungebridge.util.tool.scheduler.MapLoadableTool;
import de.timesnake.basic.loungebridge.util.user.TablistTeam;
import de.timesnake.library.chat.ExTextColor;
import de.timesnake.library.packets.util.packet.TablistHead;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static de.timesnake.basic.loungebridge.util.server.LoungeBridgeServerManager.SPECTATOR_NAME;
import static de.timesnake.basic.loungebridge.util.server.LoungeBridgeServerManager.SPECTATOR_TABLIST_CHAT_COLOR;

public class TablistManager implements MapLoadableTool {

  protected Tablist2 gameTablist;

  protected TablistTeam tablistGameTeam;
  protected TablistGroup spectatorGroup;

  public void loadTablist(Tablist.Type type) {
    this.tablistGameTeam = this.loadGameTeam();
    this.spectatorGroup = this.loadSpectatorGroup();

    List<de.timesnake.basic.bukkit.util.user.scoreboard.TablistGroupType> types = new ArrayList<>();
    types.add(TablistGroupType.GAME_TEAM);
    types.addAll(DisplayGroup.MAIN_TABLIST_GROUPS);

    Tablist2.Builder builder = new Tablist2.Builder("game")
        .type(type)
        .groupTypes(types)
        .colorGroupType(TablistGroupType.GAME_TEAM)
        .addDefaultGroup(TablistGroupType.GAME_TEAM, this.spectatorGroup)
        .setGroupGap(null, 2);

    if (LoungeBridgeServer.getServerTeamAmount() > 0 && !LoungeBridgeServer.getGame().hideTeams()) {
      if (LoungeBridgeServer.getMaxPlayersPerTeam() == null) {
        this.gameTablist = Server.getScoreboardManager().registerTablist(builder
            .addGroupDecoration(TablistGroupType.GAME_TEAM, e -> {
                  if (!e.getGroup().equals(this.spectatorGroup)) {
                    e.addHeader(new TablistTextEntry("0",
                        "§" + e.getGroup().getTablistColor().getLegacyToken() + "§l" + e.getGroup().getTablistName(),
                        TablistHead.BLANK));
                  }
                }
            ));
      } else {
        this.gameTablist = Server.getScoreboardManager().registerTablist(builder);
      }
    } else {
      this.gameTablist = Server.getScoreboardManager().registerTablist(builder);
    }

    this.gameTablist.setHeader("§6" + LoungeBridgeServer.getGame().getDisplayName());
    this.gameTablist.setFooter(ScoreboardManager.getDefaultFooter());
  }

  @Override
  public void onMapLoad() {
    if (this.gameTablist != null) {
      Map map = LoungeBridgeServer.getMap();
      if (map != null) {
        this.gameTablist.setHeader(ChatColor.GOLD + LoungeBridgeServer.getGame().getDisplayName()
            + ChatColor.GRAY + ": " + ChatColor.DARK_GREEN + map.getDisplayName() +
            (!map.getAuthors().isEmpty() ? "\n" + ChatColor.GRAY + " by "
                + ChatColor.BLUE + String.join("\n", map.getAuthors()) : ""));
      } else {
        this.gameTablist.setHeader(ChatColor.GOLD + LoungeBridgeServer.getGame().getDisplayName());
      }
    }
  }

  protected TablistTeam loadGameTeam() {
    return new TablistTeam(0, "game", "", ExTextColor.WHITE, ExTextColor.WHITE);
  }

  protected TablistGroup loadSpectatorGroup() {
    return new TablistGroup() {
      @Override
      public int getTablistRank() {
        return 80;
      }

      @Override
      public @NotNull String getTablistName() {
        return SPECTATOR_NAME;
      }

      @Override
      public ExTextColor getTablistPrefixColor() {
        return SPECTATOR_TABLIST_CHAT_COLOR;
      }
    };
  }

  public TablistTeam getTablistGameTeam() {
    return tablistGameTeam;
  }

  public Tablist2 getGameTablist() {
    return gameTablist;
  }
}
