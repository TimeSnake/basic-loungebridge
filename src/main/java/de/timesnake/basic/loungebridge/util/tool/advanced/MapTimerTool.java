/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.basic.loungebridge.util.tool.advanced;

import de.timesnake.basic.loungebridge.util.server.LoungeBridgeServer;
import de.timesnake.basic.loungebridge.util.tool.Timeable;
import de.timesnake.basic.loungebridge.util.tool.scheduler.MapLoadableTool;

public abstract class MapTimerTool extends TimerTool implements MapLoadableTool {

  public MapTimerTool() {
    super(() -> ((Timeable) LoungeBridgeServer.getMap()).getTime());
  }
}
