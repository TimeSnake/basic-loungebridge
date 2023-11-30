/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.basic.loungebridge.util.server;

import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.chat.MessageBlockBuilder;
import de.timesnake.basic.bukkit.util.user.User;
import de.timesnake.basic.game.util.game.Team;
import de.timesnake.basic.loungebridge.util.user.GameUser;
import de.timesnake.library.extension.util.chat.Chat;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public class EndMessage {

  public static final String NO_WINNER = "Game has ended";

  private final List<String> winners = new LinkedList<>();
  private final List<String> stats = new LinkedList<>();
  private final List<String> extras = new LinkedList<>();

  private final Collection<User> winnerUsers = new LinkedList<>();
  private float winCoins = LoungeBridgeServer.WIN_COINS;

  private boolean showTitle = true;
  private String title;
  private String subTitle;

  public EndMessage() {

  }

  public EndMessage winner(@Nullable User user) {
    if (user == null) {
      return this;
    }

    this.winners.add(user.getTDChatName());
    this.winnerUsers.add(user);
    return this;
  }

  public EndMessage winner(@Nullable Team team) {
    if (team == null) {
      return this.noWinner();
    }

    this.winners.add(team.getTDColor() + team.getDisplayName());
    this.winnerUsers.addAll(team.getUsers());
    return this;
  }

  public EndMessage winner(Collection<User> users) {
    this.winners.add(String.join(", ", users.stream().map(User::getTDChatName).toList()));
    this.winnerUsers.addAll(users);
    return this;
  }

  public EndMessage winner(String winner) {
    this.winners.add(winner);
    return this;
  }


  public EndMessage noWinner() {
    this.winners.clear();
    this.winners.add(NO_WINNER);
    return this;
  }

  public EndMessage addStat(String text) {
    this.stats.add(text);
    return this;
  }

  public EndMessage addStat(String name, Collection<? extends User> users, int maxUsers, Function<GameUser, ?
      extends Comparable<?>> keyExtractor) {
    this.stats.add(LoungeBridgeServer.getHighscoreMessage(name, (Collection<? extends GameUser>) users, maxUsers,
        keyExtractor));
    return this;
  }

  public EndMessage addStat(String name, Collection<? extends User> users, int maxUsers,
                            Predicate<GameUser> predicateToBroadcast,
                            Function<GameUser, ? extends Comparable<?>> keyExtractor) {
    this.stats.add(LoungeBridgeServer.getHighscoreMessage(name, (Collection<? extends GameUser>) users, maxUsers,
        predicateToBroadcast, keyExtractor));
    return this;
  }


  public EndMessage addExtra(String text) {
    this.extras.add(text);
    return this;
  }

  public EndMessage addExtraLineSeparator() {
    this.extras.add(Chat.getLineTDSeparator());
    return this;
  }

  public EndMessage winCoins(float coins) {
    this.winCoins = coins;
    return this;
  }

  public EndMessage showTitle(boolean show) {
    this.showTitle = show;
    return this;
  }

  public EndMessage title(String title) {
    this.title = title;
    return this;
  }

  public EndMessage subTitle(String subTitle) {
    this.subTitle = subTitle;
    return this;
  }

  public EndMessage applyIf(boolean condition, Consumer<EndMessage> consumer) {
    if (condition) {
      consumer.accept(this);
    }
    return this;
  }

  public void send() {
    Server.broadcastSound(LoungeBridgeServer.END_SOUND, 5F);

    if (this.winners.isEmpty()) {
      this.noWinner();
    }

    new MessageBlockBuilder()
        .separatorLine()
        .addLine(String.join(", ", this.winners) + " §pwin")
        .separatorLine()
        .addLines(this.stats)
        .separatorLine(!this.stats.isEmpty())
        .addLines(this.extras)
        .sendTo(LoungeBridgeServer::broadcastGameTDMessage);

    if (this.title == null) {
      this.title = String.join(", ", this.winners) + " §pwin!";
    }

    if (this.subTitle == null) {
      this.subTitle = "";
    }

    Server.broadcastTDTitle(this.title, this.subTitle, Duration.ofSeconds(5));


    if (this.winCoins > 0) {
      this.winnerUsers.forEach(u -> u.addCoins(this.winCoins, true));
    }
  }

}
