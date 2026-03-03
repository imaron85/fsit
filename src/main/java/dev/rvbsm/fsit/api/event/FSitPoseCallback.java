package dev.rvbsm.fsit.api.event;

import dev.rvbsm.fsit.world.entity.FSitPose;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

@FunctionalInterface
public interface FSitPoseCallback {

    Event<FSitPoseCallback> EVENT =
            EventFactory.createArrayBacked(FSitPoseCallback.class, listeners -> (player, pose, pos) -> {
                for (var listener : listeners) {
                    var newPose = listener.createPose(player, pose, pos);
                    if (newPose != null) {
                        return newPose;
                    }
                }

                return null;
            });

    @Nullable
    Vec3 createPose(Player player, FSitPose pose, FSitPose.@Nullable Context ctx);
}
