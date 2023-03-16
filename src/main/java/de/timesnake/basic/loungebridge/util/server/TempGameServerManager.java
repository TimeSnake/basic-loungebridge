/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.basic.loungebridge.util.server;

import de.timesnake.basic.bukkit.util.user.User;
import de.timesnake.basic.bukkit.util.user.scoreboard.Sideboard;
import de.timesnake.basic.bukkit.util.world.ExLocation;
import de.timesnake.basic.loungebridge.util.user.GameUser;
import de.timesnake.basic.loungebridge.util.user.Kit;
import de.timesnake.basic.loungebridge.util.user.KitNotDefinedException;
import de.timesnake.basic.loungebridge.util.user.OfflineUser;
import de.timesnake.library.basic.util.statistics.IntegerStat;
import de.timesnake.library.basic.util.statistics.StatType;
import java.util.HashSet;
import java.util.Set;
import org.bukkit.entity.Player;

public interface TempGameServerManager {

    StatType<Integer> GAMES_PLAYED = new IntegerStat("games_played", "Games Played", 0, 10,
            1, false, 0, 1);

    GameUser loadUser(Player player);

    /**
     * Get the game {@link de.timesnake.basic.bukkit.util.chat.Plugin}
     *
     * @return Return the plugin
     */
    de.timesnake.library.extension.util.chat.Plugin getGamePlugin();

    /**
     * Called by channel map load message from lounge (map-voting).
     */
    default void onMapLoad() {

    }

    /**
     * Countdown 7s, during user join
     */
    default void onGamePrepare() {

    }

    /**
     * Countdown 0s
     */
    void onGameStart();

    void onGameStop();

    /**
     * Called after all users quit
     */
    void onGameReset();

    /**
     * Ingame user quits the server
     *
     * @param user The {@link User} who left
     */
    void onGameUserQuit(GameUser user);

    /**
     * Ingame user quits before the game started.
     *
     * @param user The {@link User} who left
     */
    void onGameUserQuitBeforeStart(GameUser user);

    /**
     * Allows users to rejoin the game
     *
     * @return true if it is allowed
     */
    boolean isRejoiningAllowed();

    /**
     * Allows users with status outgame to rejoin with status outgame
     *
     * @return true if it is allowed
     */
    default boolean isOutGameRejoiningAllowed() {
        return false;
    }

    /**
     * Game user rejoins game
     *
     * @param user The user who is rejoining
     */
    default void onGameUserRejoin(GameUser user) {

    }

    default Sideboard getGameSideboard() {
        return null;
    }

    default Sideboard getSpectatorSideboard() {
        return null;
    }

    default Kit getKit(int index) throws KitNotDefinedException {
        return null;
    }

    default Kit[] getKits() {
        return new Kit[]{};
    }

    ExLocation getSpectatorSpawn();

    default OfflineUser loadOfflineUser(GameUser user) {
        return new OfflineUser(user);
    }

    default Set<StatType<?>> getStats() {
        return new HashSet<>();
    }

    default void saveGameUserStats(GameUser user) {
        user.getStat(GAMES_PLAYED).increaseAll(1);
    }

}
