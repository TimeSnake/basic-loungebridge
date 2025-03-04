/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.basic.loungebridge.util.tool;

import de.timesnake.basic.bukkit.util.server.ExTime;
import de.timesnake.basic.loungebridge.core.main.BasicLoungeBridge;
import de.timesnake.basic.loungebridge.util.server.LoungeBridgeServer;
import de.timesnake.basic.loungebridge.util.tool.scheduler.PreStopableTool;
import de.timesnake.library.basic.util.RandomList;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.scheduler.BukkitTask;

import java.util.Collection;

public abstract class TntWorldDestroyer implements PreStopableTool {

  private int tntToSpawnPerPeriod;
  private BukkitTask task;

  public TntWorldDestroyer() {
  }

  public void start(ExTime destroyPeriod, int tntToSpawnPerPeriod) {
    this.tntToSpawnPerPeriod = tntToSpawnPerPeriod;

    if (this.task != null) {
      this.task.cancel();
    }

    this.task = LoungeBridgeServer.runTaskTimerSynchrony(this::spawnTnt, 0, destroyPeriod.toTicks(),
        BasicLoungeBridge.getPlugin());
  }

  private void spawnTnt() {
    RandomList<? extends Block> blocks = new RandomList<>(this.getBlocks());
    for (int i = 0; i < this.tntToSpawnPerPeriod && !blocks.isEmpty(); i++) {
      Block block;
      do {
        Block targetBlock = blocks.popAny();
        block = LoungeBridgeServer.getMap().getWorld().getHighestBlockAt(targetBlock.getX(), targetBlock.getZ());
      } while (block.getY() == LoungeBridgeServer.getMap().getWorld().getMinHeight() && !blocks.isEmpty());
      LoungeBridgeServer.getMap().getWorld().spawnEntity(block.getLocation().add(0, 24, 0), EntityType.TNT);
    }
  }

  @Override
  public void preStop() {
    if (task != null) {
      this.task.cancel();
    }
  }

  public abstract Collection<? extends Block> getBlocks();
}