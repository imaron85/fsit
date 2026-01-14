package dev.rvbsm.fsit.network.protocol.game;

import dev.rvbsm.fsit.FSitMod;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import java.util.UUID;

public record ServerboundRidingResponsePayload(UUID uuid, boolean isAccepted) implements CustomPacketPayload {

    public static final Identifier PAYLOAD_ID = Identifier.fromNamespaceAndPath(FSitMod.MOD_ID, "riding_response");
    public static final Type<ServerboundRidingResponsePayload> ID = new Type<>(PAYLOAD_ID);

    public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundRidingResponsePayload> CODEC = StreamCodec.composite(
        UUIDUtil.STREAM_CODEC, ServerboundRidingResponsePayload::uuid,
        ByteBufCodecs.BOOL, ServerboundRidingResponsePayload::isAccepted, // was var-int 0-1

        ServerboundRidingResponsePayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
