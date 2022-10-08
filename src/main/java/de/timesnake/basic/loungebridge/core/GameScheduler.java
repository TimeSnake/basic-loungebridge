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

package de.timesnake.basic.loungebridge.core;

import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.loungebridge.core.main.BasicLoungeBridge;
import de.timesnake.basic.loungebridge.util.chat.Plugin;
import de.timesnake.basic.loungebridge.util.server.LoungeBridgeServer;
import de.timesnake.library.basic.util.chat.ExTextColor;
import net.kyori.adventure.text.Component;
import org.bukkit.Instrument;
import org.bukkit.Note;
import org.bukkit.scheduler.BukkitTask;

import java.time.Duration;

public class GameScheduler {

    private static final Integer COUNTDOWN_TIME = 7;
    private static final Integer TEXTURE_PACK_OFFSET = 2;

    protected int gameCountdown = COUNTDOWN_TIME;
    protected BukkitTask gameCountdownTask;

    public void startGameCountdown() {
        if (!LoungeBridgeServer.getState().equals(LoungeBridgeServer.State.STARTING)) {
            LoungeBridgeServer.setState(LoungeBridgeServer.State.STARTING);

            if (LoungeBridgeServer.getGame().hasTexturePack()) {
                this.gameCountdown += TEXTURE_PACK_OFFSET;
            }

            this.gameCountdownTask = Server.runTaskTimerAsynchrony(() -> {
                switch (gameCountdown) {
                    case 7 -> {
                        Server.printText(Plugin.LOUNGE, "Preparing game ...");
                        Server.runTaskSynchrony(LoungeBridgeServer::prepareGame, BasicLoungeBridge.getPlugin());
                    }
                    case 5, 4, 3, 2 -> {
                        Server.broadcastTitle(Component.text(gameCountdown, ExTextColor.WARNING), Component.empty(),
                                Duration.ofSeconds(1));
                        LoungeBridgeServer.broadcastLoungeBridgeMessage(Component.text("The Game starts in ", ExTextColor.PUBLIC)
                                .append(Component.text(gameCountdown, ExTextColor.VALUE))
                                .append(Component.text(" seconds", ExTextColor.PUBLIC)));
                        Server.broadcastNote(Instrument.STICKS, Note.natural(1, Note.Tone.A));
                    }
                    case 1 -> {
                        Server.broadcastTitle(Component.text(gameCountdown, ExTextColor.WARNING), Component.empty(),
                                Duration.ofSeconds(1));
                        LoungeBridgeServer.broadcastLoungeBridgeMessage(Component.text("The Game starts in ", ExTextColor.PUBLIC)
                                .append(Component.text(gameCountdown, ExTextColor.VALUE))
                                .append(Component.text(" second", ExTextColor.PUBLIC)));
                        Server.broadcastNote(Instrument.STICKS, Note.natural(1, Note.Tone.A));
                    }
                    case 0 -> {
                        Server.broadcastTitle(Component.text(gameCountdown, ExTextColor.WARNING), Component.empty(),
                                Duration.ofSeconds(1));
                        LoungeBridgeServer.broadcastLoungeBridgeMessage(Component.text("The Game starts in ", ExTextColor.PUBLIC)
                                .append(Component.text("now", ExTextColor.VALUE))
                                .append(Component.text(" seconds", ExTextColor.PUBLIC)));
                        Server.broadcastNote(Instrument.STICKS, Note.natural(1, Note.Tone.A));
                        Server.runTaskSynchrony(LoungeBridgeServer::startGame, BasicLoungeBridge.getPlugin());
                        this.gameCountdownTask.cancel();
                    }
                }
                gameCountdown--;
            }, 0, 20, BasicLoungeBridge.getPlugin());
        }
    }

    public void closeGame() {
        LoungeBridgeServer.broadcastLoungeBridgeMessage(Component.text("The game closes in 10 seconds", ExTextColor.WARNING));

        Server.runTaskLaterSynchrony(LoungeBridgeServer::closeGame6, 6 * 20, BasicLoungeBridge.getPlugin());

        Server.runTaskLaterSynchrony(() -> {
            LoungeBridgeServer.broadcastLoungeBridgeMessage(Component.text("Game closed", ExTextColor.WARNING));
            LoungeBridgeServer.closeGame10();

            Server.runTaskLaterSynchrony(() -> {
                this.resetGameCountdown();
                LoungeBridgeServer.resetGame();
            }, 5 * 20, BasicLoungeBridge.getPlugin());
        }, 12 * 20, BasicLoungeBridge.getPlugin());
    }

    private void resetGameCountdown() {
        if (this.gameCountdownTask != null) {
            this.gameCountdownTask.cancel();
        }
        this.gameCountdown = COUNTDOWN_TIME;
    }

    public int getGameCountdown() {
        return gameCountdown;
    }
}
