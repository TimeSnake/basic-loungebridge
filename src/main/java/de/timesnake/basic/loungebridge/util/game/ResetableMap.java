/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.basic.loungebridge.util.game;

import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.world.ExWorld;

public interface ResetableMap {

  default void reset() {
    Server.getWorldManager().reloadWorld(this.getWorld());
  }

  ExWorld getWorld();
}
