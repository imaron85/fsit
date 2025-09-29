package dev.rvbsm.fsit.network.payload.c2s;

import dev.rvbsm.fsit.FSitMod;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record ConfigUpdateJsonC2SPayload(String configJson) implements CustomPayload {

    public static final Identifier PAYLOAD_ID = Identifier.of(FSitMod.MOD_ID, "config_sync");
    public static final Id<ConfigUpdateJsonC2SPayload> ID = new Id<>(PAYLOAD_ID);

    public static final PacketCodec<RegistryByteBuf, ConfigUpdateJsonC2SPayload> CODEC = PacketCodec.tuple(
        PacketCodecs.STRING, ConfigUpdateJsonC2SPayload::configJson,

        ConfigUpdateJsonC2SPayload::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
