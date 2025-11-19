package dev.rvbsm.fsit.network.payload.s2c;

import dev.rvbsm.fsit.FSitMod;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jspecify.annotations.NullMarked;

import java.util.UUID;

@NullMarked
public record RidingRequestS2CPayload(UUID uuid) implements CustomPacketPayload {

    public static final ResourceLocation PAYLOAD_ID = ResourceLocation.fromNamespaceAndPath(FSitMod.MOD_ID, "riding_request");
    public static final Type<RidingRequestS2CPayload> ID = new Type<>(PAYLOAD_ID);

    public static final StreamCodec<RegistryFriendlyByteBuf, RidingRequestS2CPayload> CODEC = StreamCodec.composite(
        UUIDUtil.STREAM_CODEC, RidingRequestS2CPayload::uuid,

        RidingRequestS2CPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
