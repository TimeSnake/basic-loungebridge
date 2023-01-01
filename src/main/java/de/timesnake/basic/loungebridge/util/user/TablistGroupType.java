/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.basic.loungebridge.util.user;

import de.timesnake.basic.bukkit.util.user.scoreboard.TablistableGroup;

public class TablistGroupType extends de.timesnake.basic.game.util.game.TablistGroupType {

    public static final TablistGroupType GAME = new TablistGroupType(TablistTeam.class);

    public TablistGroupType(Class<? extends TablistableGroup> groupClass) {
        super(groupClass);
    }
}
