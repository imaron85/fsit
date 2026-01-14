package dev.rvbsm.fsit.network.protocol.game;

import dev.rvbsm.fsit.FSitMod;
import dev.rvbsm.fsit.world.entity.ModPose;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

public record ServerboundPoseRequestPayload(ModPose pose, Optional<Vec3> pos) implements CustomPacketPayload {

    public static final Identifier PAYLOAD_ID = Identifier.fromNamespaceAndPath(FSitMod.MOD_ID, "pose_request_v2");
    public static final Type<ServerboundPoseRequestPayload> ID = new Type<>(PAYLOAD_ID);


    public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundPoseRequestPayload> CODEC = StreamCodec.composite(
        ModPose.PACKET_CODEC, ServerboundPoseRequestPayload::pose,
        ByteBufCodecs.optional(Vec3.STREAM_CODEC), ServerboundPoseRequestPayload::pos,

        ServerboundPoseRequestPayload::new
    );

    public static final Identifier PAYLOAD_ID_LEGACY = Identifier.fromNamespaceAndPath(FSitMod.MOD_ID, "pose_request");
    public static final Type<ServerboundPoseRequestPayload> ID_LEGACY = new Type<>(PAYLOAD_ID_LEGACY);

    public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundPoseRequestPayload> CODEC_LEGACY = StreamCodec.composite(
        ModPose.PACKET_CODEC, ServerboundPoseRequestPayload::pose,

        pose -> new ServerboundPoseRequestPayload(pose, Optional.empty())
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
