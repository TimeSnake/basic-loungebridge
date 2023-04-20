/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.basic.loungebridge.util.user;

import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.user.UserDamage;
import de.timesnake.basic.bukkit.util.user.scoreboard.TablistGroupType;
import de.timesnake.basic.bukkit.util.user.scoreboard.TablistableGroup;
import de.timesnake.basic.game.util.game.Team;
import de.timesnake.basic.game.util.user.StatUser;
import de.timesnake.basic.loungebridge.core.main.BasicLoungeBridge;
import de.timesnake.basic.loungebridge.util.server.LoungeBridgeServer;
import de.timesnake.basic.loungebridge.util.tool.listener.GameUserQuitListener;
import de.timesnake.basic.loungebridge.util.tool.listener.SpectatorUserJoinListener;
import de.timesnake.library.chat.ExTextColor;
import java.time.Duration;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

public abstract class GameUser extends StatUser {

    protected boolean isLeaving;

    protected Kit kit;

    protected Integer kills = 0;
    protected Integer killStreak = 0;
    protected Integer highestKillStreak = 0;
    protected Integer deaths = 0;

    protected Integer bowShots = 0;
    protected Integer bowHitTarget = 0;
    protected Integer bowHits = 0;

    protected Integer longestShot = 0;

    protected boolean kitLoaded = false;

    private boolean playedGame = false;

    private float gameCoins = 0;

    private BukkitTask respawnTask;

    public GameUser(Player player) {
        super(player);
        if (LoungeBridgeServer.areKitsEnabled()) {
            Integer kitId = this.getDatabase().getKit();

            if (kitId != null) {
                this.kit = LoungeBridgeServer.getGame().getKitManager().getKit(kitId).orElse(null);
            }
        }
        this.isLeaving = false;

        if (LoungeBridgeServer.getGame().hasTexturePack()) {
            this.setTexturePack(LoungeBridgeServer.getGame().getTexturePackLink());
        }
    }

    protected void loadGameSettings() {
        this.setFlying(false);
        this.setAllowFlight(false);
    }

    @Override
    public TablistableGroup getTablistGroup(TablistGroupType type) {
        if (type.equals(de.timesnake.basic.loungebridge.util.user.TablistGroupType.GAME)) {
            return LoungeBridgeServer.getTablistGameTeam();
        }
        return super.getTablistGroup(type);
    }

    @Override
    public void joinSpectator() {
        super.joinSpectator();

        LoungeBridgeServer.getToolManager().applyOnTools(GameUserQuitListener.class,
                t -> t.onGameUserQuit(this));

        LoungeBridgeServer.getToolManager().applyOnTools(SpectatorUserJoinListener.class,
                t -> t.onSpectatorUserJoin(this));

        LoungeBridgeServer.getDiscordManager().onUserJoinSpectator(this);
    }

    public boolean isLeaving() {
        return isLeaving;
    }

    public void setLeaving(boolean isLeaving) {
        this.isLeaving = isLeaving;
    }

    public void playedGame() {
        this.playedGame = true;
    }

    public boolean hasPlayedGame() {
        return playedGame;
    }

    public Kit getKit() {
        return kit;
    }

    public void setKit(Kit kit) {
        this.kit = kit;
    }

    public boolean applyKit() {
        if (this.kitLoaded) {
            return false;
        }

        if (this.kit == null) {
            return false;
        }

        this.kitLoaded = true;

        this.kit.getApplier().forEach(a -> a.accept(this));
        return true;
    }

    public void changeKitTo(Kit kit) {
        this.clearInventory();
        this.removePotionEffects();

        this.kit = kit;
        kit.getApplier().forEach(a -> a.accept(this));
    }

    public Integer getKills() {
        return kills;
    }

    public void setKills(Integer kills) {
        this.kills = kills;
    }

    public Integer getKillStreak() {
        return this.killStreak;
    }

    public Integer getHighestKillStreak() {
        return this.highestKillStreak;
    }

    public void addKill() {
        this.kills++;
        this.killStreak++;
        if ((this.killStreak % 5 == 0 && this.killStreak != 0) || this.killStreak == 3) {
            this.broadcastKillStreak();
        }
    }

    public void broadcastKillStreak() {
        LoungeBridgeServer.broadcastGameMessage(this.getChatNameComponent()
                .append(Component.text(" has a kill-streak of ", ExTextColor.PUBLIC))
                .append(Component.text(this.killStreak, ExTextColor.VALUE)));
    }

    public Integer getDeaths() {
        return deaths;
    }

    public void setDeaths(Integer deaths) {
        this.deaths = deaths;
        if (this.killStreak > this.highestKillStreak) {
            this.highestKillStreak = this.killStreak;
        }
        this.killStreak = 0;
    }

    public void addDeath() {
        this.deaths++;

        if (this.killStreak > this.highestKillStreak) {
            this.highestKillStreak = this.killStreak;
        }

        this.killStreak = 0;

        if (this.getLastDamager() != null && this.getLastDamager().getDamageType()
                .equals(UserDamage.DamageType.INSTANT)) {

            GameUser damager = ((GameUser) this.getLastDamager().getDamager());
            damager.addKill();

            if (this.getLastDamager().getDamageType().equals(UserDamage.DamageType.PLAYER_BOW)) {
                int distance =
                        (int) this.getLastDamager().getUserLocation()
                                .distance(this.getLastDamager().getDamagerLocation());
                damager.checkLongestShot(distance);
            }

        } else {
            Player killer = this.getPlayer().getKiller();
            if (killer != null) {
                GameUser killerUser = (GameUser) Server.getUser(killer);
                killerUser.addKill();
                Team killerTeam = killerUser.getTeam();
                if (killerTeam != null) {
                    killerTeam.addKill();
                }

                if (this.getLastDamager() != null) {
                    GameUser damager = ((GameUser) this.getLastDamager().getDamager());

                    if (!damager.equals(killerUser)) {
                        return;
                    }

                    if (this.getLastDamager().getDamageType()
                            .equals(UserDamage.DamageType.PLAYER_BOW)) {
                        int distance =
                                (int) this.getLastDamager().getUserLocation()
                                        .distance(this.getLastDamager().getDamagerLocation());
                        damager.checkLongestShot(distance);
                    }
                }
            }
        }

        Team team = this.getTeam();
        if (team != null) {
            team.addDeath();
        }
    }

    public Double getKillDeathRatio() {
        return this.kills / ((double) this.deaths);
    }

    public Integer getBowShots() {
        return bowShots;
    }

    public void setBowShots(Integer bowShots) {
        this.bowShots = bowShots;
    }

    public void addBowShot() {
        this.bowShots++;
    }

    public Integer getBowHitTarget() {
        return bowHitTarget;
    }

    public void setBowHitTarget(Integer bowHitTarget) {
        this.bowHitTarget = bowHitTarget;
    }

    public void addBowHitTarget() {
        this.bowHitTarget++;
    }

    public Integer getBowHits() {
        return this.bowHits;
    }

    public void setBowHits(Integer bowHits) {
        this.bowHits = bowHits;
    }

    public void addBowHit() {
        this.bowHits++;
    }

    public boolean checkLongestShot(Integer distance) {
        if (this.longestShot < distance) {
            this.longestShot = distance;
            return true;
        }
        return false;
    }

    public Integer getLongestShot() {
        return longestShot;
    }

    public Component getHealthDisplay() {
        double health = this.getHealth() / 2;
        if (health - ((int) health) >= 0.25 && health - ((int) health) < 0.75) {
            health = ((int) health) + 0.5;
        } else {
            health = Math.round(health);
        }

        if (health == 0) {
            health = 0.5;
        }

        return Component.text(health + "❤", ExTextColor.WARNING);
    }

    @Override
    public void addCoins(float coins, boolean sendMessage) {
        super.addCoins(coins, sendMessage);
        this.gameCoins += coins;
    }

    public float getGameCoins() {
        return gameCoins;
    }

    public final void joinGame() {
        this.onGameJoin();
    }

    public final void stopGame() {
        if (this.respawnTask != null) {
            this.respawnTask.cancel();
        }
        this.onGameStop();
    }

    public final void respawn() {
        this.onGameRespawn();
    }

    public final void respawnDelayed(int seconds) {
        this.lockLocation();
        this.lockInventory();
        this.lockBlockBreakPlace();

        this.respawnTask = Server.runTaskTimerSynchrony(time -> {
            this.showTDTitle("§w" + time, "", Duration.ofSeconds(1));

            if (time == 0) {
                this.unlockLocation();
                this.unlockInventory();
                this.unlockBlockBreakPlace();
                this.onGameRespawn();
            }
        }, seconds, true, 20, 0, BasicLoungeBridge.getPlugin());
    }

    public void onGameJoin() {

    }

    public void onGameStart() {

    }

    public void onGameStop() {

    }

    public void onGameRespawn() {

    }
}
