package dev.rvbsm.fsit.network.protocol.game;

import dev.rvbsm.fsit.FSitMod;
import dev.rvbsm.fsit.world.entity.FSitPose;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;

public record ServerboundPlayerPosePayload(FSitPose pose, Vec3 position) implements CustomPacketPayload {

    public static final Identifier PAYLOAD_ID = FSitMod.withDefaultNamespace("serverbound/player_pose");
    public static final Type<ServerboundPlayerPosePayload> TYPE = new Type<>(PAYLOAD_ID);
    public static final StreamCodec<ByteBuf, ServerboundPlayerPosePayload> STREAM_CODEC = StreamCodec.composite(
        FSitPose.STREAM_CODEC, ServerboundPlayerPosePayload::pose,
        Vec3.STREAM_CODEC, ServerboundPlayerPosePayload::position,
        ServerboundPlayerPosePayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
