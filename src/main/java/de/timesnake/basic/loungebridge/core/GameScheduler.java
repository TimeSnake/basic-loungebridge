/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.basic.loungebridge.core;

import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.loungebridge.core.main.BasicLoungeBridge;
import de.timesnake.basic.loungebridge.util.server.LoungeBridgeServer;
import de.timesnake.library.basic.util.Loggers;
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

      this.gameCountdownTask = Server.runTaskTimerSynchrony(() -> {
        switch (gameCountdown) {
          case 7 -> {
            Loggers.LOUNGE_BRIDGE.info("Preparing game ...");
            LoungeBridgeServer.prepareGame();
          }
          case 5, 4, 3, 2, 1 -> {
            Server.broadcastTDTitle("§w" + gameCountdown, "", Duration.ofSeconds(1));
            LoungeBridgeServer.broadcastLoungeBridgeTDMessage("§pGame starts in §v" + gameCountdown + " §ps");
            Server.broadcastNote(Instrument.STICKS, Note.natural(1, Note.Tone.A));
          }
          case 0 -> {
            Server.broadcastTDTitle("§wgo", "", Duration.ofSeconds(1));
            LoungeBridgeServer.broadcastLoungeBridgeTDMessage("§pGame starts §vnow");
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
    LoungeBridgeServer.broadcastLoungeBridgeTDMessage("§wGame closes in 10 seconds");

    Server.runTaskLaterSynchrony(LoungeBridgeServer::closeGame6, 6 * 20,
        BasicLoungeBridge.getPlugin());

    Server.runTaskLaterSynchrony(() -> {
      LoungeBridgeServer.broadcastLoungeBridgeTDMessage("§wGame closed");
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
