package dev.rvbsm.fsit.mixin.client;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.sugar.Local;
import dev.rvbsm.fsit.FSitMod;
import dev.rvbsm.fsit.client.FSitModClient;
import dev.rvbsm.fsit.world.entity.ModPose;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LocalPlayer.class)
public abstract class LocalPlayerMixin extends AbstractClientPlayerMixin {

    @Unique
    private int sneakTriggerTime = 0;

    @Unique
    private ModPose lastInputPose = ModPose.STANDING;

    @Shadow
    public abstract boolean isShiftKeyDown();

    @Definition(id = "aiStep", method = "Lnet/minecraft/client/player/AbstractClientPlayer;aiStep()V")
    @Expression("super.aiStep()")
    @Inject(method = "aiStep", at = @At("MIXINEXTRAS:EXPRESSION"))
    private void sneakWindow(CallbackInfo ci, @Local(name = "wasShiftKeyDown") boolean wasShiftKeyDown) {
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
            if (wasShiftKeyDown) {
                if (this.sneakTriggerTime > 0) {
                    this.startPosingBySneaking();
                } else {
                    this.sneakTriggerTime = FSitMod.getInstance().getConfig().sneakWindow();
                }
            }
        }
    }

    // fixme: dirty implementation
    @Definition(id = "aiStep", method = "Lnet/minecraft/client/player/AbstractClientPlayer;aiStep()V")
    @Expression("super.aiStep()")
    @Inject(method = "aiStep", at = @At("MIXINEXTRAS:EXPRESSION"))
    private void tickInput(CallbackInfo ci) {
        FSitModClient.getInstance().getOptions().sitKey.tick();
        FSitModClient.getInstance().getOptions().crawlKey.tick();

        if (this.isShiftKeyDown()) {
            FSitModClient.getInstance().getOptions().sitKey.reset();
            FSitModClient.getInstance().getOptions().crawlKey.reset();
        }

        var isSitDown = FSitModClient.getInstance().getOptions().sitKey.isDown();
        var isCrawlDown = FSitModClient.getInstance().getOptions().crawlKey.isDown();

        var inputPose = this.lastInputPose;
        if (isSitDown) {
            inputPose = ModPose.SITTING;
        } else if (isCrawlDown) {
            inputPose = ModPose.CRAWLING;
        } else {
            inputPose = ModPose.STANDING;
        }

        if (inputPose != this.lastInputPose && (inputPose == ModPose.STANDING || this.canStartPosing())) {
            this.fsit$setPose(inputPose, this.position());
        }

        this.lastInputPose = inputPose;
    }
}
