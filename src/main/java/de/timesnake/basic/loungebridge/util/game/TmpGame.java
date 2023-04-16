/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.basic.loungebridge.util.game;

import de.timesnake.basic.loungebridge.util.user.Kit;
import de.timesnake.basic.loungebridge.util.user.KitManager;
import de.timesnake.database.util.game.DbGame;
import de.timesnake.database.util.game.DbKit;
import de.timesnake.database.util.game.DbTmpGame;
import java.util.Optional;

public class TmpGame extends de.timesnake.basic.game.util.game.TmpGame {

    protected KitManager<?> kitManager;

    public TmpGame(DbTmpGame database, boolean loadWorlds) {
        super(database, loadWorlds);
    }

    public KitManager<?> loadKitManager() {
        return null;
    }

    @Override
    public void loadKits(DbGame database) {
        this.kitManager = this.loadKitManager();

        if (this.kitManager == null) {
            return;
        }

        this.kits.addAll(this.getKitManager().getKits());
    }

    public KitManager<?> getKitManager() {
        return kitManager;
    }

    @Override
    public Optional<? extends Kit> loadKit(DbKit dbKit) {
        return this.getKitManager().getKit(dbKit.getId());
    }
}
