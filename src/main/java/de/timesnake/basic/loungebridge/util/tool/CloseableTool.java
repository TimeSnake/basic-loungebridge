package de.timesnake.basic.loungebridge.util.tool;

@FunctionalInterface
public interface CloseableTool extends GameTool, MapDependable {

    void close();
}
