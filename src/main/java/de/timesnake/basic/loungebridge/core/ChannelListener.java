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

package de.timesnake.basic.loungebridge.core;

import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.game.util.user.Plugin;
import de.timesnake.basic.loungebridge.util.server.LoungeBridgeServer;
import de.timesnake.channel.util.listener.ChannelHandler;
import de.timesnake.channel.util.listener.ListenerType;
import de.timesnake.channel.util.message.ChannelServerMessage;
import de.timesnake.channel.util.message.MessageType;

import java.util.List;

public class ChannelListener implements de.timesnake.channel.util.listener.ChannelListener {

    public ChannelListener() {
        Server.getChannel().addListener(this, () -> List.of(Server.getName(),
                LoungeBridgeServer.getTwinServer().getName()));
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
