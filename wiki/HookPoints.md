# Hook Points

## LoungeBridgeServerManager

The LoungeBridgeServerManager class is the main hook-point for a game plugin. It is required to
extend this class and
to
initialize the manager by calling the `onLoungeBridgeEnable()` method.

**Required Hook-Points:**

- `GameUser loadUser(Player player)` - inject own user class
- `Plugin getGamePlugin()` - chat plugin
- `boolean isGameRunning()`
- `broadcastGameMessage(String message)`
- `void onGameStart()` - called on game start
- `void onGameStop()` - called on game stop
- `void onGameReset()` - called on game reset, after all users are left
- `void onGameUserQuit(GameUser user)`
- `void onGameUserQuitBeforeStart(GameUser user)`
- `boolean isRejoiningAllowed()`- return true if allow rejoining, else false
- `Location getSpectatorSpawn()` - return the spectator spawn location

**Optional Hook-Points:**

- `BowRunGame loadGame(DbGame dbGame, boolean loadWorlds)` - inject own game class, map or kit (see
  game section)
- `void onMapLoad()` - called on map load, triggered by the map voting
- `void onGamePrepare()` - called when the countdown starts running (7s before game start)
- `void onGameUserRejoin(GameUser user)` - called if a user rejoins
- `Sideboard getSpectatorSideboard()` - return a spectator sideboard
- `Kit getKit(int index)` - return the kit by the giving index, *required for games with kits*
- `Kit[] getKits()` - returns all kits, *required for games with kits*
- `OfflineUser loadOfflineUser(GameUser user)` - called if a users leaves, allows to save data for
  rejoin
- `Set<StatType<?>> getStats()` - returns the stat-type set, allows to add own stat-types
- `void saveGameUserStats(GameUser user)` - allows to save custom stats after the game end

## Generic Hook Points

There are two types of generic hook points. Firstly, game state dependent events. Secondly, user initiated events.
Simply implement these hook points and register the class with `LoungeBridgeServer.getToolManager().add(tool)`.

### Game State

| Name              | Description                       |
|-------------------|-----------------------------------|
| MapLoadableTool   | on map load                       |
| WorldLoadableTool | on world load                     |
| StartableTool     | on game start                     |
| PreStopableTool   | on game stop (before actual stop) |
| StopableTool      | on game stop (after actual stop)  |
| PreCloseableTool  | on game close info                |
| CloseableTool     | on game close                     |
| ResetableTool     | on game reset                     |

### User Events

`GameUser` events are only triggered by actual in-/pre-/post-game users.
`SpectatorUser` events are only triggered by spectator/out-game users.

| Name                      | Description                         |
|---------------------------|-------------------------------------|
| GameUserDeathListener     | on user death                       |
| GameUserJoinListener      | on user join                        |
| GameUserQuitListener      | on user quit                        |
| GameUserRespawnListener   | on user respawn                     |
| SpectatorUserJoinListener | on spectator join                   |
| SpectatorUserQuitListener | on spectator quit                   |
| UserJoinQuitListener      | on all user and spectator join/quit |
