package dev.rvbsm.fsit.config;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.StringRepresentable;

import java.util.function.IntFunction;

/// Controls how the local client behaves when its player is sitting on another player and
/// a teleport (notably across dimensions, via portals or ender pearls) is about to happen.
public enum RidingTeleportMode implements StringRepresentable {
    /// Vanilla — let the teleport happen as-is. May leave dangling Ride entities or crash
    /// the client mid-respawn.
    VANILLA(0, "vanilla"),
    /// Refuse client-initiated teleports while sitting on a player. Server-issued `/tp` is
    /// not affected — the server still has authority.
    DISABLED(1, "disabled"),
    /// Cleanly dismount the local player from the Ride before a cross-dimension respawn so
    /// no stale passenger/vehicle state survives the level swap.
    ENHANCED(2, "enhanced");

    public static final EnumCodec<RidingTeleportMode> CODEC = StringRepresentable.fromEnum(RidingTeleportMode::values);
    private static final IntFunction<RidingTeleportMode> BY_ID = ByIdMap.continuous(RidingTeleportMode::id, values(), ByIdMap.OutOfBoundsStrategy.ZERO);
    public static final StreamCodec<ByteBuf, RidingTeleportMode> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, RidingTeleportMode::id);

    private final int id;
    private final String name;

    RidingTeleportMode(int id, String name) {
        this.id = id;
        this.name = name;
    }

    private int id() {
        return this.id;
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }
}
