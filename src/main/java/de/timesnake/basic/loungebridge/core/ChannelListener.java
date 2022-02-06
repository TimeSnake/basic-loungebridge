package de.timesnake.basic.loungebridge.core;

import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.game.util.GameServer;
import de.timesnake.basic.game.util.Map;
import de.timesnake.basic.loungebridge.core.main.BasicLoungeBridge;
import de.timesnake.basic.loungebridge.util.chat.Plugin;
import de.timesnake.basic.loungebridge.util.server.LoungeBridgeServer;
import de.timesnake.basic.loungebridge.util.server.LoungeBridgeServerManager;
import de.timesnake.channel.api.message.ChannelServerMessage;
import de.timesnake.channel.listener.ChannelServerListener;
import de.timesnake.database.util.server.DbTempGameServer;

public class ChannelListener implements ChannelServerListener {

    public ChannelListener() {
        Server.getChannel().addServerListener(this, Server.getPort());
        Server.getChannel().addServerListener(this, LoungeBridgeServer.getTwinServer().getPort());
    }

    @Override
    public void onServerMessage(ChannelServerMessage msg) {
        if (msg.getType().equals(ChannelServerMessage.MessageType.MAP)) {
            Map map = GameServer.getGame().getMap(((DbTempGameServer) Server.getDatabase()).getMapName());
            LoungeBridgeServerManager.getInstance().setMap(map);
            Server.printText(Plugin.LOUNGE, "Loading map " + map.getName() + " ...");
            Server.runTaskSynchrony(LoungeBridgeServer::loadMap, BasicLoungeBridge.getPlugin());
            Server.printText(Plugin.LOUNGE, "Loaded map " + map.getName());
        } else if (msg.getType().equals(ChannelServerMessage.MessageType.CUSTOM)) {
            if (msg.getValue().contains("estimatedPlayers:")) {
                String[] value = msg.getValue().split(":");
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
