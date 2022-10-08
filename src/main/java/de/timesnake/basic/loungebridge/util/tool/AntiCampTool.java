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
import de.timesnake.basic.bukkit.util.user.User;
import de.timesnake.basic.bukkit.util.user.event.AsyncUserMoveEvent;
import de.timesnake.basic.loungebridge.core.main.BasicLoungeBridge;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitTask;

import java.util.concurrent.ConcurrentHashMap;

public abstract class AntiCampTool implements Listener, GameTool, StartableTool, StopableTool {

    private final ConcurrentHashMap<User, Location> locationsByUser = new ConcurrentHashMap<>();
    private final double range;
    private final int time;
    private BukkitTask task;

    public AntiCampTool() {
        this(60);
    }

    public AntiCampTool(int time) {
        this(time, 3);
    }

    public AntiCampTool(double range) {
        this(60, range);
    }

    public AntiCampTool(int time, double range) {
        this.range = range;
        this.time = time;
        Server.registerListener(this, BasicLoungeBridge.getPlugin());
    }

    @EventHandler
    public void onUserMove(AsyncUserMoveEvent e) {
        User user = e.getUser();

        if (!this.locationsByUser.containsKey(user)) {
            return;
        }

        if (this.locationsByUser.get(user).distanceSquared(e.getTo()) > this.range * this.range) {
            this.locationsByUser.remove(user);
        }
    }

    @Override
    public void start() {
        this.task = Server.runTaskTimerAsynchrony(() -> {
            this.locationsByUser.keySet().forEach(user -> Server.runTaskSynchrony(() -> this.teleport(user), BasicLoungeBridge.getPlugin()));
            Server.getInGameUsers().forEach(u -> this.locationsByUser.put(u, u.getLocation()));
        }, 0, time * 20, BasicLoungeBridge.getPlugin());
    }

    @Override
    public void stop() {
        if (this.task != null) {
            this.task.cancel();
        }
        this.locationsByUser.clear();
    }

    public abstract void teleport(User user);
}
