/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.basic.loungebridge.util.game;

import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.world.ExLocation;
import de.timesnake.basic.loungebridge.core.main.BasicLoungeBridge;
import de.timesnake.basic.loungebridge.util.server.LoungeBridgeServer;
import de.timesnake.basic.loungebridge.util.tool.GameTool;
import de.timesnake.basic.loungebridge.util.tool.StartableTool;
import de.timesnake.basic.loungebridge.util.tool.StopableTool;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import java.util.List;
import java.util.Random;

public class ItemSpawner implements GameTool, StartableTool, StopableTool {

    private final int delayBase;
    private final int delayRange;
    private final Integer locationIndex;
    private final List<? extends ItemStack> items;
    private final Random random = new Random();
    private boolean inTicks = false;
    private BukkitTask task;
    private int delay;

    public ItemSpawner(Integer locationIndex, int delay, int delayRange, List<? extends ItemStack> items) {
        this.locationIndex = locationIndex;
        this.items = items;
        this.delayBase = delay;
        this.delayRange = delayRange;
    }

    public ItemSpawner(Integer locationIndex, int delay, int delayRange, boolean inTicks,
                       List<? extends ItemStack> items) {
        this.locationIndex = locationIndex;
        this.items = items;
        this.delayBase = delay;
        this.delayRange = delayRange;
        this.inTicks = true;
    }

    @Override
    public void start() {
        ExLocation location = LoungeBridgeServer.getMap().getLocation(this.locationIndex);

        if (location == null) {
            return;
        }

        this.delay = this.random.nextInt(delayRange) + delayBase;

        this.task = Server.runTaskTimerSynchrony(() -> {
            delay--;

            if (delay <= 0) {
                location.getWorld().dropItem(location, this.items.get(this.random.nextInt(this.items.size())));
                this.delay = this.random.nextInt(delayRange) + delayBase;
            }
        }, this.inTicks ? 1 : 20, this.inTicks ? 1 : 20, BasicLoungeBridge.getPlugin());
    }

    @Override
    public void stop() {
        if (this.task != null) {
            this.task.cancel();
        }
    }

}
