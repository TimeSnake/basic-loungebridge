package de.timesnake.basic.loungebridge.util.server;

import de.timesnake.basic.bukkit.util.user.User;
import de.timesnake.basic.loungebridge.util.user.GameUser;
import de.timesnake.library.basic.util.chat.ExTextColor;
import de.timesnake.library.extension.util.chat.Chat;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

public interface HighScoreCalculator {

    default <U extends GameUser> Set<U> getMostKills(Collection<U> users, int number) {
        return this.getHighScore(users, number, Comparator.comparing(GameUser::getKills));
    }

    default <U extends GameUser> Set<U> getHighestKillStreak(Collection<U> users, int number) {
        return this.getHighScore(users, number, Comparator.comparing(GameUser::getHighestKillStreak));
    }

    default <U extends GameUser> Set<U> getMostDeaths(Collection<U> users, int number) {
        return this.getHighScore(users, number, Comparator.comparing(GameUser::getDeaths));
    }

    default <U extends GameUser> Set<U> getHighestKD(Collection<U> users, int number) {
        return this.getHighScore(users, number, Comparator.comparing(GameUser::getKillDeathRatio));
    }

    default <U extends GameUser> Set<U> getLongestShot(Collection<U> users, int number) {
        return this.getHighScore(users, number,
                Comparator.comparing((Function<U, Comparable>) GameUser::getLongestShot));
    }

    default <U extends GameUser> Set<U> getHighScore(Collection<U> users, int number, Comparator<U> comparator) {
        if (users == null || users.isEmpty()) return new HashSet<>();

        Set<U> highestUsers = new HashSet<>();
        U userWithHighscore = users.stream().findFirst().orElse(null);

        for (U user : users) {
            if (highestUsers.isEmpty()) {
                highestUsers.add(user);
            } else if (comparator.compare(user, userWithHighscore) > 0) {
                highestUsers.clear();
                highestUsers.add(user);
                userWithHighscore = user;
            } else if (comparator.compare(user, userWithHighscore) == 0 && highestUsers.size() < number) {
                highestUsers.add(user);
            }
        }
        return highestUsers;
    }

    default void broadcastHighscore(String name, Collection<? extends GameUser> users, int number,
                                    Predicate<GameUser> predicateToBroadcast,
                                    Function<GameUser, ? extends Comparable> keyExtractor) {
        Set<GameUser> highestUsers = this.getHighScore(users, number, Comparator.comparing(keyExtractor));
        if (highestUsers.size() == 0 || !predicateToBroadcast.test(highestUsers.stream().findFirst().get())) {
            return;
        }

        TextComponent.Builder builder = Component.text();

        builder.append(Component.text(name + ": ", ExTextColor.WHITE))
                .append(Component.text("" + keyExtractor.apply(highestUsers.stream().findFirst().get()), ExTextColor.GOLD))
                .append(Component.text(" by ").color(ExTextColor.WHITE));

        builder.append(Chat.listToComponent(highestUsers.stream().map(User::getChatNameComponent).toList()));

        this.broadcastGameMessage(builder.build());
    }

    default void broadcastHighscore(String name, Collection<? extends GameUser> users, int number, Function<GameUser,
            ? extends Comparable> keyExtractor) {
        this.broadcastHighscore(name, users, number, (u) -> true, keyExtractor);
    }

    void broadcastGameMessage(Component msg);
}
