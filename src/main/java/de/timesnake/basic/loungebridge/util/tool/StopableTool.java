/*
 * Copyright (C) 2022 timesnake
 */

package de.timesnake.basic.loungebridge.util.tool;

@FunctionalInterface
public interface StopableTool extends GameTool, MapDependable {

    void stop();
}
