package dev.rvbsm.fsit.mixin.client;

import dev.rvbsm.fsit.client.network.ClientModNetworkHandler;
import dev.rvbsm.fsit.entity.ModPose;
import dev.rvbsm.fsit.mixin.common.PlayerEntityMixin_poseStore;
import dev.rvbsm.fsit.network.payload.c2s.PoseRequestC2SPayload;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;

import java.util.Optional;

@Mixin(ClientPlayerEntity.class)
public abstract class ClientPlayerEntityMixin_poseHandle extends PlayerEntityMixin_poseStore {

    private ClientPlayerEntityMixin_poseHandle(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    @Override
    protected void onPoseUpdate(ModPose pose, Vec3d pos) {
        ClientModNetworkHandler.trySend(
            PoseRequestC2SPayload.ID, () -> new PoseRequestC2SPayload(pose, Optional.of(pos)),
            () -> {}
        );
    }
}
