package dev.rvbsm.fsit.network.payload.s2c;

import dev.rvbsm.fsit.FSitMod;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.minecraft.util.Uuids;

import java.util.UUID;

public record RidingRequestS2CPayload(UUID uuid) implements CustomPayload {

    public static final Identifier PAYLOAD_ID = Identifier.of(FSitMod.MOD_ID, "riding_request");
    public static final Id<RidingRequestS2CPayload> ID = new Id<>(PAYLOAD_ID);

    public static final PacketCodec<RegistryByteBuf, RidingRequestS2CPayload> CODEC = PacketCodec.tuple(
        Uuids.PACKET_CODEC, RidingRequestS2CPayload::uuid,

        RidingRequestS2CPayload::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
