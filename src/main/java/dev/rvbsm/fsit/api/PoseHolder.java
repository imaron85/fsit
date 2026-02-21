package dev.rvbsm.fsit.api;

import dev.rvbsm.fsit.world.entity.ModPose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public interface PoseHolder {

    static void set(Player player, ModPose pose, @Nullable Vec3 pos) {
        ((PoseHolder) player).fsit$setPose(pose, pos);
    }

    static void set(Player player, ModPose pose) {
        set(player, pose, null);
    }

    static ModPose get(Player player) {
        return ((PoseHolder) player).fsit$getPose();
    }

    void fsit$setPose(ModPose pose, @Nullable Vec3 pos);

    ModPose fsit$getPose();
}
