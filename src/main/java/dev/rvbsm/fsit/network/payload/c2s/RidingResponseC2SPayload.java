//~ packet_codec_boolean

package dev.rvbsm.fsit.network.payload.c2s;

import dev.rvbsm.fsit.FSitMod;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jspecify.annotations.NullMarked;

import java.util.UUID;

@NullMarked
public record RidingResponseC2SPayload(UUID uuid, boolean isAccepted) implements CustomPacketPayload {

    public static final ResourceLocation PAYLOAD_ID = ResourceLocation.fromNamespaceAndPath(FSitMod.MOD_ID, "riding_response");
    public static final Type<RidingResponseC2SPayload> ID = new Type<>(PAYLOAD_ID);

    public static final StreamCodec<RegistryFriendlyByteBuf, RidingResponseC2SPayload> CODEC = StreamCodec.composite(
        UUIDUtil.STREAM_CODEC, RidingResponseC2SPayload::uuid,
        ByteBufCodecs.BOOL, RidingResponseC2SPayload::isAccepted, // was var-int 0-1

        RidingResponseC2SPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
