/*
 * workspace.basic-loungebridge.main
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
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;

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
     * @return Return true if the game is running
     */
    boolean isGameRunning();

    /**
     * Broadcast game info chat messages
     *
     * @param message The message to broadcast
     */
    @Deprecated
    default void broadcastGameMessage(String message) {

    }

    /**
     * Broadcast game info chat messages
     *
     * @param message The message to broadcast
     */
    void broadcastGameMessage(Component message);

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
     * Game user rejoins game
     *
     * @param user The user who is rejoining
     */
    default void onGameUserRejoin(GameUser user) {

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
