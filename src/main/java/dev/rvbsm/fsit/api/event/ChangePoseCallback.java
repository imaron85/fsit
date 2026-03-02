package dev.rvbsm.fsit.api.event;

import dev.rvbsm.fsit.world.entity.FSitPose;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.world.entity.player.Player;
import org.jspecify.annotations.Nullable;

@FunctionalInterface
public interface ChangePoseCallback {

    Event<ChangePoseCallback> EVENT =
            EventFactory.createArrayBacked(
                ChangePoseCallback.class, listeners -> (player, pose, pos) -> {
                for (var listener : listeners) {
                    var newPose = listener.changePose(player, pose, pos);
                    if (newPose != null) {
                        return newPose;
                    }
                }

                return null;
            });

    @Nullable
    FSitPose changePose(Player player, FSitPose pose, FSitPose.@Nullable Context ctx);
}
