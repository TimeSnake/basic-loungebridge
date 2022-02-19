package de.timesnake.basic.loungebridge.core;

import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.game.util.GameServer;
import de.timesnake.basic.game.util.Map;
import de.timesnake.basic.loungebridge.core.main.BasicLoungeBridge;
import de.timesnake.basic.loungebridge.util.chat.Plugin;
import de.timesnake.basic.loungebridge.util.server.LoungeBridgeServer;
import de.timesnake.basic.loungebridge.util.server.LoungeBridgeServerManager;
import de.timesnake.channel.util.listener.ChannelHandler;
import de.timesnake.channel.util.listener.ListenerType;
import de.timesnake.channel.util.message.ChannelServerMessage;
import de.timesnake.channel.util.message.MessageType;
import de.timesnake.database.util.server.DbTempGameServer;

import java.util.List;

public class ChannelListener implements de.timesnake.channel.util.listener.ChannelListener {

    public ChannelListener() {
        Server.getChannel().addListener(this, () -> List.of(Server.getPort(), LoungeBridgeServer.getTwinServer().getPort()));
    }

    @ChannelHandler(type = {ListenerType.SERVER_MAP, ListenerType.SERVER_CUSTOM}, filtered = true)
    public void onServerMessage(ChannelServerMessage<?> msg) {
        if (msg.getMessageType().equals(MessageType.Server.MAP)) {
            Map map = GameServer.getGame().getMap(((DbTempGameServer) Server.getDatabase()).getMapName());
            LoungeBridgeServerManager.getInstance().setMap(map);
            Server.printText(Plugin.LOUNGE, "Loading map " + map.getName() + " ...");
            Server.runTaskSynchrony(LoungeBridgeServer::loadMap, BasicLoungeBridge.getPlugin());
            Server.printText(Plugin.LOUNGE, "Loaded map " + map.getName());
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
        }
    }

}
