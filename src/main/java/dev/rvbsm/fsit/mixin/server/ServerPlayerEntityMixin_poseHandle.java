package dev.rvbsm.fsit.mixin.server;

import dev.rvbsm.fsit.api.access.Configurable;
import dev.rvbsm.fsit.config.ConfigData;
import dev.rvbsm.fsit.server.network.CrawlEntity;
import dev.rvbsm.fsit.entity.ModPose;
import dev.rvbsm.fsit.entity.SeatEntity;
import dev.rvbsm.fsit.mixin.common.PlayerEntityMixin_poseStore;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.network.packet.s2c.play.EntitiesDestroyS2CPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin_poseHandle extends PlayerEntityMixin_poseStore {

    @Shadow
    public ServerPlayNetworkHandler networkHandler;
    @Unique
    private CrawlEntity crawlEntity = null;

    private ServerPlayerEntityMixin_poseHandle(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "playerTick", at = @At("TAIL"))
    private void crawlTick(CallbackInfo ci) {
        if (this.crawlEntity != null) {
            if (this.crawlEntity.isRemoved()) {
                this.networkHandler.sendPacket(new EntitiesDestroyS2CPacket(this.crawlEntity.getId()));
                this.crawlEntity = null;
                return;
            }

            this.crawlEntity.ghostTick();

            if (this.crawlEntity.isAlive() && this.crawlEntity.isDirty()) {
                this.networkHandler.sendPacket(this.crawlEntity.createSyncPacket());
            }
        }
    }

    @Override
    protected void onPoseUpdate(ModPose pose, Vec3d pos) {
        switch (pose) {
            case STANDING -> {
                if (this.hasVehicle()) {
                    this.stopRiding();
                }

                if (this.crawlEntity != null) {
                    this.crawlEntity.discard();
                }
            }

            case SITTING -> {
                final ConfigData config = ((Configurable) this).fsit$configOrDefault();
                if (config.sitting().shouldCenter()) {
                    pos = new Vec3d(Math.floor(pos.x) + 0.5, pos.y, Math.floor(pos.z) + 0.5);
                }

                this.getEntityWorld().spawnEntity(new SeatEntity((ServerPlayerEntity) (Object) this, pos));
            }

            case CRAWLING -> {
                if (!((Configurable) this).fsit$hasConfig()) {
                    this.crawlEntity = new CrawlEntity((ServerPlayerEntity) (Object) this);
                }
            }

            default -> {
            }
        }
    }
}
