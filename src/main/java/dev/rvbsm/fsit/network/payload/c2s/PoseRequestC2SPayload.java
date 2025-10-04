package dev.rvbsm.fsit.network.payload.c2s;

import dev.rvbsm.fsit.FSitMod;
import dev.rvbsm.fsit.entity.ModPose;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;

import java.util.Optional;

public record PoseRequestC2SPayload(ModPose pose, Optional<Vec3d> pos) implements CustomPayload {

    public static final Identifier PAYLOAD_ID = Identifier.of(FSitMod.MOD_ID, "pose_request_v2");
    public static final Id<PoseRequestC2SPayload> ID = new Id<>(PAYLOAD_ID);


    public static final PacketCodec<RegistryByteBuf, PoseRequestC2SPayload> CODEC = PacketCodec.tuple(
        ModPose.PACKET_CODEC, PoseRequestC2SPayload::pose,
        PacketCodecs.optional(Vec3d.PACKET_CODEC), PoseRequestC2SPayload::pos,

        PoseRequestC2SPayload::new
    );

    public static final Identifier PAYLOAD_ID_LEGACY = Identifier.of(FSitMod.MOD_ID, "pose_request");
    public static final Id<PoseRequestC2SPayload> ID_LEGACY = new Id<>(PAYLOAD_ID_LEGACY);

    public static final PacketCodec<RegistryByteBuf, PoseRequestC2SPayload> CODEC_LEGACY = PacketCodec.tuple(
        ModPose.PACKET_CODEC, PoseRequestC2SPayload::pose,

        pose -> new PoseRequestC2SPayload(pose, Optional.empty())
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
