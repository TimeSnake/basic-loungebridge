/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.basic.loungebridge.util.tool;

import de.timesnake.basic.game.util.game.Map;
import de.timesnake.basic.loungebridge.util.server.LoungeBridgeServer;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.function.Consumer;

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
        this.runTools(type,
                this.gameToolsByMap.getOrDefault(LoungeBridgeServer.getMap(), new ArrayList<>(0)));
    }

    public <T extends GameTool> void applyOnTools(Class<T> type, Consumer<T> consumer) {
        this.applyOnTools(type, this.gameTools, consumer);
        this.applyOnTools(type,
                this.gameToolsByMap.getOrDefault(LoungeBridgeServer.getMap(), new ArrayList<>(0)),
                consumer);
    }

    private void runTools(Class<? extends GameTool> type, Collection<GameTool> tools) {
        this.applyOnTools(type, tools, t -> {
            try {
                type.getDeclaredMethods()[0].invoke(t);
            } catch (IllegalAccessException | InvocationTargetException ignored) {
            }
        });
    }

    private <T extends GameTool> void applyOnTools(Class<T> type, Collection<GameTool> tools,
            Consumer<T> consumer) {
        tools.stream().filter(t -> type.isAssignableFrom(t.getClass()))
                .forEach(gameTool -> consumer.accept((T) gameTool));
    }
}
