package dev.rvbsm.fsit.mixin.server;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import dev.rvbsm.fsit.api.access.Configurable;
import dev.rvbsm.fsit.api.access.Poseable;
import dev.rvbsm.fsit.config.ConfigData;
import dev.rvbsm.fsit.entity.ModPose;
import dev.rvbsm.fsit.server.compat.geyser.GeyserApi;
import dev.rvbsm.fsit.server.network.ServerModNetworkHandler;
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
import org.spongepowered.asm.mixin.Final;
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

    @ModifyVariable(method = "handleUseItemOn", at = @At("STORE"))
    private InteractionResult onInteractBlock(
        InteractionResult actionResult,
        @Local ServerLevel world,
        @Local LocalRef<InteractionHand> handRef,
        @Local ItemStack stack,
        @Local(ordinal = 0) Vec3 hitPos,
        @Local BlockPos hitBlockPos,
        @Local Direction hitSide
    ) {
        if (actionResult != InteractionResult.PASS || stack.getUseAnimation().ordinal() > 0) {
            return actionResult;
        }

        if (hitSide != Direction.UP || this.player.isSecondaryUseActive()) {
            return actionResult;
        }

        if (handRef.get() == InteractionHand.MAIN_HAND && !GeyserApi.isBedrockPlayer(this.player.getUUID())) {
            return actionResult;
        }

        final ConfigData config = ((Configurable) this.player).fsit$configOrDefault();
        if (!config.onUse().sitting() || !hitPos.closerThan(hitPos, config.onUse().range(), 2.0)) {
            return actionResult;
        }

        if (config.onUse().checkSuffocation()) {
            final Vec3 eyePos = this.player.position().add(0.0, this.player.getEyeHeight() - 0.5, 0.0);
            final double width = this.player.getBbWidth() * 0.8;
            final AABB collisionsBox = AABB.ofSize(eyePos, width, 1.0e-6, width);

            final boolean isSuffocation = BlockPos.betweenClosedStream(collisionsBox).anyMatch(blockPos -> {
                final BlockState state = world.getBlockState(blockPos);
                return !state.isAir() && state.isViewBlocking(world, blockPos) && Shapes.joinIsNotEmpty(
                    state.getCollisionShape(world, blockPos).move(blockPos.getX(), blockPos.getY(), blockPos.getZ()),
                    Shapes.create(collisionsBox),
                    BooleanOp.AND
                );
            });

            if (isSuffocation) {
                return actionResult;
            }
        }

        final BlockState hitState = world.getBlockState(hitBlockPos);

        for (final var entry : SITTABLE_PROPERTIES.entrySet()) {
            final Property<?> propertyKey = entry.getKey();
            final Set<Comparable<?>> expectedProperties = entry.getValue();

            if (hitState.hasProperty(propertyKey)) {
                final Comparable<?> property = hitState.getValue(propertyKey);
                if (!expectedProperties.contains(property)) {
                    return actionResult;
                }
            }
        }

        if (!config.onUse().blocks().test(hitState.getBlock())) {
            return actionResult;
        }

        ((Poseable) this.player).fsit$setPose(ModPose.SITTING, hitPos);

        // swing main hand
        handRef.set(InteractionHand.MAIN_HAND);
        //? if >=1.21.2 {
        return InteractionResult.SUCCESS_SERVER;
        //?} else
        /*return ActionResult.SUCCESS;*/
    }

    @Mixin(targets = "net.minecraft.server.network.ServerGamePacketListenerImpl$1")
    public static abstract class ServerboundInteractPacket_HandlerMixin {

        @Shadow
        @Final
        ServerGamePacketListenerImpl field_28963;

        @Shadow
        @Final
        Entity val$target;

        @ModifyVariable(method = "performInteraction", at = @At("STORE"))
        private InteractionResult onInteractEntity(
            InteractionResult actionResult,
            @Local(argsOnly = true) LocalRef<InteractionHand> handRef,
            @Local(ordinal = 0) ItemStack stack
        ) {
            if (!val$target.isAlwaysTicking()) {
                return actionResult;
            }

            if (actionResult != InteractionResult.PASS || stack.getUseAnimation().ordinal() > 0) {
                return actionResult;
            }

            if (handRef.get() == InteractionHand.MAIN_HAND && !GeyserApi.isBedrockPlayer(field_28963.player.getUUID())) {
                return actionResult;
            }

            if (field_28963.player.equals(val$target) || field_28963.player.getUUID().equals(val$target.getUUID())) {
                return actionResult;
            }

            final ConfigData config = ((Configurable) field_28963.player).fsit$configOrDefault();
            if (!config.onUse().riding()) {
                return actionResult;
            }

            if (!field_28963.player.closerThan(val$target, config.onUse().range(), 2.0)) {
                return actionResult;
            }

            ServerModNetworkHandler.requestRiding(field_28963.player, (ServerPlayer) val$target);

            handRef.set(InteractionHand.MAIN_HAND);
            //? if >=1.21.2 {
            return InteractionResult.SUCCESS_SERVER;
            //?} else
            /*return ActionResult.SUCCESS;*/
        }
    }
}
