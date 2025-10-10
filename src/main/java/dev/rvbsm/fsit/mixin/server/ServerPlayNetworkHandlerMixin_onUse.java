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
import net.minecraft.block.BlockState;
import net.minecraft.block.enums.BlockHalf;
import net.minecraft.block.enums.SlabType;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.ClientConnection;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ConnectedClientData;
import net.minecraft.server.network.ServerCommonNetworkHandler;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.Properties;
import net.minecraft.state.property.Property;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.function.BooleanBiFunction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShapes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.Map;
import java.util.Set;

@Mixin(ServerPlayNetworkHandler.class)
public abstract class ServerPlayNetworkHandlerMixin_onUse extends ServerCommonNetworkHandler {

    @Unique
    private static final Map<Property<?>, Set<Comparable<?>>> SITTABLE_PROPERTIES =
        ImmutableMap.<Property<?>, Set<Comparable<?>>>builder()
            .put(Properties.AXIS, ImmutableSet.of(Direction.Axis.X, Direction.Axis.Z))
            .put(Properties.BLOCK_HALF, ImmutableSet.of(BlockHalf.BOTTOM))
            .put(Properties.SLAB_TYPE, ImmutableSet.of(SlabType.BOTTOM))
            .build();

    @Shadow
    public ServerPlayerEntity player;

    private ServerPlayNetworkHandlerMixin_onUse(MinecraftServer server, ClientConnection connection, ConnectedClientData clientData) {
        super(server, connection, clientData);
    }

    @ModifyVariable(method = "onPlayerInteractBlock", at = @At("STORE"))
    private ActionResult onInteractBlock(
        ActionResult actionResult,
        @Local ServerWorld world,
        @Local LocalRef<Hand> handRef,
        @Local ItemStack stack,
        @Local(ordinal = 0) Vec3d hitPos,
        @Local BlockPos hitBlockPos,
        @Local Direction hitSide
    ) {
        if (actionResult != ActionResult.PASS || stack.getUseAction().ordinal() > 0) {
            return actionResult;
        }

        if (hitSide != Direction.UP || this.player.shouldCancelInteraction()) {
            return actionResult;
        }

        if (handRef.get() == Hand.MAIN_HAND && !GeyserApi.isBedrockPlayer(this.player.getUuid())) {
            return actionResult;
        }

        final ConfigData config = ((Configurable) this.player).fsit$configOrDefault();
        if (!config.onUse().sitting() || !hitPos.isWithinRangeOf(hitPos, config.onUse().range(), 2.0)) {
            return actionResult;
        }

        if (config.onUse().checkSuffocation()) {
            final Vec3d eyePos = this.player.getEntityPos().add(0.0, this.player.getStandingEyeHeight() - 0.5, 0.0);
            final double width = this.player.getWidth() * 0.8;
            final Box collisionsBox = Box.of(eyePos, width, 1.0e-6, width);

            final boolean isSuffocation = BlockPos.stream(collisionsBox).anyMatch(blockPos -> {
                final BlockState state = world.getBlockState(blockPos);
                return !state.isAir() && state.shouldBlockVision(world, blockPos) && VoxelShapes.matchesAnywhere(
                    state.getCollisionShape(world, blockPos).offset(blockPos.getX(), blockPos.getY(), blockPos.getZ()),
                    VoxelShapes.cuboid(collisionsBox),
                    BooleanBiFunction.AND
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

            if (hitState.contains(propertyKey)) {
                final Comparable<?> property = hitState.get(propertyKey);
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
        handRef.set(Hand.MAIN_HAND);
        //? if >=1.21.2 {
        return ActionResult.SUCCESS_SERVER;
        //?} else
        /*return ActionResult.SUCCESS;*/
    }

    @Mixin(targets = "net.minecraft.server.network.ServerPlayNetworkHandler$1")
    public static abstract class PlayerInteractEntityC2SPacket_HandlerMixin {

        @Shadow
        @Final
        ServerPlayNetworkHandler field_28963;

        @Shadow
        @Final
        Entity field_28962;

        @ModifyVariable(method = "processInteract", at = @At("STORE"))
        private ActionResult onInteractEntity(
            ActionResult actionResult,
            @Local(argsOnly = true) LocalRef<Hand> handRef,
            @Local(ordinal = 0) ItemStack stack
        ) {
            if (!field_28962.isPlayer()) {
                return actionResult;
            }

            if (actionResult != ActionResult.PASS || stack.getUseAction().ordinal() > 0) {
                return actionResult;
            }

            if (handRef.get() == Hand.MAIN_HAND && !GeyserApi.isBedrockPlayer(field_28963.player.getUuid())) {
                return actionResult;
            }

            if (field_28963.player.equals(field_28962) || field_28963.player.getUuid().equals(field_28962.getUuid())) {
                return actionResult;
            }

            final ConfigData config = ((Configurable) field_28963.player).fsit$configOrDefault();
            if (!config.onUse().riding()) {
                return actionResult;
            }

            if (!field_28963.player.isInRange(field_28962, config.onUse().range(), 2.0)) {
                return actionResult;
            }

            ServerModNetworkHandler.requestRiding(field_28963.player, (ServerPlayerEntity) field_28962);

            handRef.set(Hand.MAIN_HAND);
            //? if >=1.21.2 {
            return ActionResult.SUCCESS_SERVER;
            //?} else
            /*return ActionResult.SUCCESS;*/
        }
    }
}
