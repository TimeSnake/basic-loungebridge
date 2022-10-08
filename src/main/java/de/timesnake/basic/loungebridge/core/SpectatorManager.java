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

import com.destroystokyo.paper.event.player.PlayerPickupExperienceEvent;
import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.user.ExInventory;
import de.timesnake.basic.bukkit.util.user.ExItemStack;
import de.timesnake.basic.bukkit.util.user.User;
import de.timesnake.basic.bukkit.util.user.event.*;
import de.timesnake.basic.bukkit.util.user.scoreboard.ItemHoldClick;
import de.timesnake.basic.loungebridge.core.main.BasicLoungeBridge;
import de.timesnake.basic.loungebridge.util.chat.Plugin;
import de.timesnake.basic.loungebridge.util.user.SpectatorUser;
import de.timesnake.library.basic.util.Status;
import de.timesnake.library.basic.util.chat.ExTextColor;
import de.timesnake.library.packets.util.listener.PacketHandler;
import de.timesnake.library.packets.util.listener.PacketPlayOutListener;
import de.timesnake.library.packets.util.packet.ExPacket;
import de.timesnake.library.packets.util.packet.ExPacketPlayOut;
import de.timesnake.library.packets.util.packet.ExPacketPlayOutEntityMetadata;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class SpectatorManager implements UserInventoryClickListener, UserInventoryInteractListener,
        PacketPlayOutListener, Listener {

    // teleports the spectator to spawn if he goes lower than min y
    public static final Integer MIN_Y = -84;
    public static final ExItemStack USER_INV = new ExItemStack(1, Material.PLAYER_HEAD, "§9Teleporter");
    public static final ExItemStack GLOWING = new ExItemStack(2, Material.SPECTRAL_ARROW, "§6Glowing");
    public static final ExItemStack SPEED = new ExItemStack(3, Material.FEATHER, "§bSpeed");
    public static final ExItemStack FLYING = new ExItemStack(4, Material.RABBIT_FOOT, "§9Flying");
    public static final ExItemStack LEAVE_ITEM = new ExItemStack(8, Material.ANVIL, "§6Leave (hold right)");
    private static final Integer LEAVE_TIME = 2000;
    private final HashMap<User, ItemHoldClick> clickedLeaveUsers = new HashMap<>();
    private final HashMap<Integer, User> userHeadsById = new HashMap<>();
    private final Set<User> clickCooldownUsers = new HashSet<>();
    private ExInventory gameUserInv;
    private Set<User> glowingUsers = new HashSet<>();

    public SpectatorManager() {
        this.gameUserInv = Server.createExInventory(9, "Players");

        Server.getInventoryEventManager().addInteractListener(this, USER_INV, GLOWING, SPEED, FLYING, LEAVE_ITEM);
        Server.getPacketManager().addListener(this);
        Server.registerListener(this, BasicLoungeBridge.getPlugin());
    }

    public void updateSpectatorTools() {
        this.updateTeleportInventory();
        this.updateGlowReceivers();
        this.updateGlowingPlayers();
    }

    public void clearTools() {
        this.glowingUsers.clear();
    }

    private void updateTeleportInventory() {
        Server.getInventoryEventManager().removeClickListener(this);
        int inGame = Server.getInGameUsers().size();
        this.gameUserInv = Server.createExInventory(inGame == 0 ? 9 : (inGame + 8) / 9 * 9, "Players");
        this.userHeadsById.clear();
        int slot = 0;
        for (User user : Server.getInGameUsers()) {
            ExItemStack head = ExItemStack.getHead(user.getPlayer(), user.getChatName()).setLore("", "§7Click to " +
                    "teleport");
            this.userHeadsById.put(head.getId(), user);
            this.gameUserInv.setItemStack(slot, head);
            Server.getInventoryEventManager().addClickListener(this, head);
            slot++;
        }
    }

    public ExInventory getGameUserInventory() {
        return gameUserInv;
    }

    public void updateGlowReceivers() {
        this.sendUpdatePackets();
    }

    public void updateGlowingPlayers() {
        this.glowingUsers = new HashSet<>(Server.getInGameUsers());
        this.sendUpdatePackets();
    }

    private void sendUpdatePackets() {
        Collection<User> receivers = Server.getSpectatorUsers();
        receivers.addAll(Server.getOutGameUsers());

        for (User glowingUser : this.glowingUsers) {
            ExPacketPlayOut packet = ExPacketPlayOutEntityMetadata.wrap(glowingUser.getPlayer(),
                    ExPacketPlayOutEntityMetadata.DataType.UPDATE);
            for (User receiver : receivers) {
                if (((SpectatorUser) receiver).hasGlowingEnabled()) {
                    receiver.sendPacket(packet);
                }
            }
        }
    }

    @PacketHandler(type = ExPacket.Type.PLAY_OUT_ENTITY_METADATA, modify = true)
    public ExPacketPlayOut onPacketPlayOut(ExPacketPlayOut packet, Player receiver) {
        if (!(packet instanceof ExPacketPlayOutEntityMetadata)) {
            return packet;
        }

        Integer index = ((ExPacketPlayOutEntityMetadata) packet).getNMSIndex();
        if (index == null || index != 0) {
            return packet;
        }

        Integer entityId = ((ExPacketPlayOutEntityMetadata) packet).getEntityId();

        Player player = null;
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.getEntityId() == entityId) {
                player = p;
                break;
            }
        }

        if (player == null) {
            return packet;
        }

        if (!this.glowingUsers.contains(Server.getUser(player))) {
            return packet;
        }

        SpectatorUser user = (SpectatorUser) Server.getUser(receiver);

        if (!(user.hasGlowingEnabled())
                || !(user.getStatus().equals(Status.User.OUT_GAME) || user.getStatus().equals(Status.User.SPECTATOR))) {
            return packet;
        }

        packet = ((ExPacketPlayOutEntityMetadata) packet).cloneByte();
        ((ExPacketPlayOutEntityMetadata) packet).setGlowing(true);

        return packet;
    }

    @Override
    public void onUserInventoryInteract(UserInventoryInteractEvent e) {
        ExItemStack clickedItem = e.getClickedItem();
        SpectatorUser user = (SpectatorUser) e.getUser();

        if (this.clickCooldownUsers.contains(user)) {
            return;
        }

        this.clickCooldownUsers.add(user);
        Server.runTaskLaterSynchrony(() -> this.clickCooldownUsers.remove(user), 10, BasicLoungeBridge.getPlugin());

        if (clickedItem.equals(USER_INV)) {
            user.openGameUserInventory();
        } else if (clickedItem.equals(LEAVE_ITEM)) {

            if (e.getAction() == Action.RIGHT_CLICK_BLOCK || e.getAction() == Action.RIGHT_CLICK_AIR) {
                if (!this.clickedLeaveUsers.containsKey(user)) {
                    this.clickedLeaveUsers.put(user, new ItemHoldClick(LEAVE_TIME));
                } else {
                    if (this.clickedLeaveUsers.get(user).click()) {
                        user.sendActionBarText("");
                        user.switchToLobbyLast();
                    } else {
                        user.sendActionBarText("§cLeaving...");
                    }
                }
            }
        } else if (clickedItem.equals(GLOWING)) {
            user.setGlowingEnabled(!user.hasGlowingEnabled());
            if (user.hasGlowingEnabled()) {
                user.sendPluginMessage(Plugin.GAME, Component.text("Enabled glowing", ExTextColor.PERSONAL));
                clickedItem.enchant();
            } else {
                user.sendPluginMessage(Plugin.GAME, Component.text("Disabled glowing", ExTextColor.PERSONAL));
                clickedItem.disenchant();
            }
        } else if (clickedItem.equals(SPEED)) {
            user.setSpeedEnabled(!user.hasSpeedEnabled());
            if (user.hasSpeedEnabled()) {
                user.sendPluginMessage(Plugin.GAME, Component.text("Enabled speed", ExTextColor.PERSONAL));
                clickedItem.enchant();
            } else {
                user.sendPluginMessage(Plugin.GAME, Component.text("Disabled speed", ExTextColor.PERSONAL));
                clickedItem.disenchant();
            }
        } else if (clickedItem.equals(FLYING)) {
            user.setFlyEnabled(user.hasFlyEnabled());
            user.sendPluginMessage(Plugin.GAME, Component.text((user.getAllowFlight() ? "Enabled" : "Disabled") + " flying", ExTextColor.PERSONAL));
            if (user.getAllowFlight()) clickedItem.enchant();
            else clickedItem.disenchant();
        }

        user.setItem(clickedItem);
        user.updateInventory();
    }

    @EventHandler
    public void onUserLeave(UserQuitEvent e) {
        this.clickedLeaveUsers.remove(e.getUser());
    }

    @Override
    public void onUserInventoryClick(UserInventoryClickEvent e) {
        User user = e.getUser();
        ExItemStack clickedItem = e.getClickedItem();
        if (!this.userHeadsById.containsKey(clickedItem.getId())) {
            e.setCancelled(true);
            return;
        }

        User clickedUser = this.userHeadsById.get(clickedItem.getId());
        user.teleport(clickedUser);
        user.asSender(Plugin.GAME).sendPluginMessage(Component.text("Teleported to player ", ExTextColor.PERSONAL)
                .append(clickedUser.getChatNameComponent()));

        e.setCancelled(true);
    }

    @EventHandler
    public void onUserDamage(UserDamageEvent e) {
        User user = e.getUser();
        if (user.getStatus().equals(Status.User.SPECTATOR) || user.getStatus().equals(Status.User.OUT_GAME)) {
            e.setCancelled(true);
            if (user.getLocation().getY() < MIN_Y) {
                ((SpectatorUser) user).teleportToSpectatorSpawn();
            }
        }
    }

    @EventHandler
    public void onUserMove(UserMoveEvent e) {
        User user = e.getUser();
        if (user.getStatus().equals(Status.User.SPECTATOR) || user.getStatus().equals(Status.User.OUT_GAME)) {
            if (user.getLocation().getY() < MIN_Y) {
                ((SpectatorUser) user).teleportToSpectatorSpawn();
            }
        }
    }

    @EventHandler
    public void onPlayerPickupExperience(PlayerPickupExperienceEvent e) {
        User user = Server.getUser(e.getPlayer());
        if (user.getStatus().equals(Status.User.SPECTATOR) || user.getStatus().equals(Status.User.OUT_GAME)) {
            e.setCancelled(true);
        }
    }
}
