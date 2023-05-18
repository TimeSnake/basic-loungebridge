/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.basic.loungebridge.core;

import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.loungebridge.util.server.LoungeBridgeServer;
import de.timesnake.channel.util.listener.ChannelHandler;
import de.timesnake.channel.util.listener.ListenerType;
import de.timesnake.channel.util.message.ChannelServerMessage;
import de.timesnake.channel.util.message.MessageType;
import de.timesnake.library.basic.util.Loggers;
import java.util.List;

public class ChannelListener implements de.timesnake.channel.util.listener.ChannelListener {

  public ChannelListener() {
    Server.getChannel().addListener(this, () -> List.of(Server.getName(),
        LoungeBridgeServer.getTwinServer().getName()));
  }

  @ChannelHandler(type = {ListenerType.SERVER_GAME_MAP, ListenerType.SERVER_GAME_WORLD,
      ListenerType.SERVER_CUSTOM}, filtered = true)
  public void onServerMessage(ChannelServerMessage<?> msg) {
    if (msg.getMessageType().equals(MessageType.Server.GAME_MAP)) {
      LoungeBridgeServer.loadMap();
    } else if (msg.getMessageType().equals(MessageType.Server.GAME_WORLD)) {
      LoungeBridgeServer.loadWorld();
    } else if (msg.getMessageType().equals(MessageType.Server.CUSTOM)) {
      if (((String) msg.getValue()).contains("estimatedPlayers:")) {
        String[] value = ((String) msg.getValue()).split(":");
        if (value.length == 2) {
          try {
            Integer estimatedPlayers = Integer.valueOf(value[1]);
            LoungeBridgeServer.setEstimatedPlayers(estimatedPlayers);
            Loggers.LOUNGE_BRIDGE.info("Estimated Players: " + estimatedPlayers);
          } catch (NumberFormatException ignored) {

          }
        }
      }
    }
  }

}
