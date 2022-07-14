package de.timesnake.basic.loungebridge.core;

import com.destroystokyo.paper.event.player.PlayerStopSpectatingEntityEvent;
import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.chat.Chat;
import de.timesnake.basic.bukkit.util.chat.ChatColor;
import de.timesnake.basic.bukkit.util.user.User;
import de.timesnake.basic.bukkit.util.user.event.*;
import de.timesnake.basic.game.util.TeamUser;
import de.timesnake.basic.loungebridge.core.main.BasicLoungeBridge;
import de.timesnake.basic.loungebridge.util.chat.Plugin;
import de.timesnake.basic.loungebridge.util.server.LoungeBridgeServer;
import de.timesnake.basic.loungebridge.util.server.LoungeBridgeServerManager;
import de.timesnake.basic.loungebridge.util.user.GameUser;
import de.timesnake.basic.loungebridge.util.user.OfflineUser;
import de.timesnake.basic.loungebridge.util.user.SpectatorUser;
import de.timesnake.library.basic.util.Status;
import org.bukkit.GameMode;
import org.bukkit.block.Block;
import org.bukkit.block.ShulkerBox;
import org.bukkit.block.data.type.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.UUID;

public class UserManager implements Listener {

    private static final int REJOIN_TIME = 120; // in seconds

    private final HashMap<UUID, OfflineUser> offlineUsersByUniqueId = new HashMap<>();
    private final HashMap<UUID, BukkitTask> offlineUserRemoveTaskByUniqueId = new HashMap<>();

    public UserManager() {
        Server.registerListener(this, BasicLoungeBridge.getPlugin());
    }

    @EventHandler
    public void onUserJoin(UserJoinEvent e) {
        GameUser user = (GameUser) e.getUser();
        String task = user.getTask();

        if (this.offlineUsersByUniqueId.containsKey(user.getUniqueId())) {
            OfflineUser offlineUser = this.offlineUsersByUniqueId.remove(user.getUniqueId());
            this.offlineUserRemoveTaskByUniqueId.remove(user.getUniqueId()).cancel();
            offlineUser.loadInto(user);
            LoungeBridgeServer.onGameUserRejoin(user);
        } else if (task != null && task.equalsIgnoreCase(LoungeBridgeServer.getGame().getName()) && user.getStatus().equals(Status.User.PRE_GAME)) {

            user.getInventory().clear();
            user.heal();
            user.setInvulnerable(false);
            user.setAllowFlight(false);
            user.setFlying(false);
            user.setGravity(true);
            user.setFlySpeed((float) 0.2);
            user.setWalkSpeed((float) 0.2);
            user.setGameMode(GameMode.ADVENTURE);
            user.setFireTicks(0);
            user.removePotionEffects();

            user.updateTeam();

            if (user.getTeam() != null && LoungeBridgeServer.getServerTeamAmount() > 0) {
                Chat teamChat = Server.getChat(user.getTeam().getName());
                if (teamChat != null) {
                    teamChat.addWriter(user);
                    teamChat.addListener(user);
                    Server.getGlobalChat().removeWriter(user);
                }
            }

            user.joinGame();

            for (User otherUser : Server.getUsers()) {
                if (otherUser.getStatus().equals(Status.User.SPECTATOR) || otherUser.getStatus().equals(Status.User.OUT_GAME)) {
                    otherUser.showUser(user);
                    user.hideUser(otherUser);
                } else {
                    user.showUser(otherUser);
                    otherUser.showUser(user);
                }
            }

            LoungeBridgeServer.checkGameStart();

            LoungeBridgeServer.updateSpectatorInventory();
        } else {
            user.setStatus(Status.User.SPECTATOR);
            user.joinSpectator();
        }
    }

    public void clearRejoinUsers() {
        this.offlineUsersByUniqueId.clear();
    }

    @EventHandler
    public void onUserQuit(UserQuitEvent e) {
        User user = e.getUser();

        if (LoungeBridgeServer.isRejoiningAllowed() && !user.getStatus().equals(Status.User.SPECTATOR) && LoungeBridgeServer.isGameRunning()) {
            OfflineUser offlineUser = LoungeBridgeServer.loadOfflineUser(((GameUser) user));
            this.offlineUsersByUniqueId.put(user.getUniqueId(), offlineUser);
            Server.printText(Plugin.LOUNGE, "Saved user " + user.getChatName(), "User");
            this.offlineUserRemoveTaskByUniqueId.put(user.getUniqueId(),
                    Server.runTaskLaterSynchrony(() -> this.offlineUsersByUniqueId.remove(user.getUniqueId()),
                            20 * REJOIN_TIME, BasicLoungeBridge.getPlugin()));
        }

        if (user.getStatus().equals(Status.User.IN_GAME) || user.getStatus().equals(Status.User.PRE_GAME)) {
            if (!LoungeBridgeServer.isGameRunning()) {
                LoungeBridgeServerManager.getInstance().onGameUserQuitBeforeStart((GameUser) user);
            } else {
                LoungeBridgeServerManager.getInstance().onGameUserQuit((GameUser) user);
            }

            LoungeBridgeServer.updateSpectatorInventory();
        }

        ((TeamUser) user).setTeam(null);

    }

    @EventHandler
    public void onPlayerDeath(UserDeathEvent e) {
        User user = e.getUser();

        if (user == null) {
            return;
        }

        if (user.isInGame()) {
            ((GameUser) user).addDeath();
        }
    }


    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent e) {
        User user = Server.getUser(e.getPlayer());

        if (user == null) {
            return;
        }

        if (user.getStatus().equals(Status.User.OUT_GAME)) {
            ((GameUser) user).joinSpectator();
        }
    }


    @EventHandler
    public void onBowShoot(EntityShootBowEvent e) {
        if (e.getEntity() instanceof Player) {
            User user = Server.getUser(((Player) e.getEntity()));

            if (user == null) {
                return;
            }

            if (user.isInGame()) {
                ((GameUser) user).addBowShot();
            }
        }
    }

    @EventHandler
    public void onHit(ProjectileHitEvent e) {
        if (e.getEntity().getShooter() instanceof Player) {
            if (e.getHitEntity() instanceof Player) {
                User shooter = Server.getUser(((Player) e.getEntity().getShooter()));
                User hitUser = Server.getUser(((Player) e.getHitEntity()));

                if (shooter == null || hitUser == null) {
                    return;
                }

                if (shooter.isInGame()) {
                    ((GameUser) shooter).addBowHitTarget();
                }
                if (hitUser.isInGame()) {
                    ((GameUser) hitUser).addBowHit();
                }
            }
        }
    }

    @EventHandler
    public void onUserDropItem(UserDropItemEvent e) {
        User user = e.getUser();

        if (user.isService()) {
            return;
        }

        if (!LoungeBridgeServer.isGameRunning()) {
            e.setCancelled(true);
        }

        Status.User status = user.getStatus();

        if (status.equals(Status.User.SPECTATOR) || status.equals(Status.User.OUT_GAME)) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onUserDamage(UserDamageEvent e) {
        User user = e.getUser();

        if (user.isService()) {
            return;
        }

        if (!LoungeBridgeServer.isGameRunning()) {
            e.setCancelled(true);
            e.setCancelDamage(true);
        }

        Status.User status = user.getStatus();
        if (status.equals(Status.User.SPECTATOR) || status.equals(Status.User.OUT_GAME)) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent e) {
        if (e.getDamager() instanceof Player) {
            User user = Server.getUser(((Player) e.getDamager()));
            if (user == null) {
                return;
            }
            Status.User status = user.getStatus();

            if (user.isService()) {
                return;
            }

            if (status.equals(Status.User.SPECTATOR) || status.equals(Status.User.OUT_GAME)) {
                e.setCancelled(true);
                user.getPlayer().setFireTicks(0);
            }
        }
    }

    @EventHandler
    public void onUserDamageByUser(UserDamageByUserEvent e) {
        Status.User status = e.getUserDamager().getStatus();

        GameUser clickedUser = (GameUser) e.getUser();
        GameUser user = (GameUser) e.getUserDamager();

        if (!LoungeBridgeServer.isTeamMateDamage()) {
            if (clickedUser.isTeamMate(user)) {
                user.sendPluginMessage(LoungeBridgeServer.getGamePlugin(), ChatColor.PERSONAL + "You can't damage "
                        + "your teammate");
                e.setCancelled(true);
                return;
            }
        }

        if (status.equals(Status.User.SPECTATOR) || status.equals(Status.User.OUT_GAME)) {
            if (!user.isService()) {
                e.setCancelled(true);
                e.setCancelDamage(true);
            }

            if (clickedUser.isInGame()) {
                user.setGameMode(GameMode.SPECTATOR);
                user.setSpectatorTarget(clickedUser.getPlayer());
            }
        }
    }

    @EventHandler
    public void onEntityInteract(PlayerInteractEntityEvent e) {
        SpectatorUser user = (SpectatorUser) Server.getUser(e.getPlayer());
        Status.User status = user.getStatus();

        if (!(e.getRightClicked() instanceof Player)) {
            return;
        }

        GameUser clickedUser = (GameUser) Server.getUser(((Player) e.getRightClicked()));

        if (status.equals(Status.User.SPECTATOR) || status.equals(Status.User.OUT_GAME)) {
            if (!user.isService()) {
                e.setCancelled(true);
            }
            if (clickedUser.isInGame()) {
                user.openInventory(clickedUser.getInventory());
            }
        }
    }

    @EventHandler
    public void onPlayerStopSpectatingEntity(PlayerStopSpectatingEntityEvent e) {
        User user = Server.getUser(e.getPlayer());

        user.setGameMode(GameMode.ADVENTURE);
    }

    @EventHandler
    public void onPlayerPickUpItem(PlayerAttemptPickupItemEvent e) {
        User user = Server.getUser(e.getPlayer());

        if (user == null) {
            return;
        }

        if (user.isService()) {
            return;
        }

        if (!LoungeBridgeServer.isGameRunning()) {
            e.setCancelled(true);
            return;
        }

        Status.User status = user.getStatus();
        if (status.equals(Status.User.SPECTATOR) || status.equals(Status.User.OUT_GAME)) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerPickUpArrow(PlayerPickupArrowEvent e) {
        User user = Server.getUser(e.getPlayer());

        if (user == null) {
            return;
        }

        if (user.isService()) {
            return;
        }

        if (!LoungeBridgeServer.isGameRunning()) {
            e.setCancelled(true);
            return;
        }

        Status.User status = user.getStatus();
        if (status.equals(Status.User.SPECTATOR) || status.equals(Status.User.OUT_GAME)) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        User user = Server.getUser(e.getPlayer());

        if (user == null) {
            return;
        }

        if (user.isService()) {
            return;
        }

        if (!LoungeBridgeServer.isGameRunning()) {
            e.setCancelled(true);
            return;
        }

        Status.User status = user.getStatus();
        if (status.equals(Status.User.SPECTATOR) || status.equals(Status.User.OUT_GAME)) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        User user = Server.getUser(e.getWhoClicked().getUniqueId());

        if (user == null) {
            return;
        }

        if (user.isService()) {
            return;
        }

        if (!LoungeBridgeServer.isGameRunning()) {
            e.setCancelled(true);
            user.closeInventory();
            return;
        }

        Status.User status = user.getStatus();
        if (status.equals(Status.User.SPECTATOR) || status.equals(Status.User.OUT_GAME)) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onSwapHandItem(PlayerSwapHandItemsEvent e) {
        User user = Server.getUser(e.getPlayer());

        if (user == null) {
            return;
        }

        if (user.isService()) {
            return;
        }

        if (!LoungeBridgeServer.isGameRunning()) {
            e.setCancelled(true);
            return;
        }

        Status.User status = user.getStatus();
        if (status.equals(Status.User.SPECTATOR) || status.equals(Status.User.OUT_GAME)) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onFoodLevelChange(FoodLevelChangeEvent e) {
        if (!(e.getEntity() instanceof Player)) {
            return;
        }

        User user = Server.getUser(((Player) e.getEntity()));

        if (user == null) {
            return;
        }

        if (user.isService()) {
            return;
        }

        if (!LoungeBridgeServer.isGameRunning() || !user.isInGame()) {
            e.setCancelled(true);
            return;
        }

        Status.User status = user.getStatus();
        if (status.equals(Status.User.SPECTATOR) || status.equals(Status.User.OUT_GAME)) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent e) {
        if (!(e.getPlayer() instanceof Player)) {
            return;
        }

        User user = Server.getUser(((Player) e.getPlayer()));

        if (user == null) {
            return;
        }

        if (user.isService()) {
            return;
        }

        if (!LoungeBridgeServer.isGameRunning()) {
            e.setCancelled(true);
            user.closeInventory();
            return;
        }

        if (e.getInventory().getHolder() instanceof Chest || e.getInventory().getHolder() instanceof ShulkerBox) {
            Status.User status = user.getStatus();
            if (status.equals(Status.User.SPECTATOR) || status.equals(Status.User.OUT_GAME)) {
                e.setCancelled(true);
                Inventory inv = e.getInventory();
                user.openInventory(inv);
            }
        }
    }

    @EventHandler
    public void onRide(VehicleEnterEvent e) {
        if (!(e.getEntered() instanceof Player)) {
            return;
        }

        User user = Server.getUser(((Player) e.getEntered()));

        if (user == null) {
            return;
        }

        if (user.isService()) {
            return;
        }

        Status.User status = user.getStatus();
        if (status.equals(Status.User.SPECTATOR) || status.equals(Status.User.OUT_GAME)) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e) {
        User user = Server.getUser(e.getPlayer());

        if (user == null) {
            return;
        }

        if (user.isService()) {
            return;
        }

        if (!LoungeBridgeServer.isGameRunning()) {
            e.setUseInteractedBlock(Event.Result.DENY);
            e.setCancelled(true);

            Block block = e.getClickedBlock();
            if (block != null) {
                block.setType(block.getType(), true);
                block.setBlockData(block.getBlockData());
            }
        }

        Status.User status = user.getStatus();
        if (!(status.equals(Status.User.SPECTATOR) || status.equals(Status.User.OUT_GAME))) {
            return;
        }

        e.setUseInteractedBlock(Event.Result.DENY);
        e.setCancelled(true);
    }
}
