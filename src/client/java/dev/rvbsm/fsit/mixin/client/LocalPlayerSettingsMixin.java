package dev.rvbsm.fsit.mixin.client;

import dev.rvbsm.fsit.api.FSitPlayerSettings;
import dev.rvbsm.fsit.api.PlayerSettings;
import dev.rvbsm.fsit.client.FSitModClient;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(LocalPlayer.class)
public abstract class LocalPlayerSettingsMixin implements FSitPlayerSettings {

    @Override
    public PlayerSettings fsit$getSettings() {
        return FSitModClient.getInstance().getPlayerSettings();
    }

    @Override
    public void fsit$setSettings(PlayerSettings settings) {
        FSitModClient.getInstance().setPlayerSettings(settings);
    }
}
