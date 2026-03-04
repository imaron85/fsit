package dev.rvbsm.fsit.server.level;

import com.mojang.logging.LogUtils;
import dev.rvbsm.fsit.api.FSitPoseHolder;
import dev.rvbsm.fsit.world.entity.FSitPose;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.protocol.game.ClientboundBundlePacket;
import net.minecraft.network.protocol.game.ClientboundEntityPositionSyncPacket;
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Shulker;
import org.slf4j.Logger;

import java.util.ArrayList;

public class ServerCrawl extends Shulker {

    private static final Logger LOGGER = LogUtils.getLogger();
    private final ServerPlayer player;

    public ServerCrawl(ServerPlayer player) {
        super(EntityType.SHULKER, player.level());
        this.player = player;
        this.setInvisible(true);
        this.entityData.set(DATA_ATTACH_FACE_ID, Direction.UP);
        this.refreshPosition();

        // to prevent accidentally adding it to the level
        this.remove(RemovalReason.DISCARDED);
    }

    @Override
    public void tick() {
        if (this.needsRefresh() || this.tickCount % 60 == 0) {
            this.refreshPosition();

            var bundle = new ArrayList<Packet<? super ClientGamePacketListener>>();
            if (this.firstTick) {
                bundle.add(new ClientboundAddEntityPacket(this, 0, this.blockPosition()));
            }

            // client interpolates it anyway, i think there is no need to use move packets
            bundle.add(ClientboundEntityPositionSyncPacket.of(this));
            // syncing only dirty resets entity data on the client???
            var trackedDataValues = this.entityData.getNonDefaultValues();
            if (trackedDataValues != null) {
                bundle.add(new ClientboundSetEntityDataPacket(this.getId(), trackedDataValues));
            }

            this.player.connection.send(new ClientboundBundlePacket(bundle));
        }

        this.firstTick = false;
        this.tickCount += 1;
    }

    private boolean needsRefresh() {
        return this.getBlockX() != this.player.getBlockX()
            || this.getBlockZ() != this.player.getBlockZ()
            || this.getY() - this.player.getY() > 2
            || this.player.getKnownMovement().y != 0.0;
    }

    private void refreshPosition() {
        // todo: respect player scaling
        this.setPos(this.player.getX(), this.player.getY() + 2.0, this.player.getZ());

        double y = this.player.getY() - 0.51;
        var peek = (byte) (100 - Math.round((y - Math.floor(y)) * 100));
        this.entityData.set(DATA_PEEK_ID, peek);
        LOGGER.debug("move it move it {} {} {}", this.player.position(), this.position(), peek);
    }

    @Override
    public void remove(RemovalReason reason) {
        super.remove(reason);
        if (!this.firstTick) {
            this.player.connection.send(new ClientboundRemoveEntitiesPacket(this.getId()));
            // no need to send the event since this entity is created only for players without client mod
            FSitPoseHolder.set(this.player, FSitPose.STANDING);
        }
    }
}
