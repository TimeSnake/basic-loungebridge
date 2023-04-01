/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.basic.loungebridge.util.tool.listener;

import de.timesnake.basic.game.util.user.SpectatorUser;
import de.timesnake.basic.loungebridge.util.tool.GameTool;

@FunctionalInterface
public interface SpectatorUserJoinListener extends GameTool {

    void onSpectatorUserJoin(SpectatorUser user);

}
