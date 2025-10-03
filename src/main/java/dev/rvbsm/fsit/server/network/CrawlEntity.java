//~ entity_position_sync_packet_import
//~ entity_position_sync_packet_new

package dev.rvbsm.fsit.server.network;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.mob.ShulkerEntity;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BundleS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityPositionSyncS2CPacket;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityTrackerUpdateS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.Direction;

import java.util.ArrayList;
import java.util.List;

public final class CrawlEntity extends ShulkerEntity {

    private static final float HEIGHT = 1.49f;

    private final ServerPlayerEntity player;
    private boolean positionDirty = true;
    private boolean hidden = true;

    public CrawlEntity(ServerPlayerEntity player) {
        super(EntityType.SHULKER, player.getEntityWorld());
        this.player = player;

        this.setCustomName(Text.literal("FSit_CrawlEntity"));

        this.setPosition(this.player.getBlockX(), this.player.getY() + HEIGHT, this.player.getBlockZ());
        this.dataTracker.set(ATTACHED_FACE, Direction.UP);
        this.setInvisible(true);
    }

    // todo: predict next position if player moves fast
    public void ghostTick() {
        final int blockDistanceX = Math.abs(this.getBlockX() - this.player.getBlockX());
        final int blockDistanceZ = Math.abs(this.getBlockZ() - this.player.getBlockZ());
        final int blockDistance = blockDistanceX + blockDistanceZ;
        final double distanceY = Math.abs(this.getY() - this.player.getY());

        if (blockDistance >= 1 || distanceY >= 2) {
            this.positionDirty = true;
            this.setPosition(this.player.getBlockX(), this.player.getY() + HEIGHT, this.player.getBlockZ());
        }

        final byte peekAmount = (byte) ((1 - distanceY % 1) * 100);
        this.dataTracker.set(PEEK_AMOUNT, peekAmount > 50 ? 0 : peekAmount, this.positionDirty);

    }

    public boolean isDirty() {
        return this.positionDirty || this.dataTracker.isDirty();
    }

    public Packet<ClientPlayPacketListener> createSyncPacket() {
        final var bundle = new ArrayList<Packet<? super ClientPlayPacketListener>>();

        if (this.hidden) {
            this.hidden = false;
            bundle.add(new EntitySpawnS2CPacket(this, 0, this.getBlockPos()));
        }

        if (this.positionDirty) {
            bundle.add(EntityPositionSyncS2CPacket.create(this));
        }

        if (this.positionDirty || this.dataTracker.isDirty()) {
            this.positionDirty = false;

            final List<DataTracker.SerializedEntry<?>> trackedEntries = this.dataTracker.getDirtyEntries();
            if (trackedEntries != null) {
                bundle.add(new EntityTrackerUpdateS2CPacket(this.getId(), trackedEntries));
            }
        }

        return new BundleS2CPacket(bundle);
    }
}
