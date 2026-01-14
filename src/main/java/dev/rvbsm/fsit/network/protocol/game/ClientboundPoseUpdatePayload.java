package dev.rvbsm.fsit.network.protocol.game;

import dev.rvbsm.fsit.FSitMod;
import dev.rvbsm.fsit.world.entity.ModPose;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;

public record ClientboundPoseUpdatePayload(ModPose pose, Vec3 pos) implements CustomPacketPayload {

    public static final Identifier PAYLOAD_ID = Identifier.fromNamespaceAndPath(FSitMod.MOD_ID, "pose_sync");
    public static final Type<ClientboundPoseUpdatePayload> ID = new Type<>(PAYLOAD_ID);

    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundPoseUpdatePayload> CODEC = StreamCodec.composite(
        ModPose.PACKET_CODEC, ClientboundPoseUpdatePayload::pose,
        Vec3.STREAM_CODEC, ClientboundPoseUpdatePayload::pos,

        ClientboundPoseUpdatePayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
