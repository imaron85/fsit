//~ packet_codec_boolean

package dev.rvbsm.fsit.network.payload.c2s;

import dev.rvbsm.fsit.FSitMod;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.minecraft.util.Uuids;

import java.util.UUID;

public record RidingResponseC2SPayload(UUID uuid, boolean isAccepted) implements CustomPayload {

    public static final Identifier PAYLOAD_ID = Identifier.of(FSitMod.MOD_ID, "riding_response");
    public static final Id<RidingResponseC2SPayload> ID = new Id<>(PAYLOAD_ID);

    public static final PacketCodec<RegistryByteBuf, RidingResponseC2SPayload> CODEC = PacketCodec.tuple(
        Uuids.PACKET_CODEC, RidingResponseC2SPayload::uuid,
        PacketCodecs.BOOLEAN, RidingResponseC2SPayload::isAccepted, // was var-int 0-1

        RidingResponseC2SPayload::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
