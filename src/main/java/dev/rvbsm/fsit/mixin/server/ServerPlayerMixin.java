package dev.rvbsm.fsit.mixin.server;

import dev.rvbsm.fsit.FSitMod;
import dev.rvbsm.fsit.mixin.PlayerMixin;
import dev.rvbsm.fsit.world.entity.FSitPose;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin extends PlayerMixin {

    @Unique
    private int fsit$sneakTriggerTime = 0;

    @Unique
    private boolean fsit$lastShiftKeyDown = false;

    @Inject(method = "tick", at = @At("TAIL"))
    private void sneakWindow(CallbackInfo ci) {
        if (FSitMod.getInstance().handledByClient(this.getUUID())) {
            return;
        }

        if (this.fsit$getPose() == FSitPose.CRAWLING && this.wantsToStopRiding()) {
            this.fsit$setPose(FSitPose.STANDING, null, true);
            return;
        }

        if (this.fsit$sneakTriggerTime > 0) {
            this.fsit$sneakTriggerTime -= 1;
        }

        if (!this.onGround() || this.isPassenger()) {
            this.fsit$sneakTriggerTime = 0;
        }

        if (this.fsit$canStartPosingBySneaking()) {
            if (this.fsit$lastShiftKeyDown) {
                if (this.fsit$sneakTriggerTime > 0) {
                    this.fsit$startPosingBySneaking();
                } else {
                    this.fsit$sneakTriggerTime = FSitMod.getInstance().getConfig().sneakWindow();
                }
            }
        }

        this.fsit$lastShiftKeyDown = this.isShiftKeyDown();
    }
}
