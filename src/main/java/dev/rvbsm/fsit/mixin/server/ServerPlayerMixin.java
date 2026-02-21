package dev.rvbsm.fsit.mixin.server;

import dev.rvbsm.fsit.FSitMod;
import dev.rvbsm.fsit.mixin.PlayerMixin;
import dev.rvbsm.fsit.world.entity.ModPose;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin extends PlayerMixin {

    @Unique
    private int sneakTriggerTime = 0;

    @Unique
    private boolean lastShiftKeyDown = false;

    @Inject(method = "tick", at = @At("TAIL"))
    private void sneakWindow(CallbackInfo ci) {
        if (FSitMod.getInstance().handledByClient(this.getUUID())) {
            return;
        }

        if (this.fsit$getPose() == ModPose.CRAWLING && this.wantsToStopRiding()) {
            this.fsit$setPose(ModPose.STANDING, this.position());
            return;
        }

        if (this.sneakTriggerTime > 0) {
            this.sneakTriggerTime -= 1;
        }

        if (!this.onGround() || this.isPassenger()) {
            this.sneakTriggerTime = 0;
        }

        if (this.canStartPosingBySneaking()) {
            if (this.lastShiftKeyDown) {
                if (this.sneakTriggerTime > 0) {
                    this.startPosingBySneaking();
                } else {
                    this.sneakTriggerTime = FSitMod.getInstance().getConfig().sneakWindow();
                }
            }
        }

        this.lastShiftKeyDown = this.isShiftKeyDown();
    }
}
