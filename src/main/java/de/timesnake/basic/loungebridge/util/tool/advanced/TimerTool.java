/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.basic.loungebridge.util.tool.advanced;

import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.loungebridge.core.main.BasicLoungeBridge;
import de.timesnake.basic.loungebridge.util.tool.GameTool;
import de.timesnake.basic.loungebridge.util.tool.scheduler.MapLoadableTool;
import de.timesnake.basic.loungebridge.util.tool.scheduler.PreStopableTool;
import de.timesnake.basic.loungebridge.util.tool.scheduler.StartableTool;
import de.timesnake.basic.loungebridge.util.tool.scheduler.WorldLoadableTool;
import org.bukkit.scheduler.BukkitTask;

public abstract class TimerTool implements GameTool, StartableTool, WorldLoadableTool,
    MapLoadableTool, PreStopableTool {

  protected int maxTime;
  protected int time;
  protected BukkitTask task;

  public TimerTool(int time) {
    this.maxTime = time;
    this.time = time;
  }

  @Override
  public void onWorldLoad() {
    this.prepare();
  }

  @Override
  public void onMapLoad() {
    this.prepare();
  }

  protected void prepare() {
    this.time = this.maxTime;
  }

  @Override
  public void start() {
    this.task = Server.runTaskTimerSynchrony(() -> {
      this.onTimerUpdate();

      if (this.time <= 0) {
        this.onTimerEnd();
        this.task.cancel();
        return;
      }

      this.time--;
    }, 0, 20, BasicLoungeBridge.getPlugin());
  }

  @Override
  public void preStop() {
    if (this.task != null) {
      this.task.cancel();
    }
  }

  public int getTime() {
    return time;
  }

  public void setTime(int time) {
    this.time = time;
  }

  public BukkitTask getTask() {
    return task;
  }

  public abstract void onTimerUpdate();

  public abstract void onTimerEnd();
}
