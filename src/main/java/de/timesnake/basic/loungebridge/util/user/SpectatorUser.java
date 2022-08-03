package de.timesnake.basic.loungebridge.util.user;

import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.chat.Chat;
import de.timesnake.basic.bukkit.util.user.User;
import de.timesnake.basic.game.util.StatUser;
import de.timesnake.basic.loungebridge.core.SpectatorManager;
import de.timesnake.basic.loungebridge.core.main.BasicLoungeBridge;
import de.timesnake.basic.loungebridge.util.server.LoungeBridgeServer;
import de.timesnake.library.basic.util.Status;
import de.timesnake.library.packets.util.packet.ExPacketPlayOutEntityEffect;
import org.bukkit.entity.Player;

public abstract class SpectatorUser extends StatUser {

    protected boolean glowingEnabled = false;
    protected boolean speedEnabled = false;
    protected boolean flyEnabled = true;

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

        this.glowingEnabled = true;
        this.speedEnabled = false;

        Server.runTaskLaterSynchrony(() -> {
            this.setAllowFlight(true);
            this.setFlying(true);
        }, 10, BasicLoungeBridge.getPlugin());

        // show other spectators and hide for ingame users
        for (User user : Server.getUsers()) {
            if (Status.User.IN_GAME.equals(user.getStatus()) || Status.User.PRE_GAME.equals(user.getStatus()) || Status.User.ONLINE.equals(user.getStatus())) {

                user.hideUser(this);

                this.sendPacket(ExPacketPlayOutEntityEffect.wrap(user.getPlayer(),
                        ExPacketPlayOutEntityEffect.Effect.GLOWING, ((byte) 0), Integer.MAX_VALUE, true, true, true));

            } else if (Status.User.OUT_GAME.equals(user.getStatus()) || Status.User.SPECTATOR.equals(user.getStatus())) {
                user.showUser(this);
            }
        }

        // remove from team chat
        if (this.getTeam() != null && this.getTeam().hasPrivateChat()) {
            Chat teamChat = Server.getChat(this.getTeam().getName());
            if (teamChat != null) {
                teamChat.removeWriter(this);
                teamChat.removeListener(this);
            }
        }

        // set tablist team
        if (!LoungeBridgeServer.getGame().hideTeams() || this.getStatus().equals(Status.User.SPECTATOR)) {
            LoungeBridgeServer.getGameTablist().removeEntry(this);
            LoungeBridgeServer.getGameTablist().addRemainEntry(this);
        }

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

        LoungeBridgeServer.getDiscordManager().onUserJoinSpectator(this);
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

    public boolean hasGlowingEnabled() {
        return glowingEnabled;
    }

    public boolean hasSpeedEnabled() {
        return speedEnabled;
    }

    public void setGlowingEnabled(boolean glowingEnabled) {
        this.glowingEnabled = glowingEnabled;
    }

    public void setSpeedEnabled(boolean speedEnabled) {
        this.speedEnabled = speedEnabled;
        this.setFlySpeed(this.speedEnabled ? 0.2F : 0.4F);
        this.setWalkSpeed(this.speedEnabled ? 0.2F : 0.4F);
    }

    public boolean hasFlyEnabled() {
        return flyEnabled;
    }

    public void setFlyEnabled(boolean flyEnabled) {
        this.flyEnabled = flyEnabled;
        this.setAllowFlight(flyEnabled);
        this.setFlying(flyEnabled);
    }

    public void rejoinGame() {
        this.glowingEnabled = false;
        this.speedEnabled = false;
    }
}
