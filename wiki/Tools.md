# Game Tools

## Gameplay

### AntiCampTool

Teleports players if they are camping (staying in same area).

```java
new AntiCampTool(time, range) {

  @Override
  public void teleport(User user) {
    // telport user to custom location
  }
};
```

### DeadBody

Provides a tool to spawn and despawn "dead" players.

```java
DeadPlayer body = new DeadPlayer(user, location);
body.spawn(); // spawn body at given location
body.despawn(); // remove body
```

### ItemSpawner

Spawns items at given location at fixed/random intervall.
The spawning starts at game start and stops at game end. So spawner can be created while loading the map.

```java
new ItemSpawner(locationIndex, // index of location (of map)
                unit, // time unit
                delay, // min spawn delay
                delayRange, // random spawn offset
                items); // item to spawn
```

### WorldBorderTool

Sets a world border.

```java
WorldBorderTool worldBorder = new WorldBorderTool() {
  
  @Override
  public Location getBorderCenter() {
    return center; // set center
  }
  
  @Override
  public double getBorderSize() {
    return size; // set size
  }

  @Override
  public double getBorderDamagePerSec() {
    return damage; // set damage
  }
};

worldBorder.shrinkBorder(size, // target size
                         time); // time to target size
```

## Display

### BossBar(Map)TimerTool

Shows a timer in the boss bar.
BossBarMapTimerTool loads the time from the map (map must implement the `Timeable` interface).

```java
new BossBarTimerTool(time) {

  @Override
  public ToolWatcher getWatchers() {
    return toolWatcher; // define which users can see it: ALL, IN_GAME, SPECTATOR
  }

  @Override
  public String getTitle(String time) {
    return "";
  }
};
```