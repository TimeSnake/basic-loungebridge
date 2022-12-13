/*
 * workspace.basic-loungebridge.main
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

package de.timesnake.basic.loungebridge.core.main;

import de.timesnake.basic.bukkit.util.chat.Chat;
import de.timesnake.basic.bukkit.util.user.scoreboard.Sideboard;
import de.timesnake.basic.bukkit.util.user.scoreboard.TeamTablist;
import de.timesnake.basic.bukkit.util.world.ExLocation;
import de.timesnake.basic.loungebridge.util.server.LoungeBridgeServer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SpectatorManager extends de.timesnake.basic.game.util.user.SpectatorManager {
    @Override
    public @NotNull TeamTablist getGameTablist() {
        return LoungeBridgeServer.getGameTablist();
    }

    @Override
    public @Nullable Sideboard getGameSideboard() {
        return LoungeBridgeServer.getGameSideboard();
    }

    @Override
    public @Nullable Sideboard getSpectatorSideboard() {
        return LoungeBridgeServer.getSpectatorSideboard();
    }

    @Override
    public @Nullable Chat getSpectatorChat() {
        return LoungeBridgeServer.getSpectatorChat();
    }

    @Override
    public ExLocation getSpectatorSpawn() {
        return LoungeBridgeServer.getSpectatorSpawn();
    }

    @Override
    public boolean loadTools() {
        return !LoungeBridgeServer.getState().equals(LoungeBridgeServer.State.CLOSING)
                && !LoungeBridgeServer.getState().equals(LoungeBridgeServer.State.STOPPED);
    }
}
