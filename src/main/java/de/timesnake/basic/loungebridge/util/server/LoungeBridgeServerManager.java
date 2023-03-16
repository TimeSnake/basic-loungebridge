/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.basic.loungebridge.util.server;

import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.ServerManager;
import de.timesnake.basic.bukkit.util.chat.Chat;
import de.timesnake.basic.bukkit.util.user.User;
import de.timesnake.basic.bukkit.util.user.scoreboard.Tablist;
import de.timesnake.basic.bukkit.util.user.scoreboard.TeamTablist;
import de.timesnake.basic.game.util.game.Map;
import de.timesnake.basic.game.util.game.Team;
import de.timesnake.basic.game.util.game.TmpGame;
import de.timesnake.basic.game.util.server.GameServer;
import de.timesnake.basic.game.util.server.GameServerManager;
import de.timesnake.basic.game.util.user.Plugin;
import de.timesnake.basic.game.util.user.SpectatorManager;
import de.timesnake.basic.loungebridge.core.ChannelListener;
import de.timesnake.basic.loungebridge.core.CoinsManager;
import de.timesnake.basic.loungebridge.core.DiscordManager;
import de.timesnake.basic.loungebridge.core.GameScheduler;
import de.timesnake.basic.loungebridge.core.StatsManager;
import de.timesnake.basic.loungebridge.core.UserManager;
import de.timesnake.basic.loungebridge.core.main.BasicLoungeBridge;
import de.timesnake.basic.loungebridge.util.game.ResetableMap;
import de.timesnake.basic.loungebridge.util.tool.CloseableTool;
import de.timesnake.basic.loungebridge.util.tool.MapLoadableTool;
import de.timesnake.basic.loungebridge.util.tool.PreCloseableTool;
import de.timesnake.basic.loungebridge.util.tool.PreStopableTool;
import de.timesnake.basic.loungebridge.util.tool.PrepareableTool;
import de.timesnake.basic.loungebridge.util.tool.ResetableTool;
import de.timesnake.basic.loungebridge.util.tool.StartableTool;
import de.timesnake.basic.loungebridge.util.tool.StopableTool;
import de.timesnake.basic.loungebridge.util.tool.ToolManager;
import de.timesnake.basic.loungebridge.util.user.GameUser;
import de.timesnake.basic.loungebridge.util.user.Kit;
import de.timesnake.basic.loungebridge.util.user.TablistTeam;
import de.timesnake.channel.util.message.ChannelServerMessage;
import de.timesnake.channel.util.message.MessageType;
import de.timesnake.database.util.game.DbGame;
import de.timesnake.database.util.object.UnsupportedStringException;
import de.timesnake.database.util.server.DbLoungeServer;
import de.timesnake.database.util.server.DbTmpGameServer;
import de.timesnake.library.basic.util.Status;
import de.timesnake.library.chat.ExTextColor;
import java.util.Collection;
import java.util.HashSet;
import java.util.stream.Collectors;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public abstract class LoungeBridgeServerManager<Game extends TmpGame> extends
        GameServerManager<Game> implements TempGameServerManager, HighScoreCalculator {

    public static final String SPECTATOR_NAME = "spectator";
    public static final String SPECTATOR_CHAT_DISPLAY_NAME = "Spec";
    public static final String SPECTATOR_TABLIST_PREFIX = "";
    public static final ExTextColor SPECTATOR_CHAT_COLOR = ExTextColor.GRAY;
    public static final org.bukkit.ChatColor SPECTATOR_TABLIST_CHAT_COLOR = org.bukkit.ChatColor.GRAY;
    public static final org.bukkit.ChatColor SPECTATOR_TABLIST_PREFIX_CHAT_COLOR = org.bukkit.ChatColor.GRAY;

    public static final Integer MAX_START_DELAY = 5 * 20; // max start delay after first join

    public static LoungeBridgeServerManager<?> getInstance() {
        return (LoungeBridgeServerManager<?>) ServerManager.getInstance();
    }

    protected DbLoungeServer twinServer;

    protected LoungeBridgeServer.State state;
    protected ChannelListener channelListener;
    protected boolean kitsEnabled;
    protected boolean mapsEnabled;
    protected Integer serverTeamAmount;
    protected Integer maxPlayersPerTeam;
    protected boolean teamMateDamage = true;
    protected Integer estimatedPlayers;
    protected boolean discord;
    protected ToolManager toolManager;
    protected Boolean running = false;
    private Map map;
    private UserManager userManager;
    private GameScheduler gameScheduler;
    private TablistManager tablistManager;
    private DiscordManager discordManager;

    public final void onLoungeBridgeEnable() {
        this.toolManager = this.loadToolManager();
        if (this.toolManager == null) {
            this.toolManager = new ToolManager();
        }

        super.onGameEnable();

        // update kits into database, if enabled
        this.kitsEnabled = ((DbTmpGameServer) this.getDatabase()).areKitsEnabled();
        if (this.kitsEnabled) {
            this.loadKitsIntoDatabase();
        }

        // search twin server
        DbTmpGameServer database = ((DbTmpGameServer) Server.getDatabase());
        this.twinServer = database.getTwinServer();
        if (twinServer == null) {
            Server.printWarning(Plugin.LOUNGE, "No twin server found in database");
            Bukkit.shutdown();
            return;
        }

        this.userManager = new UserManager();
        this.gameScheduler = new GameScheduler();

        StatsManager statsManager = this.loadStatsManager();
        if (statsManager != null) {
            statsManager.loadStatTypesIntoDatabase();
            this.toolManager.add(statsManager);
        }

        CoinsManager coinsManager = new CoinsManager();
        if (coinsManager != null) {
            this.toolManager.add(coinsManager);
        }

        this.mapsEnabled = ((DbTmpGameServer) this.getDatabase()).areMapsEnabled();
        Integer serverTeamAmount = ((DbTmpGameServer) this.getDatabase()).getTeamAmount();
        this.serverTeamAmount =
                serverTeamAmount != null ? serverTeamAmount : this.getGame().getTeams().size();
        this.maxPlayersPerTeam = ((DbTmpGameServer) this.getDatabase()).getMaxPlayersPerTeam();

        this.channelListener = new ChannelListener();

        this.discordManager = this.loadDiscordManager();
        if (discordManager == null) {
            this.discordManager = new DiscordManager();
        }
        this.toolManager.add(this.discordManager);
        this.discordManager.update();

        this.tablistManager = this.loadTablistManager();
        if (this.tablistManager == null) {
            this.tablistManager = new TablistManager();
        }
        this.tablistManager.loadTablist(Tablist.Type.DUMMY);
        Server.getScoreboardManager().setActiveTablist(this.tablistManager.getGameTablist());

        this.toolManager.add((StartableTool) () -> Server.getInGameUsers()
                .forEach(u -> ((GameUser) u).onGameStart()));

        this.toolManager.add((StopableTool) () -> Server.getInGameUsers()
                .forEach(u -> ((GameUser) u).onGameStop()));

        // load maps, if enabled
        if (this.mapsEnabled) {
            for (Map map : this.getGame().getMaps()) {
                map.getWorld();
            }
        }

        // set map from database
        this.setMap(this.getGame().getMap(database.getMapName()));

        // silent join quit
        Server.getChat().broadcastJoinQuit(false);

        this.loadChats();

        // mark as ready
        Server.printText(Plugin.GAME, "Server loaded");
        this.setState(LoungeBridgeServer.State.WAITING);
    }

    public ToolManager loadToolManager() {
        return new ToolManager();
    }

    public StatsManager loadStatsManager() {
        return new StatsManager();
    }

    public CoinsManager loadCoinsManager() {
        return new CoinsManager();
    }

    public TablistManager loadTablistManager() {
        return new TablistManager();
    }

    public DiscordManager loadDiscordManager() {
        return new DiscordManager();
    }

    @Override
    protected SpectatorManager loadSpectatorManager() {
        return new de.timesnake.basic.loungebridge.core.main.SpectatorManager();
    }

    public void loadChats() {
        // create team chat
        for (Team team : this.getGame().getTeams()) {
            // chat
            if (team.hasPrivateChat()) {
                Server.getChatManager()
                        .createChat(team.getName(), team.getDisplayName(), team.getTextColor(),
                                new HashSet<>());
            }
        }

        // create spectator chat
        Server.getChatManager()
                .createChat(SPECTATOR_NAME, SPECTATOR_CHAT_DISPLAY_NAME, SPECTATOR_CHAT_COLOR,
                        new HashSet<>());
    }

    @Override
    public abstract GameUser loadUser(Player player);

    public void checkGameStart() {
        int gameUsers = Server.getPreGameUsers().size();
        if (this.getEstimatedPlayers() != null && gameUsers >= this.getEstimatedPlayers()) {
            this.startGameCountdown();
        } else if (gameUsers == 1) {
            Server.runTaskLaterSynchrony(() -> {
                if (!this.state.equals(LoungeBridgeServer.State.STARTING)) {
                    this.startGameCountdown();
                }
            }, LoungeBridgeServerManager.MAX_START_DELAY, BasicLoungeBridge.getPlugin());
        }
    }

    private void startGameCountdown() {
        this.gameScheduler.startGameCountdown();
    }

    public void resetKillsAndDeaths() {
        for (Team team : this.getGame().getTeams()) {
            team.setDeaths(0);
            team.setKills(0);
        }
    }

    public void loadKitsIntoDatabase() {
        if (this.getKits() == null) {
            return;
        }
        DbGame game = this.getGame().getDatabase();
        for (Kit kit : this.getKits()) {
            if (game.getKit(kit.getId()).exists()) {
                game.removeKitSynchronized(kit.getId());
            }
            try {
                game.addKit(kit.getId(), kit.getName(), kit.getMaterial().toString(),
                        kit.getDescription());
                Server.printText(Plugin.LOUNGE,
                        "Loaded kit " + kit.getName() + " into the database", "Kit");
            } catch (UnsupportedStringException e) {
                Server.printWarning(Plugin.LOUNGE,
                        "Can not load kit " + kit.getName() + " into database " +
                                "(UnsupportedStringException)", "Kit");
            } catch (Exception e) {
                Server.printWarning(Plugin.LOUNGE,
                        "Can not load kit " + kit.getName() + " into database ", "Kit");
            }
        }
    }

    public final void loadMap() {
        Map map = GameServer.getGame()
                .getMap(((DbTmpGameServer) Server.getDatabase()).getMapName());
        this.setMap(map);

        this.toolManager.runTools(MapLoadableTool.class);
        this.onMapLoad();

        Server.printText(Plugin.LOUNGE, "Loaded map " + map.getName());
    }

    public final void prepareGame() {
        this.onGamePrepare();
        this.toolManager.runTools(PrepareableTool.class);
    }

    public final void startGame() {
        LoungeBridgeServer.setState(LoungeBridgeServer.State.RUNNING);
        this.running = true;

        for (User user : Server.getPreGameUsers()) {
            user.setStatus(Status.User.IN_GAME);
            ((GameUser) user).playedGame();
        }

        this.toolManager.runTools(StartableTool.class);

        Server.getChat().broadcastJoinQuit(true);

        this.onGameStart();
    }

    public final void stopGame() {
        if (!this.running) {
            return;
        }

        this.running = false;

        this.toolManager.runTools(PreStopableTool.class);

        this.onGameStop();

        this.toolManager.runTools(StopableTool.class);

        this.closeGame();
    }

    public final void closeGame() {
        if (LoungeBridgeServer.getState() != LoungeBridgeServer.State.CLOSING) {
            LoungeBridgeServer.setState(LoungeBridgeServer.State.CLOSING);

            this.toolManager.runTools(CloseableTool.class);

            for (User user : Server.getInGameUsers()) {
                user.setDefault();
                user.getPlayer().setInvulnerable(true);
                user.lockLocation();
            }

            for (User user : Server.getSpectatorUsers()) {
                user.clearInventory();
            }

            this.getLoungeBridgeUserManager().clearRejoinUsers();

            Chat specChat = this.getSpectatorChat();
            for (User user : Server.getUsers()) {
                if (((GameUser) user).getTeam() != null) {
                    Chat teamChat = Server.getChat(((GameUser) user).getTeam().getName());
                    if (teamChat != null) {
                        teamChat.removeWriter(user);
                        teamChat.removeListener(user);
                    }
                }

                specChat.removeWriter(user);
                specChat.removeListener(user);

                Server.getGlobalChat().addWriter(user);
                Server.getGlobalChat().addListener(user);
                user.clearInventory();
            }

            this.getSpectatorManager().clearTools();

            this.gameScheduler.closeGame();
        }
    }

    public final void closeGame6() {
        this.toolManager.runTools(PreCloseableTool.class);
    }

    public final void closeGame10() {
        Server.getChat().broadcastJoinQuit(false);
        for (User user : Server.getUsers()) {
            if (!user.isAirMode() && !user.isService()) {
                user.getDatabase().setKit(null);
                ((GameUser) user).setTeam(null);
            }

            if (LoungeBridgeServer.getGame().hasTexturePack()) {
                user.setTexturePack(
                        "https://timesnake.de/data/minecraft/resource_packs/texture_pack_base.zip");
            }

            user.switchToServer(LoungeBridgeServer.getTwinServer());
        }
    }

    public final void resetGame() {
        LoungeBridgeServer.setState(LoungeBridgeServer.State.RESETTING);

        this.toolManager.runTools(ResetableTool.class);

        if (this.getMap() != null && this.getMap() instanceof ResetableMap) {
            ((ResetableMap) this.getMap()).reset();
        }

        LoungeBridgeServer.resetKillsAndDeaths();

        this.onGameReset();
        LoungeBridgeServer.setState(LoungeBridgeServer.State.WAITING);
    }

    public de.timesnake.basic.loungebridge.core.main.SpectatorManager getSpectatorManager() {
        return (de.timesnake.basic.loungebridge.core.main.SpectatorManager) super.getSpectatorManager();
    }

    public Chat getSpectatorChat() {
        return Server.getChat(SPECTATOR_NAME);
    }

    public void updateSpectatorTools() {
        this.getSpectatorManager().updateSpectatorTools();
    }

    public boolean isTeamMateDamage() {
        return teamMateDamage;
    }

    public void setTeamMateDamage(boolean teamMateDamage) {
        this.teamMateDamage = teamMateDamage;
    }

    public TablistTeam getTablistGameTeam() {
        return this.tablistManager.getTablistGameTeam();
    }

    public TablistTeam getTablistSpectatorTeam() {
        return this.tablistManager.getSpectatorTeam();
    }

    public boolean areKitsEnabled() {
        return kitsEnabled;
    }

    public boolean areMapsEnabled() {
        return mapsEnabled;
    }

    public UserManager getLoungeBridgeUserManager() {
        return this.userManager;
    }

    public boolean isDiscord() {
        return discord;
    }

    public void setDiscord(boolean discord) {
        this.discord = discord;
    }

    public Integer getMaxPlayersPerTeam() {
        return maxPlayersPerTeam;
    }

    public ToolManager getToolManager() {
        return toolManager;
    }

    public boolean isGameRunning() {
        return this.running;
    }

    public Map getMap() {
        return map;
    }

    public void setMap(Map map) {
        this.map = map;
        this.tablistManager.updateMapFooter(map);
    }

    public Integer getServerTeamAmount() {
        return serverTeamAmount;
    }

    public DbLoungeServer getTwinServer() {
        return twinServer;
    }

    public int getGameCountdown() {
        return this.gameScheduler.getGameCountdown();
    }

    @Deprecated
    public void broadcastLoungeBridgeMessage(String msg) {
        Server.broadcastMessage(
                de.timesnake.library.extension.util.chat.Chat.getSenderPlugin(this.getGamePlugin())
                        .append(Component.text(msg)));
    }

    public void broadcastLoungeBridgeMessage(Component msg) {
        Server.broadcastMessage(
                de.timesnake.library.extension.util.chat.Chat.getSenderPlugin(this.getGamePlugin())
                        .append(msg));
    }

    public void broadcastGameMessage(Component msg) {
        Server.broadcastMessage(this.getGamePlugin(), msg);
    }

    public void broadcastGameTDMessage(String msg) {
        Server.broadcastTDMessage(this.getGamePlugin(), msg);
    }

    public TeamTablist getGameTablist() {
        return this.tablistManager.getGameTablist();
    }

    public Collection<Team> getNotEmptyInGameTeams() {
        return this.getGame().getTeams().stream().filter((team -> team.getInGameUsers().size() > 0))
                .collect(Collectors.toList());
    }

    public Integer getEstimatedPlayers() {
        return this.estimatedPlayers;
    }

    public void setEstimatedPlayers(Integer amount) {
        this.estimatedPlayers = amount;
    }

    public LoungeBridgeServer.State getState() {
        return state;
    }

    public void setState(LoungeBridgeServer.State state) {
        this.state = state;
        switch (state) {
            case RUNNING, STOPPED -> Server.setStatus(Status.Server.IN_GAME);
            case STARTING -> Server.setStatus(Status.Server.PRE_GAME);
            case CLOSING, RESETTING -> Server.setStatus(Status.Server.POST_GAME);
            case WAITING -> {
                Server.setStatus(Status.Server.ONLINE);
                Server.getChannel().sendMessage(
                        new ChannelServerMessage<>(Server.getName(), MessageType.Server.STATE
                                , ChannelServerMessage.State.READY));
                Server.printText(Plugin.GAME, "Send lounge ready state");
            }
        }
    }

    public DiscordManager getDiscordManager() {
        return discordManager;
    }
}
