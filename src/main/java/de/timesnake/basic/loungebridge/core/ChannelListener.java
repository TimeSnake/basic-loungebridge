package de.timesnake.basic.loungebridge.core;

import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.loungebridge.util.chat.Plugin;
import de.timesnake.basic.loungebridge.util.server.LoungeBridgeServer;
import de.timesnake.channel.util.listener.ChannelHandler;
import de.timesnake.channel.util.listener.ListenerType;
import de.timesnake.channel.util.message.ChannelServerMessage;
import de.timesnake.channel.util.message.MessageType;

import java.util.List;

public class ChannelListener implements de.timesnake.channel.util.listener.ChannelListener {

    public ChannelListener() {
        Server.getChannel().addListener(this, () -> List.of(Server.getPort(),
                LoungeBridgeServer.getTwinServer().getPort()));
    }

    @ChannelHandler(type = {ListenerType.SERVER_MAP, ListenerType.SERVER_CUSTOM, ListenerType.SERVER_DISCORD},
            filtered = true)
    public void onServerMessage(ChannelServerMessage<?> msg) {
        if (msg.getMessageType().equals(MessageType.Server.MAP)) {
            LoungeBridgeServer.loadMap();
        } else if (msg.getMessageType().equals(MessageType.Server.CUSTOM)) {
            if (((String) msg.getValue()).contains("estimatedPlayers:")) {
                String[] value = ((String) msg.getValue()).split(":");
                if (value.length == 2) {
                    try {
                        Integer estimatedPlayers = Integer.valueOf(value[1]);
                        LoungeBridgeServer.setEstimatedPlayers(estimatedPlayers);
                        Server.printText(Plugin.LOUNGE, "Estimated Players: " + estimatedPlayers);
                    } catch (NumberFormatException ignored) {

                    }
                }
            }
        } else if (msg.getMessageType().equals(MessageType.Server.DISCORD)) {
            LoungeBridgeServer.setDiscord((boolean) msg.getValue());
            LoungeBridgeServer.getDiscordManager().update();
        }
    }

}
