package dev.rvbsm.fsit.network.payload.c2s;

import dev.rvbsm.fsit.FSitMod;
import dev.rvbsm.fsit.entity.ModPose;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.NullMarked;

import java.util.Optional;

@NullMarked
public record PoseRequestC2SPayload(ModPose pose, Optional<Vec3> pos) implements CustomPacketPayload {

    public static final ResourceLocation PAYLOAD_ID = ResourceLocation.fromNamespaceAndPath(FSitMod.MOD_ID, "pose_request_v2");
    public static final Type<PoseRequestC2SPayload> ID = new Type<>(PAYLOAD_ID);


    public static final StreamCodec<RegistryFriendlyByteBuf, PoseRequestC2SPayload> CODEC = StreamCodec.composite(
        ModPose.PACKET_CODEC, PoseRequestC2SPayload::pose,
        ByteBufCodecs.optional(Vec3.STREAM_CODEC), PoseRequestC2SPayload::pos,

        PoseRequestC2SPayload::new
    );

    public static final ResourceLocation PAYLOAD_ID_LEGACY = ResourceLocation.fromNamespaceAndPath(FSitMod.MOD_ID, "pose_request");
    public static final Type<PoseRequestC2SPayload> ID_LEGACY = new Type<>(PAYLOAD_ID_LEGACY);

    public static final StreamCodec<RegistryFriendlyByteBuf, PoseRequestC2SPayload> CODEC_LEGACY = StreamCodec.composite(
        ModPose.PACKET_CODEC, PoseRequestC2SPayload::pose,

        pose -> new PoseRequestC2SPayload(pose, Optional.empty())
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
