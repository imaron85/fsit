package dev.rvbsm.fsit.mixin.server;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import dev.rvbsm.fsit.FSitMod;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ServerGamePacketListenerImpl.class)
public class ServerGamePacketListenerImplMixin_TEMPORARYFIXANNOYINGOUTOFCONTEXTBUILDLIMITMESSAGE {

    @Definition(
            id = "sendBuildLimitMessage",
            method = "Lnet/minecraft/server/level/ServerPlayer;sendBuildLimitMessage(ZI)V")
    @Expression("?.sendBuildLimitMessage(true, ?)")
    @WrapWithCondition(method = "handleUseItemOn", at = @At(value = "MIXINEXTRAS:EXPRESSION", ordinal = 3))
    private static boolean stop(ServerPlayer instance, boolean isTooHigh, int limit) {
        return !instance.getRootVehicle().entityTags().contains(FSitMod.MOD_ID);
    }
}
