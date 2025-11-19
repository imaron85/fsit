package dev.rvbsm.fsit.mixin.server;

import com.mojang.authlib.GameProfile;
import dev.rvbsm.fsit.FSitMod;
import dev.rvbsm.fsit.api.access.Configurable;
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
public abstract class ServerPlayerMixin_configStore extends Player implements Configurable {

    @Unique
    private @Nullable ConfigData config;

    //? if >=1.21.6 {
    private ServerPlayerMixin_configStore(Level world, GameProfile profile) {
        super(world, profile);
    }
    //?} else {
    /*private ServerPlayerEntityMixin_configStore(World world, net.minecraft.util.math.BlockPos pos, float yaw, GameProfile gameProfile) {
        super(world, pos, yaw, gameProfile);
    }
    *///?}

    @Inject(method = "restoreFrom", at = @At("TAIL"))
    private void copyConfig(ServerPlayer oldPlayer, boolean alive, CallbackInfo ci) {
        final ConfigData oldConfig = ((Configurable) oldPlayer).fsit$getConfig();
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
//        return Optional.ofNullable(this.config);
        return this.config;
    }

    @Override
    public boolean fsit$hasConfig() {
        return this.config != null;
    }

    @Override
    public ConfigData fsit$configOrDefault() {
        final ConfigData config = this.fsit$getConfig();
        final ConfigData modConfig = FSitMod.getInstance().getConfig();
        if (config == null || modConfig.useServer() || config.useServer()) {
            return modConfig;
        }

        return config;
    }
}
