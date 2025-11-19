package dev.rvbsm.fsit.network.payload.c2s;

import dev.rvbsm.fsit.FSitMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jspecify.annotations.NullMarked;

@NullMarked
public record ConfigUpdateJsonC2SPayload(String configJson) implements CustomPacketPayload {

    public static final ResourceLocation PAYLOAD_ID = ResourceLocation.fromNamespaceAndPath(FSitMod.MOD_ID, "config_sync");
    public static final Type<ConfigUpdateJsonC2SPayload> ID = new Type<>(PAYLOAD_ID);

    public static final StreamCodec<RegistryFriendlyByteBuf, ConfigUpdateJsonC2SPayload> CODEC = StreamCodec.composite(
        ByteBufCodecs.STRING_UTF8, ConfigUpdateJsonC2SPayload::configJson,

        ConfigUpdateJsonC2SPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
