package dev.rvbsm.fsit.network.protocol.game;

import dev.rvbsm.fsit.FSitMod;
import dev.rvbsm.fsit.world.entity.ModPose;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;

public record ClientboundChangePosePayload(ModPose pose, Vec3 pos) implements CustomPacketPayload {

    public static final Identifier PAYLOAD_ID = Identifier.fromNamespaceAndPath(FSitMod.MOD_ID, "change_pose");
    public static final Type<ClientboundChangePosePayload> TYPE = new Type<>(PAYLOAD_ID);

    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundChangePosePayload> CODEC = StreamCodec.composite(
        ModPose.STREAM_CODEC, ClientboundChangePosePayload::pose,
        Vec3.STREAM_CODEC, ClientboundChangePosePayload::pos,

        ClientboundChangePosePayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
