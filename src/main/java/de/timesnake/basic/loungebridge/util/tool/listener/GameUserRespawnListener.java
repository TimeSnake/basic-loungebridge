/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.basic.loungebridge.util.tool.listener;

import de.timesnake.basic.loungebridge.util.tool.GameTool;
import de.timesnake.basic.loungebridge.util.user.GameUser;
import org.bukkit.Location;

@FunctionalInterface
public interface GameUserRespawnListener extends GameTool {

  Location onGameUserRespawn(GameUser user);
}
