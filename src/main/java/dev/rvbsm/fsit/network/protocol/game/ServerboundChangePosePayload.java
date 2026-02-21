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

public record ServerboundChangePosePayload(ModPose pose, Optional<Vec3> pos) implements CustomPacketPayload {

    public static final Identifier PAYLOAD_ID = Identifier.fromNamespaceAndPath(FSitMod.MOD_ID, "change_pose");
    public static final Type<ServerboundChangePosePayload> TYPE = new Type<>(PAYLOAD_ID);

    public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundChangePosePayload> CODEC = StreamCodec.composite(
        ModPose.STREAM_CODEC, ServerboundChangePosePayload::pose,
        ByteBufCodecs.optional(Vec3.STREAM_CODEC), ServerboundChangePosePayload::pos,

        ServerboundChangePosePayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
