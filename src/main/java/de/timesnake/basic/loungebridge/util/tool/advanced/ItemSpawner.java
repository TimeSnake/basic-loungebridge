/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.basic.loungebridge.util.tool.advanced;

import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.server.TimeUnit;
import de.timesnake.basic.bukkit.util.world.ExLocation;
import de.timesnake.basic.loungebridge.core.main.BasicLoungeBridge;
import de.timesnake.basic.loungebridge.util.server.LoungeBridgeServer;
import de.timesnake.basic.loungebridge.util.tool.GameTool;
import de.timesnake.basic.loungebridge.util.tool.scheduler.StartableTool;
import de.timesnake.basic.loungebridge.util.tool.scheduler.StopableTool;
import de.timesnake.library.basic.util.Loggers;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import java.util.List;
import java.util.Random;

public class ItemSpawner implements GameTool, StartableTool, StopableTool {

  private final int delayBase;
  private final int delayRange;
  private final Integer locationIndex;
  private final List<? extends ItemStack> items;
  private final Random random = new Random();
  private final TimeUnit unit;
  private BukkitTask task;
  private int delay;

  public ItemSpawner(Integer locationIndex, TimeUnit unit, int delay, List<? extends ItemStack> items) {
    this(locationIndex, unit, delay, 0, items);
  }

  public ItemSpawner(Integer locationIndex, TimeUnit unit, int delay, int delayRange, List<? extends ItemStack> items) {
    this.locationIndex = locationIndex;
    this.items = items;
    this.delayBase = delay;
    this.delayRange = delayRange;
    this.unit = unit;
  }


  @Override
  public void start() {
    ExLocation location = LoungeBridgeServer.getMap().getLocation(this.locationIndex);

    if (location == null) {
      Loggers.GAME.warning("Item spawn location with index '" + this.locationIndex + "' not found");
      return;
    }

    this.delay = (this.delayRange > 0 ? this.random.nextInt(delayRange) : 0) + delayBase;

    this.task = Server.runTaskTimerSynchrony(() -> {
      delay--;

      if (delay <= 0) {
        location.getWorld().dropItem(location, this.items.get(this.random.nextInt(this.items.size())));
        this.delay = (this.delayRange > 0 ? this.random.nextInt(delayRange) : 0) + delayBase;
      }
    }, 0, this.unit.getTicks(), BasicLoungeBridge.getPlugin());
  }

  @Override
  public void stop() {
    if (this.task != null) {
      this.task.cancel();
    }
  }

}
