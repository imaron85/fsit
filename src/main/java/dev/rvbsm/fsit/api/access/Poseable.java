package dev.rvbsm.fsit.api.access;

import dev.rvbsm.fsit.world.entity.ModPose;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public interface Poseable {

    void fsit$setPose(final ModPose pose, final @Nullable Vec3 pos);

    ModPose fsit$getPose();

    default void fsit$setPose(final ModPose pose) {
        this.fsit$setPose(pose, null);
    }
}
