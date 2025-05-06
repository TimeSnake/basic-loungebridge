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

public class ItemSpawner extends LocationItemSpawner {

  private final Logger logger = LogManager.getLogger("game.item-spawner");
  private final Integer locationIndex;


  public ItemSpawner(Integer locationIndex, TimeUnit unit, int delay, List<? extends ItemStack> items) {
    this(locationIndex, unit, delay, 0, items);
  }

  public ItemSpawner(Integer locationIndex, TimeUnit unit, int delay, int delayRange, List<? extends ItemStack> items) {
    this(locationIndex, unit, delay, delayRange, () -> List.of(items.get(Server.getRandom().nextInt(items.size()))));
  }

  public ItemSpawner(Integer locationIndex, TimeUnit unit, int delay, int delayRange, Supplier<List<? extends ItemStack>> itemSupplier) {
    super(null, unit, delay, delayRange, itemSupplier);
    this.locationIndex = locationIndex;
  }

  @Override
  public void start() {
    this.location = LoungeBridgeServer.getMap().getLocation(this.locationIndex);

    if (this.location == null) {
      this.logger.warn("Item spawn location with index '{}' not found", this.locationIndex);
      return;
    }
    super.start();
  }
}
