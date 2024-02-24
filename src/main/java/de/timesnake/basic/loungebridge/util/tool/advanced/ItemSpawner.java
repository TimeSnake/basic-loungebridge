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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import java.util.List;
import java.util.function.Supplier;

public class ItemSpawner implements GameTool, StartableTool, StopableTool {

  private final Logger logger = LogManager.getLogger("game.item-spawner");

  private final int delayBase;
  private final int delayRange;
  private final Integer locationIndex;
  private final Supplier<List<? extends ItemStack>> itemSupplier;
  private final TimeUnit unit;
  private BukkitTask task;
  private int delay;

  public ItemSpawner(Integer locationIndex, TimeUnit unit, int delay, List<? extends ItemStack> items) {
    this(locationIndex, unit, delay, 0, items);
  }

  public ItemSpawner(Integer locationIndex, TimeUnit unit, int delay, int delayRange, List<? extends ItemStack> items) {
    this(locationIndex, unit, delay, delayRange, () -> List.of(items.get(Server.getRandom().nextInt(items.size()))));
  }

  public ItemSpawner(Integer locationIndex, TimeUnit unit, int delay, int delayRange, Supplier<List<?
      extends ItemStack>> itemSupplier) {
    this.locationIndex = locationIndex;
    this.itemSupplier = itemSupplier;
    this.delayBase = delay;
    this.delayRange = delayRange;
    this.unit = unit;
  }

  @Override
  public void start() {
    this.logger.info("Starting item spawner '{}", this.locationIndex);

    ExLocation location = LoungeBridgeServer.getMap().getLocation(this.locationIndex);

    if (location == null) {
      this.logger.warn("Item spawn location with index '{}' not found", this.locationIndex);
      return;
    }

    this.delay = (this.delayRange > 0 ? Server.getRandom().nextInt(delayRange) : 0) + delayBase;

    this.task = Server.runTaskTimerSynchrony(() -> {
      delay--;

      if (delay <= 0) {
        this.itemSupplier.get().forEach(i -> location.getWorld().dropItem(location, i));
        this.delay = (this.delayRange > 0 ? Server.getRandom().nextInt(delayRange) : 0) + delayBase;
      }
    }, 0, this.unit.getTicks(), BasicLoungeBridge.getPlugin());
  }

  @Override
  public void stop() {
    if (this.task != null) {
      this.task.cancel();
    }

    this.logger.info("Stopping item spawner '{}'", this.locationIndex);
  }

}
