package dev.rvbsm.fsit.network.payload.s2c;

import dev.rvbsm.fsit.FSitMod;
import dev.rvbsm.fsit.entity.ModPose;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;

public record PoseUpdateS2CPayload(ModPose pose, Vec3d pos) implements CustomPayload {

    public static final Identifier PAYLOAD_ID = Identifier.of(FSitMod.MOD_ID, "pose_sync");
    public static final Id<PoseUpdateS2CPayload> ID = new Id<>(PAYLOAD_ID);

    public static final PacketCodec<RegistryByteBuf, PoseUpdateS2CPayload> CODEC = PacketCodec.tuple(
        ModPose.PACKET_CODEC, PoseUpdateS2CPayload::pose,
        Vec3d.PACKET_CODEC, PoseUpdateS2CPayload::pos,

        PoseUpdateS2CPayload::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
