/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.basic.loungebridge.util.tool;

@FunctionalInterface
public interface WatchableTool extends GameTool {

  ToolWatcher getWatchers();

}
