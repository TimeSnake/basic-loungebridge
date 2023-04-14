/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.basic.loungebridge.util.tool.scheduler;

import de.timesnake.basic.loungebridge.util.tool.GameTool;

@FunctionalInterface
public interface WorldLoadableTool extends GameTool {

    void onWorldLoad();

}
