package dev.rvbsm.fsit.network.protocol.game;

import dev.rvbsm.fsit.FSitMod;
import dev.rvbsm.fsit.world.entity.FSitPose;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record ClientboundPlayerPosePayload(FSitPose pose) implements CustomPacketPayload {

    public static final Identifier PAYLOAD_ID = FSitMod.withDefaultNamespace("clientbound/player_pose");
    public static final Type<ClientboundPlayerPosePayload> TYPE = new Type<>(PAYLOAD_ID);
    public static final StreamCodec<ByteBuf, ClientboundPlayerPosePayload> STREAM_CODEC = StreamCodec.composite(
        FSitPose.STREAM_CODEC, ClientboundPlayerPosePayload::pose,
        ClientboundPlayerPosePayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
