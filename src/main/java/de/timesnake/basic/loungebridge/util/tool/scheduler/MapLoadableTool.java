/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.basic.loungebridge.util.tool.scheduler;

import de.timesnake.basic.loungebridge.util.tool.GameTool;
import de.timesnake.basic.loungebridge.util.tool.MapDependable;

@FunctionalInterface
public interface MapLoadableTool extends GameTool, MapDependable {

  void onMapLoad();
}
