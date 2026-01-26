package dev.rvbsm.fsit.mixin.server;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import dev.rvbsm.fsit.api.access.Configurable;
import dev.rvbsm.fsit.api.access.Poseable;
import dev.rvbsm.fsit.config.ConfigData;
import dev.rvbsm.fsit.server.compat.geyser.GeyserApi;
import dev.rvbsm.fsit.world.entity.ModPose;
import dev.rvbsm.fsit.world.entity.RideEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.Connection;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerCommonPacketListenerImpl;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.Map;
import java.util.Set;

@Mixin(ServerGamePacketListenerImpl.class)
public abstract class ServerGamePacketListenerImplMixin_onUse extends ServerCommonPacketListenerImpl {

    @Unique
    private static final Map<Property<?>, Set<Comparable<?>>> SITTABLE_PROPERTIES =
        ImmutableMap.<Property<?>, Set<Comparable<?>>>builder()
            .put(BlockStateProperties.AXIS, ImmutableSet.of(Direction.Axis.X, Direction.Axis.Z))
            .put(BlockStateProperties.HALF, ImmutableSet.of(Half.BOTTOM))
            .put(BlockStateProperties.SLAB_TYPE, ImmutableSet.of(SlabType.BOTTOM))
            .build();

    @Shadow
    public ServerPlayer player;

    private ServerGamePacketListenerImplMixin_onUse(MinecraftServer server, Connection connection, CommonListenerCookie clientData) {
        super(server, connection, clientData);
    }

    @ModifyVariable(method = "handleUseItemOn", at = @At("STORE"), name = "interactionResult")
    private InteractionResult onInteractBlock(
        final InteractionResult interactionResult,
        final @Local(name = "level") ServerLevel level,
        final @Local(name = "hand") LocalRef<InteractionHand> handRef,
        final @Local(name = "itemStack") ItemStack itemStack,
        final @Local(name = "location") Vec3 location,
        final @Local(name = "pos") BlockPos pos,
        final @Local(name = "direction") Direction direction
    ) {
        if (interactionResult != InteractionResult.PASS || itemStack.getUseAnimation().ordinal() > 0) {
            return interactionResult;
        }

        if (direction != Direction.UP || this.player.isSecondaryUseActive()) {
            return interactionResult;
        }

        if (handRef.get() == InteractionHand.MAIN_HAND && !GeyserApi.isBedrockPlayer(this.player.getUUID())) {
            return interactionResult;
        }

        final ConfigData config = ((Configurable) this.player).fsit$configOrDefault();
        if (!config.onUse().sitting() || !location.closerThan(location, config.onUse().range(), 2.0)) {
            return interactionResult;
        }

        if (config.onUse().checkSuffocation()) {
            final Vec3 eyePos = this.player.position().add(0.0, this.player.getEyeHeight() - 0.5, 0.0);
            final double width = this.player.getBbWidth() * 0.8;
            final AABB collisionsBox = AABB.ofSize(eyePos, width, 1.0e-6, width);

            final boolean isSuffocation = BlockPos.betweenClosedStream(collisionsBox).anyMatch(blockPos -> {
                final BlockState state = level.getBlockState(blockPos);
                return !state.isAir() && state.isViewBlocking(level, blockPos) && Shapes.joinIsNotEmpty(
                    state.getCollisionShape(level, blockPos).move(blockPos.getX(), blockPos.getY(), blockPos.getZ()),
                    Shapes.create(collisionsBox),
                    BooleanOp.AND
                );
            });

            if (isSuffocation) {
                return interactionResult;
            }
        }

        final BlockState hitState = level.getBlockState(pos);

        for (final var entry : SITTABLE_PROPERTIES.entrySet()) {
            final Property<?> propertyKey = entry.getKey();
            final Set<Comparable<?>> expectedProperties = entry.getValue();

            if (hitState.hasProperty(propertyKey)) {
                final Comparable<?> property = hitState.getValue(propertyKey);
                if (!expectedProperties.contains(property)) {
                    return interactionResult;
                }
            }
        }

        if (!config.onUse().blocks().test(hitState.getBlock())) {
            return interactionResult;
        }

        ((Poseable) this.player).fsit$setPose(ModPose.SITTING, location);

        // swing main hand
        handRef.set(InteractionHand.MAIN_HAND);
        return InteractionResult.SUCCESS_SERVER;
    }

    @ModifyVariable(method = "handleInteract", at = @At("STORE"), name = "result")
    private InteractionResult onInteractEntity(
        final InteractionResult interactionResult,
        @Local(name = "target") final Entity target,
        @Local(name = "hand") final LocalRef<InteractionHand> handRef,
        @Local(name = "tool") final ItemStack tool
    ) {
        if (!target.isAlwaysTicking()) {
            return interactionResult;
        }

        if (interactionResult != InteractionResult.PASS || tool.getUseAnimation().ordinal() > 0) {
            return interactionResult;
        }

        if (handRef.get() == InteractionHand.MAIN_HAND && !GeyserApi.isBedrockPlayer(this.player.getUUID())) {
            return interactionResult;
        }

        if (player.equals(target) || this.player.getUUID().equals(target.getUUID())) {
            return interactionResult;
        }

        if (player.hasIndirectPassenger(target)) {
            return interactionResult;
        }

        final ConfigData config = ((Configurable) this.player).fsit$configOrDefault();
        if (!config.onUse().riding()) {
            return interactionResult;
        }

        if (!this.player.closerThan(target, config.onUse().range(), 2.0)) {
            return interactionResult;
        }

        player.level().addFreshEntity(new RideEntity(player, (ServerPlayer) target));
        handRef.set(InteractionHand.MAIN_HAND);
        return InteractionResult.SUCCESS_SERVER;
    }
}
