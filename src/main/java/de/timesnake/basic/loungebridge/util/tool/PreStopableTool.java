package de.timesnake.basic.loungebridge.util.tool;

@FunctionalInterface
public interface PreStopableTool extends GameTool, MapDependable {

    void preStop();
}
