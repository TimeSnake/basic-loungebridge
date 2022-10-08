/*
 * basic-lounge-bridge.main
 * Copyright (C) 2022 timesnake
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; If not, see <http://www.gnu.org/licenses/>.
 */

package de.timesnake.basic.loungebridge.util.tool;

import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.loungebridge.core.main.BasicLoungeBridge;
import de.timesnake.basic.loungebridge.util.server.LoungeBridgeServer;
import org.bukkit.scheduler.BukkitTask;

public abstract class TimerTool implements GameTool, StartableTool, MapLoadableTool, PreStopableTool {

    protected int time = 0;
    protected BukkitTask task;

    public TimerTool() {

    }

    @Override
    public void onMapLoad() {
        this.time = ((Timeable) LoungeBridgeServer.getMap()).getTime();
    }

    @Override
    public void start() {
        this.task = Server.runTaskTimerSynchrony(() -> {
            this.onTimerUpdate();

            if (this.time <= 0) {
                this.onTimerEnd();
                this.task.cancel();
                return;
            }

            this.time--;
        }, 0, 20, BasicLoungeBridge.getPlugin());
    }

    @Override
    public void preStop() {
        if (this.task != null) {
            this.task.cancel();
        }
    }

    public int getTime() {
        return time;
    }

    public BukkitTask getTask() {
        return task;
    }

    public abstract void onTimerUpdate();

    public abstract void onTimerEnd();
}
