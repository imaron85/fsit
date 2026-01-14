package dev.rvbsm.fsit.mixin.client;

import dev.rvbsm.fsit.client.network.ClientModNetworkListener;
import dev.rvbsm.fsit.mixin.common.PlayerMixin_poseStore;
import dev.rvbsm.fsit.network.protocol.game.ServerboundPoseRequestPayload;
import dev.rvbsm.fsit.world.entity.ModPose;
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
    protected void onPoseUpdate(final ModPose pose, final Vec3 pos) {
        ClientModNetworkListener.trySend(
            ServerboundPoseRequestPayload.ID, () -> new ServerboundPoseRequestPayload(pose, Optional.of(pos)),
            () -> {}
        );
    }
}
