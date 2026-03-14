package dev.rvbsm.fsit.mixin.client;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.sugar.Local;
import dev.rvbsm.fsit.FSitMod;
import dev.rvbsm.fsit.client.FSitModClient;
import dev.rvbsm.fsit.client.network.ClientModPacketListener;
import dev.rvbsm.fsit.network.protocol.game.ServerboundPlayerPosePayload;
import dev.rvbsm.fsit.world.entity.FSitPose;
import net.minecraft.client.player.ClientInput;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LocalPlayer.class)
public abstract class LocalPlayerMixin extends AbstractClientPlayerMixin {

    @Unique
    private int fsit$sneakTriggerTime = 0;

    @Unique
    private FSitPose fsit$lastInputPose = FSitPose.STANDING;

    @Shadow
    public abstract boolean isShiftKeyDown();

    @Shadow
    public ClientInput input;

    @Definition(id = "aiStep", method = "Lnet/minecraft/client/player/AbstractClientPlayer;aiStep()V")
    @Expression("super.aiStep()")
    @Inject(method = "aiStep", at = @At("MIXINEXTRAS:EXPRESSION"))
    private void sneakWindow(CallbackInfo ci, @Local(name = "wasShiftKeyDown") boolean wasShiftKeyDown) {
        if (!FSitModClient.getInstance().isEnabled()) {
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
            if (wasShiftKeyDown) {
                if (this.fsit$sneakTriggerTime > 0) {
                    this.fsit$startPosingBySneaking();
                } else {
                    this.fsit$sneakTriggerTime = FSitMod.getInstance().getConfig().sneakWindow();
                }
            }
        }
    }

    // fixme: dirty implementation
    @Definition(id = "aiStep", method = "Lnet/minecraft/client/player/AbstractClientPlayer;aiStep()V")
    @Expression("super.aiStep()")
    @Inject(method = "aiStep", at = @At("MIXINEXTRAS:EXPRESSION"))
    private void tickInput(CallbackInfo ci) {
        if (!FSitModClient.getInstance().isEnabled()) {
            return;
        }

        FSitModClient.getInstance().getOptions().sitKey.tick();
        FSitModClient.getInstance().getOptions().crawlKey.tick();

        if (this.isShiftKeyDown()) {
            FSitModClient.getInstance().getOptions().sitKey.reset();
            FSitModClient.getInstance().getOptions().crawlKey.reset();
        }

        var isSitDown = FSitModClient.getInstance().getOptions().sitKey.isDown();
        var isCrawlDown = FSitModClient.getInstance().getOptions().crawlKey.isDown();

        var inputPose = this.fsit$lastInputPose;
        if (isSitDown) {
            inputPose = FSitPose.SITTING;
        } else if (isCrawlDown) {
            inputPose = FSitPose.CRAWLING;
        } else {
            inputPose = FSitPose.STANDING;
        }

        if (inputPose != this.fsit$lastInputPose && (inputPose == FSitPose.STANDING || this.fsit$canStartPosing())) {
            this.fsit$setPose(inputPose, new FSitPose.Context.Positioned(this.position()), true);
        }

        this.fsit$lastInputPose = inputPose;
    }

    @Override
    protected boolean fsit$canStartPosingBySneaking() {
        return this.input.getMoveVector().lengthSquared() < 1.0e-5 && super.fsit$canStartPosingBySneaking();
    }

    @Override
    protected void fsit$sendPose(Vec3 position) {
        ClientModPacketListener.send(
                ServerboundPlayerPosePayload.TYPE, () -> new ServerboundPlayerPosePayload(this.fsit$pose, position));
    }
}
