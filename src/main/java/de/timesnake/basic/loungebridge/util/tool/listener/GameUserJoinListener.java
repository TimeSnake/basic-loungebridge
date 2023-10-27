/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.basic.loungebridge.util.tool.listener;

import de.timesnake.basic.loungebridge.util.tool.GameTool;
import de.timesnake.basic.loungebridge.util.user.GameUser;

@FunctionalInterface
public interface GameUserJoinListener extends GameTool {

  /**
   * Called then user joins game, after {@link GameUser#onGameJoin()} and {@link GameUser#applyKit()}
   *
   * @param user
   */
  void onGameUserJoin(GameUser user);

}
