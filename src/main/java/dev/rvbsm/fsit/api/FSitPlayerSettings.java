package dev.rvbsm.fsit.api;

import net.minecraft.world.entity.player.Player;

public interface FSitPlayerSettings {

    static PlayerSettings get(Player player) {
        if (player instanceof FSitPlayerSettings holder) {
            var settings = holder.fsit$getSettings();
            if (settings != null) return settings;
        }
        return PlayerSettings.DEFAULT;
    }

    static void set(Player player, PlayerSettings settings) {
        if (player instanceof FSitPlayerSettings holder) {
            holder.fsit$setSettings(settings);
        }
    }

    PlayerSettings fsit$getSettings();

    void fsit$setSettings(PlayerSettings settings);
}
