package dev.rvbsm.fsit.api.event;

import dev.rvbsm.fsit.world.entity.ModPose;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
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
    ModPose changePose(Player player, ModPose pose, Vec3 pos);
}
