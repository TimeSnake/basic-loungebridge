/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.basic.loungebridge.util.tool.advanced;

import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.server.ExTime;
import de.timesnake.basic.bukkit.util.world.ExWorld;
import de.timesnake.basic.bukkit.util.world.ExWorldBorder;
import de.timesnake.basic.game.util.game.Map;
import de.timesnake.basic.game.util.user.SpectatorUser;
import de.timesnake.basic.loungebridge.core.main.BasicLoungeBridge;
import de.timesnake.basic.loungebridge.util.server.LoungeBridgeServer;
import de.timesnake.basic.loungebridge.util.tool.listener.GameUserJoinListener;
import de.timesnake.basic.loungebridge.util.tool.listener.GameUserQuitListener;
import de.timesnake.basic.loungebridge.util.tool.listener.SpectatorUserJoinListener;
import de.timesnake.basic.loungebridge.util.tool.listener.SpectatorUserQuitListener;
import de.timesnake.basic.loungebridge.util.tool.scheduler.MapLoadableTool;
import de.timesnake.basic.loungebridge.util.tool.scheduler.PreStopableTool;
import de.timesnake.basic.loungebridge.util.tool.scheduler.WorldLoadableTool;
import de.timesnake.basic.loungebridge.util.user.GameUser;
import org.bukkit.Location;
import org.bukkit.scheduler.BukkitTask;

public abstract class WorldBorderTool implements MapLoadableTool, WorldLoadableTool, PreStopableTool,
    GameUserJoinListener, GameUserQuitListener,
    SpectatorUserJoinListener, SpectatorUserQuitListener {

  private ExWorldBorder border;
  private BukkitTask shrinkStartTask;

  public WorldBorderTool() {

  }

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

  @Override
  public void preStop() {
    if (this.border != null) {
      this.border.stopShrink();
    }
    if (this.shrinkStartTask != null) {
      this.shrinkStartTask.cancel();
    }
  }

  private void loadBorder(ExWorld world) {
    Location center = this.getBorderCenter();
    double size = this.getBorderSize();
    double damage = this.getBorderDamagePerSec();

    if (this.border != null) {
      this.border.destroy();
    }

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

  public void shrinkBorder(double size, ExTime time) {
    this.border.setSize(size, time, true);
    if (this.shrinkStartTask != null) {
      this.shrinkStartTask.cancel();
    }
    this.onShrink();
  }

  public void shrinkBorder(double size, ExTime time, ExTime delay) {
    this.shrinkStartTask = Server.runTaskLaterSynchrony(() -> this.shrinkBorder(size, time), delay.toTicks(),
        BasicLoungeBridge.getPlugin());
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

  public void onShrink() {

  }
}
