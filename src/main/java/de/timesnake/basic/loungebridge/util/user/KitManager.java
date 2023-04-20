/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.basic.loungebridge.util.user;

import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.user.event.AsyncUserMoveEvent;
import de.timesnake.basic.bukkit.util.user.inventory.ExInventory;
import de.timesnake.basic.loungebridge.core.main.BasicLoungeBridge;
import de.timesnake.basic.loungebridge.util.server.LoungeBridgeServer;
import de.timesnake.library.extension.util.player.UserMap;
import java.util.Collection;
import java.util.Optional;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitTask;

public abstract class KitManager<K extends Kit> implements Listener {

    protected static final double DISTANCE = 3d;

    protected UserMap<GameUser, BukkitTask> informedUsers = new UserMap<>();
    protected ExInventory changeInventory;
    protected boolean inGameChange;

    public KitManager(boolean inGameChange) {
        this.inGameChange = inGameChange;

        if (inGameChange) {
            this.initInGameChange();
        }

        Server.registerListener(this, BasicLoungeBridge.getPlugin());
    }

    public Optional<K> getKit(int index) {
        return this.getKits().stream()
                .filter(k -> k.getId().equals(index))
                .findFirst();
    }

    public abstract Collection<K> getKits();

    protected void initInGameChange() {
        this.changeInventory = new ExInventory(this.getKits().size(), "Kit Selection");

        for (K kit : this.getKits()) {
            this.changeInventory.addItem(kit.createDisplayItem(event -> {
                GameUser user = ((GameUser) event.getUser());
                user.changeKitTo(kit);
                user.sendPluginTDMessage(LoungeBridgeServer.getGamePlugin(),
                        "§sChanged kit to §v" + kit.getName());
                user.closeInventory();
            }));
        }
    }

    @EventHandler
    public void onUserMove(AsyncUserMoveEvent e) {
        GameUser user = ((GameUser) e.getUser());

        if (!user.isInGame()) {
            return;
        }

        if (this.isInChangeArea(user)) {
            if (user.isSneaking()) {
                Server.runTaskSynchrony(() -> user.openInventory(this.changeInventory),
                        BasicLoungeBridge.getPlugin());
            } else {
                Server.runTaskSynchrony(() -> {
                    if (!this.informedUsers.containsKey(user)) {
                        this.informedUsers.put(user, null);
                        user.sendPluginTDMessage(LoungeBridgeServer.getGamePlugin(),
                                "§sSneak to open kit selection");
                    }
                }, BasicLoungeBridge.getPlugin());
            }
        } else {
            Server.runTaskSynchrony(() -> {
                if (this.informedUsers.get(user) == null) {
                    Server.runTaskLaterAsynchrony(() -> this.informedUsers.remove(user), 20,
                            BasicLoungeBridge.getPlugin());
                }
            }, BasicLoungeBridge.getPlugin());
        }
    }

    public boolean isInChangeArea(GameUser user) {
        return false;
    }
}
