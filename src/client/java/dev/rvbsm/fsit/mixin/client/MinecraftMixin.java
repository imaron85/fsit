package dev.rvbsm.fsit.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.rvbsm.fsit.api.FSitPoseHolder;
import dev.rvbsm.fsit.world.entity.FSitPose;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

// skip the bed chat screen while LAYING; normal bed sleep untouched (fsit$pose != LAYING)

//$ if ~26.1 '@Mixin(net.minecraft.client.Minecraft.class)' else '@Mixin(net.minecraft.client.gui.Gui.class)'
@Mixin(net.minecraft.client.Minecraft.class)
public abstract class MinecraftMixin {

    @WrapOperation(
            method = "tick",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;isSleeping()Z"))
    private boolean fsit$treatLayingAsAwakeForScreen(LocalPlayer instance, Operation<Boolean> original) {
        return FSitPoseHolder.get(instance) != FSitPose.LAYING && original.call(instance);
    }
}
