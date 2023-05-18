/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.basic.loungebridge.util.tool.advanced;

import de.timesnake.basic.bukkit.util.world.ExWorld;
import de.timesnake.basic.bukkit.util.world.ExWorldBorder;
import de.timesnake.basic.game.util.game.Map;
import de.timesnake.basic.game.util.user.SpectatorUser;
import de.timesnake.basic.loungebridge.util.server.LoungeBridgeServer;
import de.timesnake.basic.loungebridge.util.tool.listener.GameUserJoinListener;
import de.timesnake.basic.loungebridge.util.tool.listener.GameUserQuitListener;
import de.timesnake.basic.loungebridge.util.tool.listener.SpectatorUserJoinListener;
import de.timesnake.basic.loungebridge.util.tool.listener.SpectatorUserQuitListener;
import de.timesnake.basic.loungebridge.util.tool.scheduler.MapLoadableTool;
import de.timesnake.basic.loungebridge.util.tool.scheduler.WorldLoadableTool;
import de.timesnake.basic.loungebridge.util.user.GameUser;
import org.bukkit.Location;

public abstract class WorldBorderTool implements MapLoadableTool, WorldLoadableTool,
    GameUserJoinListener, GameUserQuitListener,
    SpectatorUserJoinListener, SpectatorUserQuitListener {

  private ExWorldBorder border;

  @Override
  public void onMapLoad() {
    Map map = LoungeBridgeServer.getMap();

    if (map == null) {
      return;
    }

    this.loadBorder(map.getWorld());
  }

  @Override
  public void onWorldLoad() {
    ExWorld world = LoungeBridgeServer.getGameWorld();

    if (world == null) {
      return;
    }

    this.loadBorder(world);
  }

  private void loadBorder(ExWorld world) {
    Location center = this.getBorderCenter();
    double size = this.getBorderSize();
    double damage = this.getBorderDamagePerSec();

    this.border = new ExWorldBorder.Builder()
        .world(world)
        .centerX(center.getX())
        .centerZ(center.getZ())
        .size(size)
        .warningDistance(5)
        .damagePerSec(damage)
        .sound(true)
        .build();
  }

  @Override
  public void onGameUserJoin(GameUser user) {
    this.border.addUser(user);
  }

  @Override
  public void onGameUserQuit(GameUser user) {
    this.border.removeUser(user);
  }

  @Override
  public void onSpectatorUserJoin(SpectatorUser user) {
    this.border.addSpectator(user);
  }

  @Override
  public void onSpectatorUserQuit(SpectatorUser user) {
    this.border.removeSpectator(user);
  }

  public ExWorldBorder getBorder() {
    return border;
  }

  public abstract Location getBorderCenter();

  public abstract double getBorderSize();

  public abstract double getBorderDamagePerSec();
}
