package dev.rvbsm.fsit.api.access;

import dev.rvbsm.fsit.entity.ModPose;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public interface Poseable {

    void fsit$setPose(ModPose pose, @Nullable Vec3 pos);

    ModPose fsit$getPose();

    default void fsit$setPose(ModPose pose) {
        this.fsit$setPose(pose, null);
    }
}
