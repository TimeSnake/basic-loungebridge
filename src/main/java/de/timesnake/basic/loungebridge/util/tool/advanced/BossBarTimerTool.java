/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.basic.loungebridge.util.tool.advanced;

import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.user.User;
import de.timesnake.basic.game.util.user.SpectatorUser;
import de.timesnake.basic.loungebridge.util.tool.ToolWatcher;
import de.timesnake.basic.loungebridge.util.tool.WatchableTool;
import de.timesnake.basic.loungebridge.util.tool.listener.GameUserJoinListener;
import de.timesnake.basic.loungebridge.util.tool.listener.GameUserQuitListener;
import de.timesnake.basic.loungebridge.util.tool.listener.SpectatorUserJoinListener;
import de.timesnake.basic.loungebridge.util.tool.listener.SpectatorUserQuitListener;
import de.timesnake.basic.loungebridge.util.user.GameUser;
import de.timesnake.library.extension.util.chat.Chat;
import de.timesnake.library.extension.util.player.UserSet;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;

public abstract class BossBarTimerTool extends TimerTool implements WatchableTool,
        GameUserJoinListener, GameUserQuitListener, SpectatorUserJoinListener,
        SpectatorUserQuitListener {

    private final UserSet<User> listeners = new UserSet<>();
    private final BossBar bar;

    public BossBarTimerTool(int time, BarColor color) {
        super(time);
        this.bar = Server.createBossBar("", color, BarStyle.SOLID);
    }

    @Override
    public void prepare() {
        super.prepare();

        this.bar.setTitle(this.getTitle(Chat.getTimeString(this.time)));
        this.bar.setProgress(1);
    }

    @Override
    public void onTimerUpdate() {
        this.bar.setTitle(this.getTitle(Chat.getTimeString(this.time)));
        this.bar.setProgress(this.time / ((double) this.maxTime));
    }

    @Override
    public void onTimerEnd() {
        this.listeners.forEach(u -> u.removeBossBar(this.bar));
        this.listeners.clear();
    }

    @Override
    public void onGameUserJoin(GameUser user) {
        if (this.getWatchers() == ToolWatcher.IN_GAME || this.getWatchers() == ToolWatcher.ALL) {
            user.addBossBar(this.bar);
            this.listeners.add(user);
        }
    }

    @Override
    public void onGameUserQuit(GameUser user) {
        if (this.getWatchers() == ToolWatcher.IN_GAME || this.getWatchers() == ToolWatcher.ALL) {
            user.removeBossBar(this.bar);
        }
    }

    @Override
    public void onSpectatorUserJoin(SpectatorUser user) {
        if (this.getWatchers() == ToolWatcher.SPECTATOR || this.getWatchers() == ToolWatcher.ALL) {
            user.addBossBar(this.bar);
            this.listeners.add(user);
        }
    }

    @Override
    public void onSpectatorUserQuit(SpectatorUser user) {
        if (this.getWatchers() == ToolWatcher.SPECTATOR || this.getWatchers() == ToolWatcher.ALL) {
            user.removeBossBar(this.bar);
        }
    }

    public abstract String getTitle(String time);

}