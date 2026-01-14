package dev.rvbsm.fsit.server.level;

import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.protocol.game.ClientboundBundlePacket;
import net.minecraft.network.protocol.game.ClientboundEntityPositionSyncPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Shulker;

import java.util.ArrayList;
import java.util.List;


public final class CrawlEntity extends Shulker {

    private static final float HEIGHT = 1.49f;

    private final ServerPlayer player;
    private boolean positionDirty = true;
    private boolean hidden = true;

    public CrawlEntity(final ServerPlayer player) {
        super(EntityType.SHULKER, player.level());
        this.player = player;

        this.setCustomName(Component.literal("FSit_CrawlEntity"));

        this.setPos(this.player.getBlockX(), this.player.getY() + HEIGHT, this.player.getBlockZ());
        this.entityData.set(DATA_ATTACH_FACE_ID, Direction.UP);
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
            this.setPos(this.player.getBlockX(), this.player.getY() + HEIGHT, this.player.getBlockZ());
        }

        final byte peekAmount = (byte) ((1 - distanceY % 1) * 100);
        this.entityData.set(DATA_PEEK_ID, peekAmount > 50 ? 0 : peekAmount, this.positionDirty);

    }

    public boolean isDirty() {
        return this.positionDirty || this.entityData.isDirty();
    }

    public Packet<ClientGamePacketListener> createSyncPacket() {
        final var bundle = new ArrayList<Packet<? super ClientGamePacketListener>>();

        if (this.hidden) {
            this.hidden = false;
            bundle.add(new ClientboundAddEntityPacket(this, 0, this.blockPosition()));
        }

        if (this.positionDirty) {
            bundle.add(ClientboundEntityPositionSyncPacket.of(this));
        }

        if (this.positionDirty || this.entityData.isDirty()) {
            this.positionDirty = false;

            final List<SynchedEntityData.DataValue<?>> trackedEntries = this.entityData.packDirty();
            if (trackedEntries != null) {
                bundle.add(new ClientboundSetEntityDataPacket(this.getId(), trackedEntries));
            }
        }

        return new ClientboundBundlePacket(bundle);
    }
}
