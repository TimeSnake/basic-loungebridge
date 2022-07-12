package de.timesnake.basic.loungebridge.util.user;

import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.chat.ChatColor;
import de.timesnake.basic.bukkit.util.user.ExItemStack;
import de.timesnake.basic.bukkit.util.user.UserDamage;
import de.timesnake.basic.bukkit.util.user.scoreboard.TablistGroupType;
import de.timesnake.basic.bukkit.util.user.scoreboard.TablistableGroup;
import de.timesnake.basic.game.util.Team;
import de.timesnake.basic.loungebridge.util.server.LoungeBridgeServer;
import de.timesnake.library.basic.util.Status;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public abstract class GameUser extends SpectatorUser {

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

    public GameUser(Player player) {
        super(player);
        if (LoungeBridgeServer.areKitsEnabled()) {
            Integer kitId = this.getDatabase().getKit();

            if (kitId != null) {
                try {
                    this.kit = LoungeBridgeServer.getKit(kitId);
                } catch (KitNotDefinedException ignored) {
                }
            }
        }
        this.isLeaving = false;

        this.setFlying(false);
        this.setAllowFlight(false);

        if (LoungeBridgeServer.getGame().hasTexturePack()) {
            this.setTexturePack(LoungeBridgeServer.getGame().getTexturePackLink());
        }
    }

    @Override
    public TablistableGroup getTablistGroup(TablistGroupType type) {
        if (type.equals(de.timesnake.basic.loungebridge.util.user.TablistGroupType.GAME)) {
            return LoungeBridgeServer.getTablistGameTeam();
        }
        return super.getTablistGroup(type);
    }

    @Override
    public void setStatus(Status.User status) {
        super.setStatus(status);
        LoungeBridgeServer.updateSpectatorInventory();
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

    public boolean setKitItems() {
        if (this.kitLoaded) {
            return false;
        }

        if (this.kit == null) {
            return false;
        }

        this.kitLoaded = true;

        int i = 0;
        for (ItemStack item : this.kit.getItems()) {
            if (item instanceof ExItemStack && ((ExItemStack) item).getSlot() != null) {
                this.getInventory().setItem(((ExItemStack) item).getSlot(), item);
            } else {
                this.getInventory().setItem(i, item);
                i++;
            }
        }
        return true;
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
            LoungeBridgeServer.broadcastGameMessage(ChatColor.VALUE + this.getChatName() + ChatColor.PUBLIC + " has a" +
                    " kill-streak of " + ChatColor.VALUE + this.killStreak);
        }
    }

    public Integer getDeaths() {
        return deaths;
    }

    public void setDeaths(Integer deaths) {
        this.deaths = deaths;
        if (this.killStreak > this.highestKillStreak) this.highestKillStreak = this.killStreak;
        this.killStreak = 0;
    }

    public void addDeath() {
        this.deaths++;

        if (this.killStreak > this.highestKillStreak) {
            this.highestKillStreak = this.killStreak;
        }

        this.killStreak = 0;

        if (this.getLastDamager() != null && this.getLastDamager().getDamageType().equals(UserDamage.DamageType.INSTANT)) {

            GameUser damager = ((GameUser) this.getLastDamager().getDamager());
            damager.addKill();

            if (this.getLastDamager().getDamageType().equals(UserDamage.DamageType.PLAYER_BOW)) {
                int distance =
                        (int) this.getLastDamager().getUserLocation().distance(this.getLastDamager().getDamagerLocation());
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

                    if (this.getLastDamager().getDamageType().equals(UserDamage.DamageType.PLAYER_BOW)) {
                        int distance =
                                (int) this.getLastDamager().getUserLocation().distance(this.getLastDamager().getDamagerLocation());
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

    public String getHealthDisplay() {
        double health = this.getHealth() / 2;
        if (health - ((int) health) >= 0.25 && health - ((int) health) < 0.75) {
            health = ((int) health) + 0.5;
        } else {
            health = Math.round(health);
        }

        if (health == 0) {
            health = 0.5;
        }

        return ChatColor.RED + "" + health + "❤";
    }

    public abstract void joinGame();

    public void rejoinGame() {
        super.rejoinGame();

        this.setDefault();
        this.setCollitionWithEntites(true);
        this.setAllowFlight(false);
        this.setFlying(false);
        this.setInvulnerable(false);
        this.unlockInventory();
        this.unlockInventoryItemMove();
    }
}
