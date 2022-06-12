package de.timesnake.basic.loungebridge.core;

import com.destroystokyo.paper.event.player.PlayerPickupExperienceEvent;
import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.chat.ChatColor;
import de.timesnake.basic.bukkit.util.user.ExInventory;
import de.timesnake.basic.bukkit.util.user.ExItemStack;
import de.timesnake.basic.bukkit.util.user.User;
import de.timesnake.basic.bukkit.util.user.event.*;
import de.timesnake.basic.bukkit.util.user.scoreboard.ItemHoldClick;
import de.timesnake.basic.loungebridge.util.chat.Plugin;
import de.timesnake.basic.loungebridge.util.user.SpectatorUser;
import de.timesnake.basic.packets.util.listener.PacketHandler;
import de.timesnake.basic.packets.util.listener.PacketPlayOutListener;
import de.timesnake.basic.packets.util.packet.ExPacket;
import de.timesnake.basic.packets.util.packet.ExPacketPlayOut;
import de.timesnake.basic.packets.util.packet.ExPacketPlayOutEntityMetadata;
import de.timesnake.library.basic.util.Status;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;

import java.util.*;

public class SpectatorManager implements UserInventoryClickListener, UserInventoryInteractListener,
        PacketPlayOutListener {

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
    private final Set<User> speedUsers = new HashSet<>();
    private ExInventory gameUserInv;
    private Set<User> glowingUsers = new HashSet<>();
    private Set<User> glowReceivers = new HashSet<>();

    public SpectatorManager() {
        this.gameUserInv = Server.createExInventory(9, "Players");

        Server.getInventoryEventManager().addInteractListener(this, USER_INV, GLOWING, SPEED, FLYING, LEAVE_ITEM);
        Server.getPacketManager().addListener(this);
    }

    public void updateSpectatorTools() {
        this.updateTeleportInventory();
        this.updateGlowReceivers();
        this.updateGlowingPlayers();
    }

    public void clearTools() {
        this.glowReceivers.clear();
        this.glowingUsers.clear();
        this.speedUsers.clear();
    }

    private void updateTeleportInventory() {
        Server.getInventoryEventManager().removeClickListener(this);
        int ingame = Server.getInGameUsers().size();
        this.gameUserInv = Server.createExInventory(ingame == 0 ? 9 : (ingame + 8) / 9 * 9, "Players");
        this.userHeadsById.clear();
        int slot = 0;
        for (User user : Server.getInGameUsers()) {
            ExItemStack head = new ExItemStack(user.getPlayer(), user.getChatName(), List.of("", "§7Click to " +
                    "teleport"));
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
        Collection<User> users = Server.getSpectatorUsers();
        users.addAll(Server.getOutGameUsers());
        this.glowReceivers = new HashSet<>(users);
        this.sendUpdatePackets();
    }

    public void updateGlowingPlayers() {
        this.glowingUsers = new HashSet<>(Server.getInGameUsers());
        this.sendUpdatePackets();
    }

    public void addGlowReceiver(User user) {
        this.glowReceivers.add(user);
        for (User glowingUser : this.glowingUsers) {
            ExPacketPlayOut packet = ExPacketPlayOutEntityMetadata.wrap(glowingUser.getPlayer(),
                    ExPacketPlayOutEntityMetadata.DataType.UPDATE);
            user.sendPacket(packet);
        }
    }

    public void removeGlowReceiver(User user) {
        this.glowReceivers.remove(user);
        for (User glowingUser : this.glowingUsers) {
            ExPacketPlayOut packet = ExPacketPlayOutEntityMetadata.wrap(glowingUser.getPlayer(),
                    ExPacketPlayOutEntityMetadata.DataType.UPDATE);
            user.sendPacket(packet);
        }
    }

    public void addGlowingPlayer(User user) {
        this.glowingUsers.add(user);
        ExPacketPlayOut packet = ExPacketPlayOutEntityMetadata.wrap(user.getPlayer(),
                ExPacketPlayOutEntityMetadata.DataType.UPDATE);
        for (User receiver : this.glowReceivers) {
            receiver.sendPacket(packet);
        }
    }

    private void sendUpdatePackets() {
        for (User glowingUser : this.glowingUsers) {
            ExPacketPlayOut packet = ExPacketPlayOutEntityMetadata.wrap(glowingUser.getPlayer(),
                    ExPacketPlayOutEntityMetadata.DataType.UPDATE);
            for (User receiver : this.glowReceivers) {
                receiver.sendPacket(packet);
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

        if (!this.glowReceivers.contains(Server.getUser(receiver))) {
            return packet;
        }

        packet = ((ExPacketPlayOutEntityMetadata) packet).cloneByte();
        ((ExPacketPlayOutEntityMetadata) packet).setGlowing(true);

        return packet;
    }

    @Override
    public void onUserInventoryInteract(UserInventoryInteractEvent e) {
        ExItemStack clickedItem = e.getClickedItem();
        User user = e.getUser();
        if (clickedItem.equals(USER_INV)) {
            ((SpectatorUser) user).openGameUserInventory();
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
            if (this.glowReceivers.contains(user)) {
                this.removeGlowReceiver(user);
                user.sendPluginMessage(Plugin.GAME, ChatColor.PERSONAL + "Disabled glowing");
                clickedItem.disenchant();
            } else {
                this.addGlowReceiver(user);
                user.sendPluginMessage(Plugin.GAME, ChatColor.PERSONAL + "Enabled glowing");
                clickedItem.enchant();
            }
        } else if (clickedItem.equals(SPEED)) {
            if (this.speedUsers.contains(user)) {
                this.speedUsers.remove(user);
                user.getPlayer().setFlySpeed(0.2F);
                user.getPlayer().setWalkSpeed(0.2F);
                user.sendPluginMessage(Plugin.GAME, ChatColor.PERSONAL + "Disabled speed");
                clickedItem.disenchant();
            } else {
                this.speedUsers.add(user);
                user.getPlayer().setFlySpeed(0.4F);
                user.getPlayer().setWalkSpeed(0.4F);
                user.sendPluginMessage(Plugin.GAME, ChatColor.PERSONAL + "Enabled speed");
                clickedItem.enchant();
            }
        } else if (clickedItem.equals(FLYING)) {
            user.setAllowFlight(!user.getAllowFlight());
            user.setFlying(user.getAllowFlight());
            user.sendPluginMessage(Plugin.GAME,
                    ChatColor.PERSONAL + (user.getAllowFlight() ? "Enabled" : "Disabled") + " flying");
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
        user.asSender(Plugin.GAME).sendPluginMessage(ChatColor.PERSONAL + "Teleported to player " + ChatColor.VALUE + clickedUser.getChatName());

        e.setCancelled(true);
    }

    @EventHandler
    public void onUserDamage(UserDamageEvent e) {
        User user = e.getUser();
        if (user.getStatus().equals(Status.User.SPECTATOR) || user.getStatus().equals(Status.User.OUT_GAME)) {
            e.setCancelled(true);
            if (user.getLocation().getY() < 0) {
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
