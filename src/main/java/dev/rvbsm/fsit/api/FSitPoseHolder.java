package dev.rvbsm.fsit.api;

import dev.rvbsm.fsit.world.entity.FSitPose;
import net.minecraft.world.entity.player.Player;
import org.jspecify.annotations.Nullable;

public interface FSitPoseHolder {

    static void set(Player player, FSitPose pose, FSitPose.@Nullable Context ctx, boolean emitEvent) {
        ((FSitPoseHolder) player).fsit$setPose(pose, ctx, emitEvent);
    }

    static void set(Player player, FSitPose pose, FSitPose.Context ctx) {
        set(player, pose, ctx, true);
    }

    static void set(Player player, FSitPose pose) {
        set(player, pose, null, false);
    }

    static FSitPose get(Player player) {
        return ((FSitPoseHolder) player).fsit$getPose();
    }

    void fsit$setPose(FSitPose pose, FSitPose.@Nullable Context ctx, boolean emitEvent);

    FSitPose fsit$getPose();
}
