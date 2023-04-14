/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.basic.loungebridge.util.tool.advanced;

import de.timesnake.basic.loungebridge.util.server.LoungeBridgeServer;
import de.timesnake.basic.loungebridge.util.tool.Timeable;
import de.timesnake.basic.loungebridge.util.tool.scheduler.MapLoadableTool;

public abstract class MapTimerTool extends TimerTool implements MapLoadableTool {

    public MapTimerTool() {
        super(0);
    }

    @Override
    public void onMapLoad() {
        this.maxTime = ((Timeable) LoungeBridgeServer.getMap()).getTime();
        this.time = ((Timeable) LoungeBridgeServer.getMap()).getTime();
    }

    @Override
    public void prepare() {

    }
}
