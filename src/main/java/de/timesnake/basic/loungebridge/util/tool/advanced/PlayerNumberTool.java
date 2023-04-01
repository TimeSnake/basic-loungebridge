/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.basic.loungebridge.util.tool.advanced;

import de.timesnake.basic.game.util.user.SpectatorUser;
import de.timesnake.basic.loungebridge.util.tool.listener.GameUserJoinListener;
import de.timesnake.basic.loungebridge.util.tool.listener.GameUserQuitListener;
import de.timesnake.basic.loungebridge.util.tool.listener.SpectatorUserJoinListener;
import de.timesnake.basic.loungebridge.util.tool.listener.SpectatorUserQuitListener;
import de.timesnake.basic.loungebridge.util.user.GameUser;

public abstract class PlayerNumberTool implements GameUserJoinListener, GameUserQuitListener,
        SpectatorUserJoinListener, SpectatorUserQuitListener {

    @Override
    public void onGameUserJoin(GameUser user) {
        this.onPlayerUpdate();
    }

    @Override
    public void onGameUserQuit(GameUser user) {
        this.onPlayerUpdate();
    }

    @Override
    public void onSpectatorUserJoin(SpectatorUser user) {
        this.onPlayerUpdate();
    }

    @Override
    public void onSpectatorUserQuit(SpectatorUser user) {
        this.onPlayerUpdate();
    }

    public abstract void onPlayerUpdate();
}
