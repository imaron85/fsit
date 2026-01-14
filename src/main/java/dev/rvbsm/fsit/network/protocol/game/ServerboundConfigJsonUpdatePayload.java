package dev.rvbsm.fsit.network.protocol.game;

import dev.rvbsm.fsit.FSitMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record ServerboundConfigJsonUpdatePayload(String configJson) implements CustomPacketPayload {

    public static final Identifier PAYLOAD_ID = Identifier.fromNamespaceAndPath(FSitMod.MOD_ID, "config_sync");
    public static final Type<ServerboundConfigJsonUpdatePayload> TYPE = new Type<>(PAYLOAD_ID);

    public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundConfigJsonUpdatePayload> CODEC = StreamCodec.composite(
        ByteBufCodecs.STRING_UTF8, ServerboundConfigJsonUpdatePayload::configJson,

        ServerboundConfigJsonUpdatePayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
