package dev.rvbsm.fsit.network.payload.s2c;

import dev.rvbsm.fsit.FSitMod;
import dev.rvbsm.fsit.entity.ModPose;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.NullMarked;

@NullMarked
public record PoseUpdateS2CPayload(ModPose pose, Vec3 pos) implements CustomPacketPayload {

    public static final ResourceLocation PAYLOAD_ID = ResourceLocation.fromNamespaceAndPath(FSitMod.MOD_ID, "pose_sync");
    public static final Type<PoseUpdateS2CPayload> ID = new Type<>(PAYLOAD_ID);

    public static final StreamCodec<RegistryFriendlyByteBuf, PoseUpdateS2CPayload> CODEC = StreamCodec.composite(
        ModPose.PACKET_CODEC, PoseUpdateS2CPayload::pose,
        Vec3.STREAM_CODEC, PoseUpdateS2CPayload::pos,

        PoseUpdateS2CPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
