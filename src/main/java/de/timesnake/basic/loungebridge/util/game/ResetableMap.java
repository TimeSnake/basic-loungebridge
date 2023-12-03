/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.basic.loungebridge.util.game;

import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.world.ExWorld;
import de.timesnake.basic.loungebridge.util.tool.scheduler.ResetableTool;

/**
 * Marks map as resetable map. So, world gets reset at game reset.
 */
public interface ResetableMap extends ResetableTool {

  @Override
  default void reset() {
    Server.getWorldManager().reloadWorld(this.getWorld());
  }

  ExWorld getWorld();
}
