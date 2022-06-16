package de.timesnake.basic.loungebridge.util.user;

import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.chat.Chat;
import de.timesnake.basic.bukkit.util.user.User;
import de.timesnake.basic.game.util.StatUser;
import de.timesnake.basic.loungebridge.core.SpectatorManager;
import de.timesnake.basic.loungebridge.core.main.BasicLoungeBridge;
import de.timesnake.basic.loungebridge.util.server.LoungeBridgeServer;
import de.timesnake.channel.util.message.ChannelDiscordMessage;
import de.timesnake.channel.util.message.MessageType;
import de.timesnake.library.basic.util.Status;
import de.timesnake.library.packets.util.packet.ExPacketPlayOutEntityEffect;
import org.bukkit.entity.Player;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;

public abstract class SpectatorUser extends StatUser {

    public SpectatorUser(Player player) {
        super(player);
    }

    public void joinSpectator() {
        if (!this.getStatus().equals(Status.User.SPECTATOR)) {
            this.setStatus(Status.User.OUT_GAME);
        }

        this.setDefault();
        this.setCollitionWithEntites(false);
        this.setAllowFlight(true);
        this.setFlying(true);
        this.setInvulnerable(true);
        this.lockInventory();
        this.lockInventoryItemMove();

        Server.runTaskLaterSynchrony(() -> {
            this.setAllowFlight(true);
            this.setFlying(true);
        }, 10, BasicLoungeBridge.getPlugin());

        // show other spectators ans hide for ingame users
        for (User user : Server.getUsers()) {
            if (Status.User.IN_GAME.equals(user.getStatus()) || Status.User.PRE_GAME.equals(user.getStatus()) || Status.User.ONLINE.equals(user.getStatus())) {

                user.hideUser(this);

                this.sendPacket(ExPacketPlayOutEntityEffect.wrap(user.getPlayer(),
                        ExPacketPlayOutEntityEffect.Effect.GLOWING, ((byte) 0), Integer.MAX_VALUE, false, true, true));

            } else if (Status.User.OUT_GAME.equals(user.getStatus()) || Status.User.SPECTATOR.equals(user.getStatus())) {
                user.showUser(this);
            }
        }

        // remove from team chat
        if (this.getTeam() != null) {
            Chat teamChat = Server.getChat(this.getTeam().getName());
            if (teamChat != null) {
                teamChat.removeWriter(this);
                teamChat.removeListener(this);
            }
        }

        // set tablist team
        LoungeBridgeServer.getGameTablist().removeEntry(this);
        LoungeBridgeServer.getGameTablist().addRemainEntry(this);

        if (this.getTeam() == null) {
            this.teleportToSpectatorSpawn();
        }

        this.setSideboard(LoungeBridgeServer.getSpectatorSideboard());

        // if game has ended, nothing to do
        if (LoungeBridgeServer.getState().equals(LoungeBridgeServer.State.CLOSING)
                || LoungeBridgeServer.getState().equals(LoungeBridgeServer.State.STOPPED)) {
            return;
        }

        //remove from global chat
        Server.getGlobalChat().removeWriter(this);

        // add to spectator chat
        LoungeBridgeServer.getSpectatorChat().addWriter(this);
        LoungeBridgeServer.getSpectatorChat().addListener(this);

        // set spec tools
        this.setSpectatorInventory();

        if (LoungeBridgeServer.isDiscord()) {
            LinkedHashMap<String, List<UUID>> uuidsByTeam = new LinkedHashMap<>();
            uuidsByTeam.put(LoungeBridgeServer.DISCORD_SPECTATOR, List.of(this.getUniqueId()));
            Server.getChannel().sendMessage(new ChannelDiscordMessage<>(Server.getName(),
                    MessageType.Discord.MOVE_TEAMS,
                    new ChannelDiscordMessage.Allocation(uuidsByTeam)));
        }
    }

    public void setSpectatorInventory() {
        this.setItem(SpectatorManager.LEAVE_ITEM.cloneWithId());
        this.setItem(SpectatorManager.USER_INV.cloneWithId());
        this.setItem(SpectatorManager.SPEED.cloneWithId());
        this.setItem(SpectatorManager.GLOWING.cloneWithId().enchant());
        this.setItem(SpectatorManager.FLYING.cloneWithId().enchant());
    }

    public void openGameUserInventory() {
        this.openInventory(LoungeBridgeServer.getSpectatorManager().getGameUserInventory());
    }

    public void teleportToSpectatorSpawn() {
        this.teleport(LoungeBridgeServer.getSpectatorSpawn());
    }

}
