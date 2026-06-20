package dev.rvbsm.fsit.mixin.client;

import dev.rvbsm.fsit.api.FSitPoseHolder;
import dev.rvbsm.fsit.world.entity.FSitPose;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

// skip the bed chat screen while LAYING; normal bed sleep untouched (fsit$pose != LAYING)

//? if 26.1 {
@Mixin(Minecraft.class)
//?} else {
/*@Mixin(Gui.class)
*///?}
public abstract class MinecraftMixin {

    @Redirect(
            method = "tick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/player/LocalPlayer;isSleeping()Z"))
    private boolean fsit$treatLayingAsAwakeForScreen(LocalPlayer player) {
        if (FSitPoseHolder.get(player) == FSitPose.LAYING) {
            return false;
        }
        return player.isSleeping();
    }
}
