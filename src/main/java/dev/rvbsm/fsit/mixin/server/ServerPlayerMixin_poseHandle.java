package dev.rvbsm.fsit.mixin.server;

import dev.rvbsm.fsit.api.access.Configurable;
import dev.rvbsm.fsit.config.ConfigData;
import dev.rvbsm.fsit.entity.ModPose;
import dev.rvbsm.fsit.entity.SeatEntity;
import dev.rvbsm.fsit.mixin.common.PlayerMixin_poseStore;
import dev.rvbsm.fsit.network.payload.s2c.PoseUpdateS2CPayload;
import dev.rvbsm.fsit.server.network.CrawlEntity;
import dev.rvbsm.fsit.server.network.ServerModNetworkHandler;
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin_poseHandle extends PlayerMixin_poseStore implements Configurable {

    @Shadow
    public ServerGamePacketListenerImpl connection;
    @Unique
    private CrawlEntity crawlEntity = null;

    private ServerPlayerMixin_poseHandle(EntityType<? extends LivingEntity> entityType, Level world) {
        super(entityType, world);
    }

    @Inject(method = "doTick", at = @At("TAIL"))
    private void crawlTick(CallbackInfo ci) {
        if (this.crawlEntity != null) {
            if (this.crawlEntity.isRemoved()) {
                this.connection.send(new ClientboundRemoveEntitiesPacket(this.crawlEntity.getId()));
                this.crawlEntity = null;
                return;
            }

            this.crawlEntity.ghostTick();

            if (this.crawlEntity.isAlive() && this.crawlEntity.isDirty()) {
                this.connection.send(this.crawlEntity.createSyncPacket());
            }
        }
    }

    @Override
    protected void onPoseUpdate(ModPose pose, Vec3 pos) {
        switch (pose) {
            case STANDING -> {
                if (this.isPassenger()) {
                    this.stopRiding();
                }

                if (this.crawlEntity != null) {
                    this.crawlEntity.discard();
                }
            }

            case SITTING -> {
                final ConfigData config = this.fsit$configOrDefault();
                if (config.sitting().shouldCenter()) {
                    pos = new Vec3(Math.floor(pos.x) + 0.5, pos.y, Math.floor(pos.z) + 0.5);
                }

                this.level().addFreshEntity(new SeatEntity((ServerPlayer) (Object) this, pos));
            }

            case CRAWLING -> {
                if (!this.fsit$hasConfig()) {
                    this.crawlEntity = new CrawlEntity((ServerPlayer) (Object) this);
                }
            }

            default -> {
                return;
            }
        }

        final Vec3 finalPos = pos;
        ServerModNetworkHandler.trySend(
            (ServerPlayer) (Object) this,
            PoseUpdateS2CPayload.ID, () -> new PoseUpdateS2CPayload(pose, finalPos),
            () -> {}
        );
    }
}
