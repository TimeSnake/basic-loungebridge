package de.timesnake.basic.loungebridge.core;

import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.chat.Chat;
import de.timesnake.basic.bukkit.util.chat.ChatColor;
import de.timesnake.basic.bukkit.util.user.User;
import de.timesnake.basic.loungebridge.core.main.BasicLoungeBridge;
import de.timesnake.basic.loungebridge.util.chat.Plugin;
import de.timesnake.basic.loungebridge.util.server.LoungeBridgeServer;
import de.timesnake.basic.loungebridge.util.user.GameUser;
import de.timesnake.channel.util.message.ChannelDiscordMessage;
import de.timesnake.channel.util.message.MessageType;
import de.timesnake.library.basic.util.Status;
import org.bukkit.Instrument;
import org.bukkit.Note;
import org.bukkit.scheduler.BukkitTask;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

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
                        if (LoungeBridgeServer.areKitsEnabled()) {
                            for (User user : Server.getPreGameUsers()) {
                                ((GameUser) user).setKitItems();
                            }
                        }
                        Server.printText(Plugin.LOUNGE, "Preparing game ...");
                        Server.runTaskSynchrony(LoungeBridgeServer::prepareGame, BasicLoungeBridge.getPlugin());
                    }
                    case 5, 4, 3, 2 -> {
                        Server.broadcastTitle(ChatColor.RED + "" + gameCountdown, "", Duration.ofSeconds(1));
                        LoungeBridgeServer.broadcastLoungeBridgeMessage(ChatColor.PUBLIC + "The Game starts in " + ChatColor.VALUE + gameCountdown + ChatColor.PUBLIC + " seconds");
                        Server.broadcastNote(Instrument.STICKS, Note.natural(1, Note.Tone.A));
                    }
                    case 1 -> {
                        Server.broadcastTitle(ChatColor.RED + "" + gameCountdown, "", Duration.ofSeconds(1));
                        LoungeBridgeServer.broadcastLoungeBridgeMessage(ChatColor.PUBLIC + "The Game starts in " + ChatColor.VALUE + "1 " + ChatColor.PUBLIC + "second");
                        Server.broadcastNote(Instrument.STICKS, Note.natural(1, Note.Tone.A));
                    }
                    case 0 -> {
                        if (LoungeBridgeServer.areKitsEnabled()) {
                            for (User user : Server.getPreGameUsers()) {
                                ((GameUser) user).setKitItems();
                            }
                        }
                        LoungeBridgeServer.broadcastLoungeBridgeMessage(ChatColor.PUBLIC + "The Game starts " + ChatColor.VALUE + "now");
                        Server.broadcastNote(Instrument.STICKS, Note.natural(1, Note.Tone.A));
                        LoungeBridgeServer.setState(LoungeBridgeServer.State.RUNNING);
                        for (User user : Server.getPreGameUsers()) {
                            user.setStatus(Status.User.IN_GAME);
                            ((GameUser) user).playedGame();
                        }

                        Server.runTaskSynchrony(LoungeBridgeServer::startGame, BasicLoungeBridge.getPlugin());
                        this.gameCountdownTask.cancel();
                    }
                }
                gameCountdown--;
            }, 0, 20, BasicLoungeBridge.getPlugin());
        }
    }

    public void closeGame() {
        if (LoungeBridgeServer.getState() != LoungeBridgeServer.State.CLOSING) {
            LoungeBridgeServer.setState(LoungeBridgeServer.State.CLOSING);
            for (User user : Server.getInGameUsers()) {
                user.setDefault();
                user.getPlayer().setInvulnerable(true);
                user.lockLocation(true);
            }

            for (User user : Server.getSpectatorUsers()) {
                user.clearInventory();
            }

            LoungeBridgeServer.getLoungeBridgeUserManager().clearRejoinUsers();

            Chat specChat = LoungeBridgeServer.getSpectatorChat();
            for (User user : Server.getUsers()) {
                if (((GameUser) user).getTeam() != null) {
                    Chat teamChat = Server.getChat(((GameUser) user).getTeam().getName());
                    if (teamChat != null) {
                        teamChat.removeWriter(user);
                        teamChat.removeListener(user);
                    }
                }

                specChat.removeWriter(user);
                specChat.removeListener(user);

                Server.getGlobalChat().addWriter(user);
                Server.getGlobalChat().addListener(user);
                user.clearInventory();
            }

            if (LoungeBridgeServer.isDiscord()) {
                Server.runTaskLaterSynchrony(() -> {
                    LinkedHashMap<String, List<UUID>> uuidsByTeam = new LinkedHashMap<>();
                    uuidsByTeam.put(LoungeBridgeServer.DISCORD_LOUNGE,
                            Server.getUsers().stream().map(User::getUniqueId).collect(Collectors.toList()));
                    Server.getChannel().sendMessage(new ChannelDiscordMessage<>(Server.getName(),
                            MessageType.Discord.MOVE_TEAMS, new ChannelDiscordMessage.Allocation(uuidsByTeam)));
                }, 20 * 2, BasicLoungeBridge.getPlugin());
            }


            LoungeBridgeServer.getSpectatorManager().clearTools();
            LoungeBridgeServer.getStatsManager().sendStatSaveRequest();
            LoungeBridgeServer.getCoinsManager().sendCoinsSaveRequest();

            LoungeBridgeServer.broadcastLoungeBridgeMessage(ChatColor.WARNING + "The game closes in 10 seconds");

            Server.runTaskLaterSynchrony(() -> {
                LoungeBridgeServer.getStatsManager().saveGameStats();
                LoungeBridgeServer.getCoinsManager().saveGameCoins();
            }, 6 * 20, BasicLoungeBridge.getPlugin());

            Server.runTaskLaterSynchrony(() -> {
                LoungeBridgeServer.broadcastLoungeBridgeMessage(ChatColor.WARNING + "Game closed");
                Server.getChat().broadcastJoinQuit(false);
                for (User user : Server.getUsers()) {
                    if (!user.isAirMode() && !user.isService()) {
                        user.getDatabase().setKit(null);
                        ((GameUser) user).setTeam(null);
                    }

                    if (LoungeBridgeServer.getGame().hasTexturePack()) {
                        user.setTexturePack("https://timesnake.de/data/minecraft/resource_packs/texture_pack_base.zip");
                    }

                    user.switchToServer(LoungeBridgeServer.getTwinServer());
                }

                Server.runTaskLaterSynchrony(() -> {
                    this.resetGameCountdown();
                    LoungeBridgeServer.resetKillsAndDeaths();
                    LoungeBridgeServer.setState(LoungeBridgeServer.State.RESETTING);
                    LoungeBridgeServer.resetGame();
                    LoungeBridgeServer.getStatsManager().reset();
                    LoungeBridgeServer.getCoinsManager().reset();
                    LoungeBridgeServer.setState(LoungeBridgeServer.State.WAITING);
                    Server.getChat().broadcastJoinQuit(true);
                }, 5 * 20, BasicLoungeBridge.getPlugin());
            }, 12 * 20, BasicLoungeBridge.getPlugin());
        }
    }

    private void resetGameCountdown() {
        if (this.gameCountdownTask != null) {
            this.gameCountdownTask.cancel();
        }
        this.gameCountdown = COUNTDOWN_TIME;
        for (User user : Server.getNotServiceUsers()) {
            user.getPlayer().setLevel(0);
            user.getPlayer().setExp(0);
        }
    }

    public int getGameCountdown() {
        return gameCountdown;
    }
}
