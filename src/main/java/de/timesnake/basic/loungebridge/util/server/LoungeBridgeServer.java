package de.timesnake.basic.loungebridge.util.server;

import de.timesnake.basic.bukkit.util.chat.Chat;
import de.timesnake.basic.bukkit.util.user.scoreboard.Sideboard;
import de.timesnake.basic.bukkit.util.user.scoreboard.TeamTablist;
import de.timesnake.basic.game.util.GameServer;
import de.timesnake.basic.game.util.Map;
import de.timesnake.basic.game.util.Team;
import de.timesnake.basic.loungebridge.core.SpectatorManager;
import de.timesnake.basic.loungebridge.core.UserManager;
import de.timesnake.basic.loungebridge.util.user.*;
import de.timesnake.database.util.server.DbLoungeServer;
import de.timesnake.library.basic.util.chat.Plugin;
import org.bukkit.Location;

import java.util.Collection;

public class LoungeBridgeServer extends GameServer {

    private static final LoungeBridgeServerManager server = LoungeBridgeServerManager.getInstance();

    public enum State {
        STARTING, RUNNING, STOPPED, CLOSING, RESETTING, WAITING
    }

    public static void closeGame() {
        server.closeGame();
    }

    public static DbLoungeServer getTwinServer() {
        return server.getTwinServer();
    }

    public static int getGameCountdown() {
        return server.getGameCountdown();
    }

    public static void broadcastLoungeBridgeMessage(String msg) {
        server.broadcastLoungeBridgeMessage(msg);
    }

    public static void setState(State state) {
        server.setState(state);
    }

    public static State getState() {
        return server.getState();
    }

    public static TeamTablist getGameTablist() {
        return server.getGameTablist();
    }

    public static SpectatorManager getSpectatorManager() {
        return server.getSpectatorManager();
    }

    public static Chat getSpectatorChat() {
        return server.getSpectatorChat();
    }

    public static void updateSpectatorInventory() {
        server.updateSpectatorTools();
    }

    public static void loadMap() {
        server.loadMap();
    }

    public static Location getSpectatorSpawn() {
        return server.getSpectatorSpawn();
    }

    public static boolean isGameRunning() {
        return server.isGameRunning();
    }

    public static Sideboard getSpectatorSideboard() {
        return server.getSpectatorSideboard();
    }

    public static Kit getKit(int index) throws KitNotDefinedException {
        return server.getKit(index);
    }

    public static void broadcastGameMessage(String message) {
        server.broadcastGameMessage(message);
    }

    public static Plugin getGamePlugin() {
        return server.getGamePlugin();
    }

    public static boolean isTeamMateDamage() {
        return server.isTeamMateDamage();
    }

    public static void setTeamMateDamage(boolean teamMateDamage) {
        server.setTeamMateDamage(teamMateDamage);
    }

    public static TablistTeam getTablistGameTeam() {
        return server.getTablistGameTeam();
    }

    public static Integer getServerTeamAmount() {
        return server.getServerTeamAmount();
    }

    public static boolean areKitsEnabled() {
        return server.areKitsEnabled();
    }

    public static boolean areMapsEnabled() {
        return server.areMapsEnabled();
    }

    public static <M extends Map> M getMap() {
        return (M) server.getMap();
    }

    public static <U extends GameUser> U getMostKills(Collection<U> users) {
        return server.getMostKills(users);
    }

    public static <U extends GameUser> U getHighestKillstreak(Collection<U> users) {
        return server.getHighestKillStreak(users);
    }

    public static <U extends GameUser> U getMostDeaths(Collection<U> users) {
        return server.getMostDeaths(users);
    }

    public static <U extends GameUser> U getHighestKD(Collection<U> users) {
        return server.getHighestKD(users);
    }

    public static <U extends GameUser> U getLongestShot(Collection<U> users) {
        return server.getLongestShot(users);
    }

    public static Collection<Team> getNotEmptyGameTeams() {
        return server.getNotEmptyInGameTeams();
    }

    public static void setEstimatedPlayers(Integer amount) {
        server.setEstimatedPlayers(amount);
    }

    public static Integer getEstimatedPlayers() {
        return server.getEstimatedPlayers();
    }

    public static void checkGameStart() {
        server.checkGameStart();
    }

    public static boolean isRejoiningAllowed() {
        return server.isRejoiningAllowed();
    }

    public static void onGameUserRejoin(GameUser user) {
        server.onGameUserRejoin(user);
    }

    public static OfflineUser loadOfflineUser(GameUser user) {
        return server.loadOfflineUser(user);
    }

    public static void prepareGame() {
        server.prepareGame();
    }

    public static void startGame() {
        server.startGame();
    }

    public static UserManager getLoungeBridgeUserManager() {
        return server.getLoungeBridgeUserManager();
    }

    public static void resetKillsAndDeaths() {
        server.resetKillsAndDeaths();
    }

    public static void resetGame() {
        server.resetGame();
    }

    public static void saveGameStats() {
        server.saveGameStats();
    }
}
