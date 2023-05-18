/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.basic.loungebridge.util.tool.advanced;

import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.user.User;
import de.timesnake.basic.bukkit.util.user.event.AsyncUserMoveEvent;
import de.timesnake.basic.loungebridge.core.main.BasicLoungeBridge;
import de.timesnake.basic.loungebridge.util.tool.GameTool;
import de.timesnake.basic.loungebridge.util.tool.scheduler.StartableTool;
import de.timesnake.basic.loungebridge.util.tool.scheduler.StopableTool;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitTask;

public abstract class AntiCampTool implements Listener, GameTool, StartableTool, StopableTool {

  private final ConcurrentHashMap<User, Location> locationsByUser = new ConcurrentHashMap<>();
  private final double range;
  private final int time;
  private BukkitTask task;

  public AntiCampTool() {
    this(60);
  }

  public AntiCampTool(int time) {
    this(time, 3);
  }

  public AntiCampTool(double range) {
    this(60, range);
  }

  public AntiCampTool(int time, double range) {
    this.range = range;
    this.time = time;
    Server.registerListener(this, BasicLoungeBridge.getPlugin());
  }

  @EventHandler
  public void onUserMove(AsyncUserMoveEvent e) {
    User user = e.getUser();

    if (!this.locationsByUser.containsKey(user)) {
      return;
    }

    if (this.locationsByUser.get(user).distanceSquared(e.getTo()) > this.range * this.range) {
      this.locationsByUser.remove(user);
    }
  }

  @Override
  public void start() {
    this.task = Server.runTaskTimerAsynchrony(() -> {
      this.locationsByUser.keySet().forEach(
          user -> Server.runTaskSynchrony(() -> this.teleport(user),
              BasicLoungeBridge.getPlugin()));
      Server.getInGameUsers().forEach(u -> this.locationsByUser.put(u, u.getLocation()));
    }, 0, time * 20, BasicLoungeBridge.getPlugin());
  }

  @Override
  public void stop() {
    if (this.task != null) {
      this.task.cancel();
    }
    this.locationsByUser.clear();
  }

  public abstract void teleport(User user);
}
