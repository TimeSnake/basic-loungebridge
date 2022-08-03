package de.timesnake.basic.loungebridge.core;

import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.user.User;
import de.timesnake.basic.bukkit.util.user.event.AsyncUserJoinEvent;
import de.timesnake.basic.bukkit.util.user.event.AsyncUserMoveEvent;
import de.timesnake.basic.bukkit.util.user.event.AsyncUserQuitEvent;
import de.timesnake.basic.bukkit.util.user.event.UserTeleportEvent;
import de.timesnake.basic.loungebridge.core.main.BasicLoungeBridge;
import de.timesnake.basic.loungebridge.util.chat.Plugin;
import de.timesnake.basic.loungebridge.util.server.LoungeBridgeServer;
import de.timesnake.basic.loungebridge.util.tool.StartableTool;
import de.timesnake.basic.loungebridge.util.tool.StopableTool;
import de.timesnake.basic.loungebridge.util.user.SpectatorUser;
import de.timesnake.channel.util.message.ChannelDiscordMessage;
import de.timesnake.channel.util.message.MessageType;
import de.timesnake.database.util.object.Type;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class DiscordManager implements Listener, StopableTool, StartableTool {

    public static final String DISCORD_SPECTATOR = "Spectator";
    public static final String DISCORD_LOUNGE = "Lounge";
    public static final String DISCORD_DISTANCE_DEFAULT = UUID.randomUUID().toString();

    public static final double HORIZONTAL_DISTANCE = 15;
    public static final double VERTICAL_DISTANCE = 10;

    public static final int DELAY = 20 * 3;
    private final Map<String, DistanceChannel> channelByName = new ConcurrentHashMap<>();
    private final Map<UUID, DistanceChannel> channelByUuid = new ConcurrentHashMap<>();
    private final Map<UUID, Action> actionsByUuid = new ConcurrentHashMap<>();
    private boolean lock = false;
    private DistanceChannel defaultChannel;
    private BukkitTask updateTask;

    private boolean isLoaded = false;

    public DiscordManager() {

    }

    public void update() {
        if (LoungeBridgeServer.isDiscord()) {
            if (LoungeBridgeServer.getGame().getDiscordType() == null || LoungeBridgeServer.getGame().getDiscordType().equals(Type.Discord.FORBIDDEN)) {
                return;
            }

            if (LoungeBridgeServer.getGame().getDiscordType().equals(Type.Discord.DISTANCE)) {
                if (!this.isLoaded) {
                    Server.registerListener(this, BasicLoungeBridge.getPlugin());
                    this.defaultChannel = new DistanceChannel(DISCORD_DISTANCE_DEFAULT) {
                        @Override
                        public UUID canJoin(UUID uuid) {
                            return null;
                        }

                        @Override
                        public boolean isClosed() {
                            return false;
                        }
                    };
                    this.channelByName.put(DISCORD_DISTANCE_DEFAULT, this.defaultChannel);
                }

                Server.printText(Plugin.GAME, "Loaded discord manager with distance channels", "Discord");
            } else if (LoungeBridgeServer.getGame().getDiscordType().equals(Type.Discord.TEAMS)) {
                Server.printText(Plugin.GAME, "Loaded discord manager with team channels", "Discord");
            }
            this.isLoaded = true;
        }
    }

    @Override
    public void start() {
        if (LoungeBridgeServer.isDiscord() && LoungeBridgeServer.getGame().getDiscordType().equals(Type.Discord.DISTANCE)) {
            Server.getChannel().sendMessage(new ChannelDiscordMessage<>(Server.getName(), MessageType.Discord.MUTE_CHANNEL, this.defaultChannel.getName()));
            Server.getChannel().sendMessage(new ChannelDiscordMessage<>(Server.getName(), MessageType.Discord.HIDE_CHANNELS, true));
            this.updateTask = Server.runTaskTimerAsynchrony(this::updateDistanceChannels, 0, DELAY, BasicLoungeBridge.getPlugin());
        }
    }

    @Override
    public void stop() {
        if (LoungeBridgeServer.isDiscord()) {
            Server.runTaskLaterSynchrony(() -> {
                LinkedHashMap<String, List<UUID>> uuidsByTeam = new LinkedHashMap<>();
                uuidsByTeam.put(DISCORD_LOUNGE, Server.getUsers().stream().map(User::getUniqueId).collect(Collectors.toList()));
                Server.getChannel().sendMessage(new ChannelDiscordMessage<>(Server.getName(), MessageType.Discord.MOVE_MEMBERS, new ChannelDiscordMessage.Allocation(uuidsByTeam)));

                if (LoungeBridgeServer.getGame().getDiscordType().equals(Type.Discord.DISTANCE)) {
                    Server.runTaskLaterSynchrony(() -> {
                        if (this.updateTask != null) {
                            this.updateTask.cancel();
                        }

                        Server.getChannel().sendMessage(new ChannelDiscordMessage<>(Server.getName(), MessageType.Discord.DESTROY_CHANNELS,
                                new ArrayList<>(this.channelByName.keySet())));
                        this.channelByName.clear();
                        this.channelByUuid.clear();
                    }, 20 * 2, BasicLoungeBridge.getPlugin());
                }

            }, 20 * 2, BasicLoungeBridge.getPlugin());
        }

    }

    public void onUserJoinSpectator(SpectatorUser user) {
        if (LoungeBridgeServer.isDiscord()) {
            LinkedHashMap<String, List<UUID>> uuidsByTeam = new LinkedHashMap<>();
            uuidsByTeam.put(DISCORD_SPECTATOR, List.of(user.getUniqueId()));
            Server.getChannel().sendMessage(new ChannelDiscordMessage<>(Server.getName(), MessageType.Discord.MOVE_MEMBERS, new ChannelDiscordMessage.Allocation(uuidsByTeam)));
        }
    }

    @EventHandler
    public void onUserJoin(AsyncUserJoinEvent e) {
        this.actionsByUuid.put(e.getUser().getUniqueId(), Action.ADD);
    }

    @EventHandler
    public void onUserMove(AsyncUserMoveEvent e) {
        this.actionsByUuid.put(e.getUser().getUniqueId(), Action.MOVE);
    }

    @EventHandler
    public void onUserTeleport(UserTeleportEvent e) {
        this.actionsByUuid.put(e.getUser().getUniqueId(), Action.MOVE);
    }

    @EventHandler
    public void onUserQuit(AsyncUserQuitEvent e) {
        this.actionsByUuid.put(e.getUser().getUniqueId(), Action.REMOVE);
    }

    public void updateDistanceChannels() {
        if (this.lock) {
            return;
        }
        this.lock = true;

        try {
            if (this.actionsByUuid.isEmpty()) {
                this.lock = false;
                return;
            }

            Map<UUID, Action> actionsByUuid = Map.copyOf(this.actionsByUuid);
            this.actionsByUuid.clear();

            for (Map.Entry<UUID, Action> entry : actionsByUuid.entrySet()) {
                UUID uuid = entry.getKey();
                Action action = entry.getValue();

                if (action == Action.REMOVE) {
                    this.removeUuid(uuid);
                } else if (action == Action.ADD || action == Action.MOVE) {
                    System.out.println(uuid);
                    DistanceChannel channelForUuid = null;
                    for (DistanceChannel channel : this.channelByName.values()) {
                        UUID joinToUuid = channel.canJoin(uuid);
                        if (joinToUuid != null) {
                            if (channelForUuid == null) {
                                // first usable channel
                                channelForUuid = channel;
                            } else {
                                // merge channels into larger one
                                if (channel.size() >= channelForUuid.size()) {
                                    DistanceChannel tmp = channelForUuid;
                                    channelForUuid = channel;
                                    channel = tmp;
                                }

                                channelForUuid.addAll(channel);
                                channel.clear();
                            }

                        }
                    }

                    // move to default channel or extract new from default
                    if (channelForUuid == null) {
                        UUID joinToUuid = this.canJoinTo(uuid, this.defaultChannel);
                        if (joinToUuid != null) {
                            this.createNewDistanceChannel(uuid, joinToUuid);
                        } else {
                            this.defaultChannel.add(uuid);
                        }
                    } else {
                        channelForUuid.add(uuid);
                    }
                }
            }

            this.executeDistanceChannelUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            this.lock = false;
        }
    }

    private void removeUuid(UUID uuid) {
        Server.getChannel().sendMessage(new ChannelDiscordMessage<>(Server.getName(), MessageType.Discord.DISCONNECT_MEMBER, uuid));
    }

    private void executeDistanceChannelUpdate() {
        List<String> channelsToRemove = new LinkedList<>();
        for (DistanceChannel channel : this.channelByName.values()) {
            if (channel.isClosed()) {
                this.channelByName.remove(channel.getName());
                channelsToRemove.add(channel.getName());
            }
        }

        if (!this.channelByName.isEmpty()) {
            Server.getChannel().sendMessage(new ChannelDiscordMessage<>(Server.getName(), MessageType.Discord.MOVE_MEMBERS, new ChannelDiscordMessage.Allocation(this.channelByName)));
        }

        if (channelsToRemove.size() > 0) {
            Server.runTaskLaterSynchrony(() -> {
                Server.getChannel().sendMessage(new ChannelDiscordMessage<>(Server.getName(), MessageType.Discord.DESTROY_CHANNELS, channelsToRemove));
            }, 20, BasicLoungeBridge.getPlugin());
        }
    }

    public DistanceChannel createNewDistanceChannel(UUID... uuids) {
        DistanceChannel channel = new DistanceChannel(UUID.randomUUID().toString());
        this.channelByName.put(channel.getName(), channel);
        channel.addAll(Arrays.stream(uuids).toList());
        return channel;
    }

    public UUID canJoinTo(UUID uuid, Collection<UUID> uuids) {
        User user = Server.getUser(uuid);

        if (user == null) {
            return null;
        }

        for (UUID memberUuid : uuids) {
            if (this.canJoinTo(user, memberUuid)) {
                return memberUuid;
            }
        }
        return null;
    }

    public boolean canJoinTo(User user, UUID toUuid) {
        User member = Server.getUser(toUuid);
        if (member == null || member.equals(user)) {
            return false;
        }

        Location userLocation = user.getLocation();
        Location memberLocation = member.getLocation();

        if (!userLocation.getWorld().equals(memberLocation.getWorld())) {
            return false;
        }

        if (Math.pow(userLocation.getX() - memberLocation.getX(), 2) + Math.pow(userLocation.getZ() - memberLocation.getZ(), 2) <= HORIZONTAL_DISTANCE * HORIZONTAL_DISTANCE) {
            if (Math.abs(userLocation.getY() - memberLocation.getY()) <= VERTICAL_DISTANCE) {
                return true;
            }
        }

        return false;
    }

    enum Action {
        ADD,
        REMOVE,
        MOVE
    }

    public class DistanceChannel extends HashSet<UUID> {

        private final String name;
        private boolean closed = false;

        public DistanceChannel(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public UUID canJoin(UUID uuid) {
            if (this.isClosed()) {
                return null;
            }

            return DiscordManager.this.canJoinTo(uuid, this);
        }

        @Override
        public void clear() {
            super.clear();
            this.closed = true;
        }

        @Override
        public boolean add(UUID uuid) {
            DistanceChannel previous = DiscordManager.this.channelByUuid.get(uuid);
            if (previous != null && !previous.equals(this)) {
                previous.remove(uuid);
            }
            DiscordManager.this.channelByUuid.put(uuid, this);
            return super.add(uuid);
        }

        @Override
        public boolean addAll(@NotNull Collection<? extends UUID> c) {
            boolean modified = false;
            for (UUID uuid : c) {
                if (add(uuid)) {
                    modified = true;
                }
            }
            return modified;
        }

        @Override
        public boolean remove(Object o) {
            boolean res = super.remove(o);
            if (this.isEmpty()) {
                this.closed = true;
            }
            return res;
        }

        public boolean isClosed() {
            return closed;
        }
    }

}
