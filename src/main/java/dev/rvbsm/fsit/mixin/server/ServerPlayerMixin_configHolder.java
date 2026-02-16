package dev.rvbsm.fsit.mixin.server;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.authlib.GameProfile;
import dev.rvbsm.fsit.FSitMod;
import dev.rvbsm.fsit.api.ConfigHolder;
import dev.rvbsm.fsit.config.ConfigData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin_configHolder extends Player implements ConfigHolder {

    @Unique
    private @Nullable ConfigData config;

    private ServerPlayerMixin_configHolder(Level world, GameProfile profile) {
        super(world, profile);
    }

    @Inject(method = "restoreFrom", at = @At("TAIL"))
    private void copyConfig(CallbackInfo ci, @Local(argsOnly = true) final ServerPlayer oldPlayer) {
        final ConfigData oldConfig = ConfigHolder.get(oldPlayer);
        if (oldConfig != null) {
            this.fsit$setConfig(oldConfig);
        }
    }

    @Override
    public void fsit$setConfig(ConfigData config) {
        this.config = config;
    }

    @Override
    public @Nullable ConfigData fsit$getConfig() {
        return this.config;
    }

    @Override
    public ConfigData fsit$getServerConfig() {
        return FSitMod.getInstance().getConfig();
    }
}
