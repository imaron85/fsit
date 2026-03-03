package dev.rvbsm.fsit.api;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

/// Attach entity to the player and manually tick it.<p/>
/// Used for entities that exist only for one player
///
/// Currently used only for [dev.rvbsm.fsit.server.level.ServerCrawl]
public interface FSitAttachable {

    static void attach(ServerPlayer player, Entity entity) {
        ((FSitAttachable) player).fsit$attach(entity);
    }

    static void detach(ServerPlayer player) {
        ((FSitAttachable) player).fsit$detach();
    }

    void fsit$attach(Entity entity);

    void fsit$detach();
}
