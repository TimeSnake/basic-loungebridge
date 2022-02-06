package de.timesnake.basic.loungebridge.util.chat;

public class Plugin extends de.timesnake.basic.bukkit.util.chat.Plugin {

    public static final Plugin LOUNGE = new Plugin("Lounge", "BLB");
    public static final Plugin GAME = new Plugin("Game", "BLG");

    protected Plugin(String name, String code) {
        super(name, code);
    }
}
