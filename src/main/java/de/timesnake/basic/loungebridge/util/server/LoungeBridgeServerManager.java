package de.timesnake.basic.loungebridge.util.server;

import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.ServerManager;
import de.timesnake.basic.bukkit.util.chat.Chat;
import de.timesnake.basic.bukkit.util.chat.ChatColor;
import de.timesnake.basic.bukkit.util.permission.Group;
import de.timesnake.basic.bukkit.util.user.User;
import de.timesnake.basic.bukkit.util.user.scoreboard.Sideboard;
import de.timesnake.basic.bukkit.util.user.scoreboard.Tablist;
import de.timesnake.basic.bukkit.util.user.scoreboard.TablistGroupType;
import de.timesnake.basic.bukkit.util.user.scoreboard.TeamTablist;
import de.timesnake.basic.game.util.GameServer;
import de.timesnake.basic.game.util.GameServerManager;
import de.timesnake.basic.game.util.Map;
import de.timesnake.basic.game.util.Team;
import de.timesnake.basic.loungebridge.core.ChannelListener;
import de.timesnake.basic.loungebridge.core.GameScheduler;
import de.timesnake.basic.loungebridge.core.SpectatorManager;
import de.timesnake.basic.loungebridge.core.UserManager;
import de.timesnake.basic.loungebridge.core.main.BasicLoungeBridge;
import de.timesnake.basic.loungebridge.util.chat.Plugin;
import de.timesnake.basic.loungebridge.util.user.*;
import de.timesnake.channel.api.message.ChannelServerMessage;
import de.timesnake.database.util.game.DbGame;
import de.timesnake.database.util.object.Status;
import de.timesnake.database.util.object.UnsupportedStringException;
import de.timesnake.database.util.server.DbLoungeServer;
import de.timesnake.database.util.server.DbTempGameServer;
import de.timesnake.library.basic.util.statistics.Stat;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

public abstract class LoungeBridgeServerManager extends GameServerManager {

    public static final String SPECTATOR_NAME = "spectator";
    public static final String SPECTATOR_CHAT_DISPLAY_NAME = "Spec";
    public static final String SPECTATOR_TABLIST_PREFIX = "";
    public static final org.bukkit.ChatColor SPECTATOR_CHAT_COLOR = org.bukkit.ChatColor.GRAY;
    public static final org.bukkit.ChatColor SPECTATOR_TABLIST_CHAT_COLOR = org.bukkit.ChatColor.GRAY;
    public static final org.bukkit.ChatColor SPECTATOR_TABLIST_PREFIX_CHAT_COLOR = org.bukkit.ChatColor.GRAY;

    public static final Integer MAX_START_DELAY = 5 * 20; // max start delay after first join

    public static final Stat<Integer> GAMES_PLAYED = Stat.Type.INTEGER.asStat("games_played", "Games Played", 0, 0, 1);

    public static final Set<Stat<?>> BASE_STATS = Set.of(GAMES_PLAYED);

    public static LoungeBridgeServerManager getInstance() {
        return (LoungeBridgeServerManager) ServerManager.getInstance();
    }


    protected DbLoungeServer twinServer;

    protected TeamTablist gameTablist;
    private TablistTeam tablistGameTeam;
    private TablistTeam spectatorTeam;

    private Map map;

    private UserManager userManager;

    private GameScheduler gameScheduler;

    protected LoungeBridgeServer.State state;

    protected SpectatorManager spectatorManager;

    protected ChannelListener channelListener;

    protected boolean kitsEnabled;
    protected boolean mapsEnabled;

    protected Integer serverTeamAmount;
    protected Integer maxPlayersPerTeam;

    protected boolean teamMateDamage = true;

    protected Integer estimatedPlayers;

    public final void onLoungeBridgeEnable() {
        super.onGameEnable();

        this.spectatorManager = new SpectatorManager();

        // update kits into database, if enabled
        this.kitsEnabled = ((DbTempGameServer) this.getDatabase()).areKitsEnabled();
        if (this.kitsEnabled) {
            this.loadKitsIntoDatabase();
        }

        this.loadStatTypesIntoDatabase();

        // search twin server
        DbTempGameServer database = ((DbTempGameServer) Server.getDatabase());
        this.twinServer = database.getTwinServer();
        if (twinServer == null) {
            Server.printError(Plugin.LOUNGE, "No twin server found in database");
            Bukkit.shutdown();
            return;
        }

        this.userManager = new UserManager();
        this.gameScheduler = new GameScheduler();

        this.mapsEnabled = ((DbTempGameServer) this.getDatabase()).areMapsEnabled();
        Integer serverTeamAmount = ((DbTempGameServer) this.getDatabase()).getTeamAmount();
        this.serverTeamAmount = serverTeamAmount != null ? serverTeamAmount : this.getGame().getTeams().size();
        this.maxPlayersPerTeam = ((DbTempGameServer) this.getDatabase()).getMaxPlayersPerTeam();

        this.channelListener = new ChannelListener();

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

        this.loadTablist();


        // add team header, create team chat
        for (Team team : this.getGame().getTeams()) {

            // chat
            Server.getChatManager().createChat(team.getName(), team.getDisplayName(), team.getChatColor(), new HashSet<>());
        }

        // create spectator chat
        Server.getChatManager().createChat(SPECTATOR_NAME, SPECTATOR_CHAT_DISPLAY_NAME, SPECTATOR_CHAT_COLOR, new HashSet<>());

        // set tablist footer and activate tablist
        this.gameTablist.setHeader("§6" + this.getGame().getDisplayName());
        this.gameTablist.setFooter("§7Server: " + Server.getName() + "\n§cSupport: /ticket or \nsupport@timesnake.de");
        Server.getScoreboardManager().setActiveTablist(this.gameTablist);

        // mark as ready
        Server.printText(Plugin.GAME, "Server loaded");
        this.setState(LoungeBridgeServer.State.WAITING);
    }

    protected void loadTablist() {
        // create team tablist
        LinkedList<TablistGroupType> types = new LinkedList<>();
        types.add(Group.getTablistType());

        // create spectatorTeam
        this.spectatorTeam = new TablistTeam("0", SPECTATOR_NAME, SPECTATOR_TABLIST_PREFIX, SPECTATOR_TABLIST_CHAT_COLOR, SPECTATOR_TABLIST_PREFIX_CHAT_COLOR);

        if (this.getServerTeamAmount() > 0) {
            if (this.maxPlayersPerTeam == null) {
                this.gameTablist = Server.getScoreboardManager().registerNewTeamTablist("game", Tablist.Type.DUMMY, TeamTablist.ColorType.TEAM, this.getGame().getTeams(), de.timesnake.basic.game.util.TablistGroupType.GAME_TEAM, types, this.spectatorTeam, types, (e, tablist) -> {
                    User user = e.getUser();
                    String task = user.getTask();

                    if (task == null) {
                        ((TeamTablist) tablist).addRemainEntry(e.getUser());
                        return;
                    }

                    if (task.equalsIgnoreCase(this.getGame().getName())) {
                        if (e.getUser().getStatus().equals(Status.User.PRE_GAME) || e.getUser().getStatus().equals(Status.User.IN_GAME)) {
                            tablist.addEntry(e.getUser());
                        } else {
                            ((TeamTablist) tablist).addRemainEntry(e.getUser());
                        }
                    }
                }, (e, tablist) -> tablist.removeEntry(e.getUser()));

                for (Team team : this.getGame().getTeamsSortedByRank(this.serverTeamAmount).values()) {

                    this.gameTablist.addTeamHeader(team.getTablistRank(), "0", team.getTablistChatColor() + "§l" + team.getTablistName());
                }
            } else {
                LinkedList<TablistGroupType> gameTeamTypes = new LinkedList<>(types);
                gameTeamTypes.addFirst(de.timesnake.basic.game.util.TablistGroupType.GAME_TEAM);
                this.tablistGameTeam = new TablistTeam("0", "game", "", ChatColor.WHITE, ChatColor.WHITE);

                this.gameTablist = Server.getScoreboardManager().registerNewTeamTablist("game", Tablist.Type.DUMMY, TeamTablist.ColorType.FIRST_GROUP, List.of(this.tablistGameTeam), this.tablistGameTeam.getTeamType(), gameTeamTypes, this.spectatorTeam, types, (e, tablist) -> {
                    User user = e.getUser();
                    String task = user.getTask();

                    if (task == null) {
                        ((TeamTablist) tablist).addRemainEntry(e.getUser());
                    }

                    if (task.equalsIgnoreCase(GameServer.getGame().getName())) {
                        if (e.getUser().getStatus().equals(Status.User.PRE_GAME) || e.getUser().getStatus().equals(Status.User.IN_GAME)) {
                            tablist.addEntry(e.getUser());
                        } else {
                            ((TeamTablist) tablist).addRemainEntry(e.getUser());
                        }
                    }
                }, (e, tablist) -> tablist.removeEntry(e.getUser()));
            }
        } else {
            this.tablistGameTeam = new TablistTeam("0", "game", "", ChatColor.WHITE, ChatColor.WHITE);

            this.gameTablist = Server.getScoreboardManager().registerNewTeamTablist("game", Tablist.Type.DUMMY, TeamTablist.ColorType.WHITE, List.of(this.tablistGameTeam), this.tablistGameTeam.getTeamType(), types, this.spectatorTeam, types, (e, tablist) -> {
                User user = e.getUser();
                String task = user.getTask();

                if (task == null) {
                    ((TeamTablist) tablist).addRemainEntry(e.getUser());
                }

                if (task.equalsIgnoreCase(GameServer.getGame().getName())) {
                    if (e.getUser().getStatus().equals(Status.User.PRE_GAME) || e.getUser().getStatus().equals(Status.User.IN_GAME)) {
                        tablist.addEntry(e.getUser());
                    } else {
                        ((TeamTablist) tablist).addRemainEntry(e.getUser());
                    }
                }
            }, (e, tablist) -> tablist.removeEntry(e.getUser()));
        }
    }

    @Override
    public abstract GameUser loadUser(Player player);

    public void checkGameStart() {
        int gameUsers = Server.getPreGameUsers().size();
        if (gameUsers == LoungeBridgeServer.getEstimatedPlayers()) {
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

    public void closeGame() {
        this.gameScheduler.closeGame();
    }

    public void saveGameStats() {
        Server.printText(Plugin.GAME, "Saved game stats", "Stats");
        for (User user : this.getGameUsers()) {
            if (((GameUser) user).hasPlayedGame()) {
                this.saveGameUserStats(((GameUser) user));
            }
        }
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
                game.addKit(kit.getId(), kit.getName(), kit.getMaterial().toString(), kit.getDescription());
                Server.printText(Plugin.LOUNGE, "Loaded kit " + kit.getName() + " into the database", "Kit");
            } catch (UnsupportedStringException e) {
                Server.printError(Plugin.LOUNGE, "Can not load kit " + kit.getName() + " into database " + "(UnsupportedStringException)", "Kit");
            } catch (Exception e) {
                Server.printError(Plugin.LOUNGE, "Can not load kit " + kit.getName() + " into database ", "Kit");
            }
        }
    }

    public void loadStatTypesIntoDatabase() {
        if (this.getStats() == null) {
            return;
        }

        DbGame game = this.getGame().getDatabase();

        for (Stat<?> stat : BASE_STATS) {
            game.removeStat(stat);
            game.addStat(stat);
        }

        for (Stat<?> stat : this.getStats()) {
            game.removeStat(stat);
            game.addStat(stat);
        }

    }

    public Map getMap() {
        return map;
    }

    public void setMap(Map map) {
        this.map = map;
        if (this.gameTablist != null) {
            if (this.map != null) {
                StringBuilder authors = new StringBuilder();
                for (String author : this.map.getAuthors(18)) {
                    if (authors.length() != 0) {
                        authors.append("\n");
                    }
                    authors.append(author);

                }
                this.gameTablist.setHeader(ChatColor.GOLD + this.getGame().getDisplayName() + ChatColor.GRAY + ": " + ChatColor.DARK_GREEN + this.getMap().getDisplayName() + "\n" + ChatColor.GRAY + " by " + ChatColor.BLUE + authors);
            } else {
                this.gameTablist.setHeader(ChatColor.GOLD + this.getGame().getDisplayName());
            }
        }

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

    public void broadcastLoungeBridgeMessage(String msg) {
        Server.broadcastMessage(Server.getChat().getSenderPlugin(this.getGamePlugin()) + msg);
    }

    public TeamTablist getGameTablist() {
        return gameTablist;
    }

    public void setState(LoungeBridgeServer.State state) {
        this.state = state;
        switch (state) {
            case RUNNING:
            case STOPPED:
                Server.setStatus(Status.Server.IN_GAME);
                break;
            case STARTING:
                Server.setStatus(Status.Server.PRE_GAME);
                break;
            case CLOSING:
            case RESETTING:
                Server.setStatus(Status.Server.POST_GAME);
                break;
            case WAITING:
                Server.setStatus(Status.Server.ONLINE);
                Server.getChannel().sendMessage(ChannelServerMessage.getStateMessage(Server.getPort(), ChannelServerMessage.State.READY));
                Server.printText(Plugin.GAME, "Send lounge ready state");
                break;
        }
    }

    public GameUser getMostKills(Collection<GameUser> users) {
        if (users == null || users.isEmpty()) return null;
        return Collections.max(users, Comparator.comparing(GameUser::getKills));
    }

    public GameUser getHighestKillStreak(Collection<GameUser> users) {
        if (users == null || users.isEmpty()) return null;
        return Collections.max(users, Comparator.comparing(GameUser::getHighestKillStreak));
    }

    public GameUser getMostDeaths(Collection<GameUser> users) {
        if (users == null || users.isEmpty()) return null;
        return Collections.max(users, Comparator.comparing(GameUser::getDeaths));
    }

    public GameUser getHighestKD(Collection<GameUser> users) {
        if (users == null || users.isEmpty()) return null;
        return Collections.max(users, Comparator.comparing(GameUser::getKillDeathRatio));
    }

    public GameUser getLongestShot(Collection<GameUser> users) {
        if (users == null || users.isEmpty()) return null;
        return Collections.max(users, Comparator.comparing(GameUser::getLongestShot));
    }

    public Collection<Team> getNotEmptyInGameTeams() {
        return this.getGame().getTeams().stream().filter((team -> team.getInGameUsers().size() > 0)).collect(Collectors.toList());
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

    public SpectatorManager getSpectatorManager() {
        return spectatorManager;
    }

    public Chat getSpectatorChat() {
        return Server.getChat(SPECTATOR_NAME);
    }

    public void updateSpectatorTools() {
        this.spectatorManager.updateSpectatorTools();
    }

    public boolean isTeamMateDamage() {
        return teamMateDamage;
    }

    public void setTeamMateDamage(boolean teamMateDamage) {
        this.teamMateDamage = teamMateDamage;
    }

    public TablistTeam getTablistGameTeam() {
        return tablistGameTeam;
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

    /**
     * Get the game {@link de.timesnake.basic.bukkit.util.chat.Plugin}
     *
     * @return Return the plugin
     */
    @GamePlugin
    public abstract de.timesnake.library.basic.util.chat.Plugin getGamePlugin();

    /**
     * @return Return true if the game is running
     */
    @GamePlugin
    public abstract boolean isGameRunning();

    /**
     * Broadcast game info chat messages
     *
     * @param message The message to broadcast
     */
    @GamePlugin
    public abstract void broadcastGameMessage(String message);

    /**
     * Countdown 7s, during user join
     */
    @GamePlugin
    public abstract void prepareGame();

    /**
     * Called by channel map load message from lounge (map-voting).
     */
    @GamePlugin
    public void loadMap() {

    }

    /**
     * Countdown 0s
     */
    @GamePlugin
    public abstract void startGame();

    /**
     * Ingame user quits the server
     *
     * @param user The {@link User} who left
     */
    @GamePlugin
    public abstract void onGameUserQuit(GameUser user);

    /**
     * Ingame user quits before the game started.
     *
     * @param user The {@link User} who left
     */
    @GamePlugin
    public abstract void onGameUserQuitBeforeStart(GameUser user);

    /**
     * Allows users to rejoin the game
     *
     * @return true if it is allowed
     */
    @GamePlugin
    public abstract boolean isRejoiningAllowed();

    /**
     * Game user rejoins game
     *
     * @param user The user who is rejoining
     */
    @GamePlugin
    public void onGameUserRejoin(GameUser user) {

    }

    /**
     * Called after all users quit
     */
    @GamePlugin
    public abstract void resetGame();

    @GamePlugin
    public Sideboard getSpectatorSideboard() {
        return null;
    }

    @GamePlugin
    public Kit getKit(int index) throws KitNotDefinedException {
        return null;
    }

    @GamePlugin
    public Kit[] getKits() {
        return new Kit[]{};
    }

    @GamePlugin
    public abstract Location getSpectatorSpawn();

    @GamePlugin
    public OfflineUser loadOfflineUser(GameUser user) {
        return new OfflineUser(user);
    }

    @GamePlugin
    public Set<Stat<?>> getStats() {
        return new HashSet<>();
    }

    @GamePlugin
    public void saveGameUserStats(GameUser user) {
        user.increaseStat(GAMES_PLAYED, 1);
    }
}
