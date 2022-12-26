/*
 * Copyright (C) 2022 timesnake
 */

package de.timesnake.basic.loungebridge.util.tool;

import de.timesnake.basic.game.util.game.Map;
import de.timesnake.basic.loungebridge.util.server.LoungeBridgeServer;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;

public class ToolManager {

    protected LinkedList<GameTool> gameTools = new LinkedList<>();
    protected HashMap<Map, Collection<GameTool>> gameToolsByMap = new HashMap<>();

    public void add(GameTool gameTool) {
        this.gameTools.add(gameTool);
    }

    public void add(Map map, GameTool gameTool) {
        this.gameToolsByMap.computeIfAbsent(map, m -> new LinkedList<>()).add(gameTool);
    }

    public void runTools(Class<? extends GameTool> type) {
        this.runTools(type, this.gameTools);
        this.runTools(type, this.gameToolsByMap.getOrDefault(LoungeBridgeServer.getMap(), new ArrayList<>(0)));
    }

    private void runTools(Class<? extends GameTool> type, Collection<GameTool> tools) {
        tools.stream().filter(t -> type.isAssignableFrom(t.getClass())).toList()
                .forEach(gameTool -> {
                    try {
                        type.getDeclaredMethods()[0].invoke(gameTool);
                    } catch (IllegalAccessException | InvocationTargetException ignored) {
                    }
                });
    }
}
