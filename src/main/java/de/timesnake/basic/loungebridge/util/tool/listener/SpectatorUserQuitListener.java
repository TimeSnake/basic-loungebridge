/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.basic.loungebridge.util.tool.listener;

import de.timesnake.basic.game.util.user.SpectatorUser;
import de.timesnake.basic.loungebridge.util.tool.GameTool;

@FunctionalInterface
public interface SpectatorUserQuitListener extends GameTool {

    void onSpectatorUserQuit(SpectatorUser user);

}
