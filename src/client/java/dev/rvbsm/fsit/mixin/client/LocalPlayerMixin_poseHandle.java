package dev.rvbsm.fsit.mixin.client;

import dev.rvbsm.fsit.client.network.ClientModNetworkHandler;
import dev.rvbsm.fsit.entity.ModPose;
import dev.rvbsm.fsit.mixin.common.PlayerMixin_poseStore;
import dev.rvbsm.fsit.network.payload.c2s.PoseRequestC2SPayload;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;

import java.util.Optional;

@Mixin(LocalPlayer.class)
public abstract class LocalPlayerMixin_poseHandle extends PlayerMixin_poseStore {

    private LocalPlayerMixin_poseHandle(EntityType<? extends LivingEntity> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    protected void onPoseUpdate(ModPose pose, Vec3 pos) {
        ClientModNetworkHandler.trySend(
            PoseRequestC2SPayload.ID, () -> new PoseRequestC2SPayload(pose, Optional.of(pos)),
            () -> {}
        );
    }
}
