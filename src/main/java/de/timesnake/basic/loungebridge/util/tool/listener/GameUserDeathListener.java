/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.basic.loungebridge.util.tool.listener;

import de.timesnake.basic.bukkit.util.user.event.UserDeathEvent;
import de.timesnake.basic.loungebridge.util.tool.GameTool;
import de.timesnake.basic.loungebridge.util.user.GameUser;

@FunctionalInterface
public interface GameUserDeathListener extends GameTool {

  void onGameUserDeath(UserDeathEvent e, GameUser user);

}
