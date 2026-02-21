package dev.rvbsm.fsit.mixin.client;

import dev.rvbsm.fsit.mixin.PlayerMixin;
import net.minecraft.client.player.AbstractClientPlayer;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(AbstractClientPlayer.class)
public abstract class AbstractClientPlayerMixin extends PlayerMixin {}
