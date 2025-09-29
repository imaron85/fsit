package dev.rvbsm.fsit.mixin.server;

import com.mojang.authlib.GameProfile;
import dev.rvbsm.fsit.api.access.Configurable;
import dev.rvbsm.fsit.config.ConfigData;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin_configStore extends PlayerEntity implements Configurable {

    @Unique
    private @Nullable ConfigData config;

    //? if >=1.21.6 {
    private ServerPlayerEntityMixin_configStore(World world, GameProfile profile) {
        super(world, profile);
    }
    //?} else {
    /*private ServerPlayerEntityMixin_configStore(World world, net.minecraft.util.math.BlockPos pos, float yaw, GameProfile gameProfile) {
        super(world, pos, yaw, gameProfile);
    }
    *///?}

    @Inject(method = "copyFrom", at = @At("TAIL"))
    private void copyConfig(ServerPlayerEntity oldPlayer, boolean alive, CallbackInfo ci) {
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
}
