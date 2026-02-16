package dev.rvbsm.fsit.api;

import dev.rvbsm.fsit.world.entity.ModPose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public interface PoseHolder {

    static void set(final Player player, final ModPose pose, final @Nullable Vec3 pos) {
        ((PoseHolder) player).fsit$setPose(pose, pos);
    }

    static void set(final Player player, final ModPose pose) {
        set(player, pose, null);
    }

    static ModPose get(final Player player) {
        return ((PoseHolder) player).fsit$getPose();
    }

    void fsit$setPose(final ModPose pose, final @Nullable Vec3 pos);

    ModPose fsit$getPose();

    default void fsit$setPose(final ModPose pose) {
        this.fsit$setPose(pose, null);
    }
}
