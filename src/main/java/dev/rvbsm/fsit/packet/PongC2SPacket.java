package dev.rvbsm.fsit.packet;

import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

public record PongC2SPacket() implements FabricPacket {

	public static final PacketType<PongC2SPacket> TYPE = PacketType.create(new Identifier("fsit", "pong"), PongC2SPacket::new);

	private PongC2SPacket(PacketByteBuf buf) {
		this();
	}

	@Override
	public void write(PacketByteBuf buf) {
	}

	@Override
	public PacketType<?> getType() {
		return TYPE;
	}
}
