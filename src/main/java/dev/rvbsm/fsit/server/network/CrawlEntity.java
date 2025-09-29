package dev.rvbsm.fsit.server.network;

import dev.rvbsm.fsit.entity.PoseEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BundleS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityPositionSyncS2CPacket;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityTrackerUpdateS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.HashSet;
import java.util.List;

public final class CrawlEntity extends PoseEntity {

    private boolean dirty = true;

    public CrawlEntity(ServerPlayerEntity player) {
        super(EntityType.SHULKER, player);
        this.setCustomName(Text.literal("FSit_CrawlEntity"));

        this.setInvisible(true);
    }

    public void ghostTick() {
        // todo
        if (this.distanceTo(this.player) > 2) {
            this.setPosition(this.player.getEntityPos().add(0.0, 1.5, 0.0));
            this.dirty = true;
        }
    }

    public boolean isDirty() {
        return this.dirty;
    }

    public Packet<ClientPlayPacketListener> createSyncPacket() {
        this.dirty = false;

        final var bundle = new HashSet<Packet<? super ClientPlayPacketListener>>();

        bundle.add(new EntitySpawnS2CPacket(this, 0, this.getBlockPos()));
        bundle.add(EntityPositionSyncS2CPacket.create(this));

        final List<DataTracker.SerializedEntry<?>> changedEntries = this.dataTracker.getChangedEntries();
        if (changedEntries != null) {
            bundle.add(new EntityTrackerUpdateS2CPacket(this.getId(), changedEntries));
        }

        return new BundleS2CPacket(bundle);
    }
}
