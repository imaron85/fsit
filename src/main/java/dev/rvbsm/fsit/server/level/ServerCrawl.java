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
//        this.setInvisible(true);
        this.entityData.set(DATA_ATTACH_FACE_ID, Direction.UP);

        // to prevent accidentally adding it to the level
        this.remove(RemovalReason.DISCARDED);
    }

    @Override
    public void tick() {
        boolean needsUpdate =
               this.getBlockX() != player.getBlockX()
            || this.getBlockZ() != player.getBlockZ()
            || this.getY() - player.getY() > 2.5;

        if (needsUpdate || this.tickCount % 60 == 0) {
            // todo: respect player scaling
            var newPosition = this.player.position().add(0.0, 1.5, 0.0);
            this.setPos(newPosition);

            // fixme: gap is too big, should be <1.5 to force crawl
            double py = player.getY() - 0.51;
            var peekAmount = (byte) (100 - (py - Math.floor(py)) * 100);
            this.entityData.set(DATA_PEEK_ID, peekAmount);

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

    @Override
    public void remove(RemovalReason reason) {
        super.remove(reason);
        if (!this.firstTick) {
            FSitPoseHolder.set(player, FSitPose.STANDING); // no need to send the event
        }
    }
}
