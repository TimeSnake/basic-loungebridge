# basic-loungebridge

## Setup

This module is part of the plugin-project [1]. You can use it as standalone project or as bundle.

### Standalone

Clone this project and enter your gitlab credentials in your gradle user home
properties (`~/.gradle/gradle.propreties`):

```
timesnakeUser=<user>
timesnakePassword=<access_token>

timesnakePluginDir=<plugins_dir>
```

Replace `<user>` with your gitlab username and `<access_token>` with an access-token.
You can optionally replace `<plugins_dir>` with a directory to export the plugin directly (therefore run the
gradle `exportAsPlugin` task).

### Bundle

To use this project in the multimodule plugin-project, read the setup guide in the root module [1].

## Testing

To test this module, you must set up a test server, therefore read regarding guide in the root module [1].

## Description

This plugin provides an API for all TmpGames and acts as bridge to the lounge server.

## Usage

All classes in the [de.timesnake.basic.loungebridge.util] module are intended to be used in the game
plugins.
It is highly recommended to not use the classes in the [de.timesnake.basic.loungebridge.core]
package.

### [LoungeBridgeServerManager]

The [LoungeBridgeServerManager] class is the main hook-point for a game plugin. It is required to
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

### Game

**Map**

**Team**

**Kit**

### GameUser

### GameTools

[LoungeBridgeServerManager]: src/main/java/de/timesnake/basic/loungebridge/util/server/LoungeBridgeServerManager.java

[de.timesnake.basic.loungebridge.util]: src/main/java/de/timesnake/basic/loungebridge/util/

[de.timesnake.basic.loungebridge.core]: src/main/java/de/timesnake/basic/loungebridge/core/
[root module](https://git.timesnake.de/timesnake/minecraft/plugin-root-project)

## Code Style

The code style guide can be found in the plugin root project [1].

## License

The source is licensed under the GNU GPLv2 license that can be found in the [LICENSE](LICENSE)
  file.

[1] https://git.timesnake.de/timesnake/minecraft/plugin-root-project