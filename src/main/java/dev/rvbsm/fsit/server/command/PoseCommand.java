package dev.rvbsm.fsit.server.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import dev.rvbsm.fsit.FSitMod;
import dev.rvbsm.fsit.api.PoseHolder;
import dev.rvbsm.fsit.server.compat.permissions.Permissions;
import dev.rvbsm.fsit.world.entity.ModPose;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.PermissionLevel;
import net.minecraft.world.phys.Vec3;

import java.util.Locale;

public enum PoseCommand {
    SIT(ModPose.SITTING),
    CRAWL(ModPose.CRAWLING),
    // TODO tbd
//    LAY(ModPose.LAYING),
//    SPIN(ModPose.SPINNING),
    ;

    private static final SimpleCommandExceptionType WRONG_DIMENSION_EXCEPTION = new SimpleCommandExceptionType(
        Component.translatable("commands.ride.mount.failure.wrong_dimension")
    );

    private final String command;
    private final ModPose pose;
    private final String permission;
    private final PermissionLevel permissionLevel;

    PoseCommand(final ModPose pose) {
        this.command = this.name().toLowerCase(Locale.ROOT);
        this.pose = pose;
        this.permission = "%s.command.%s".formatted(FSitMod.MOD_ID, this.command);
        this.permissionLevel = PermissionLevel.ALL;
    }

    public void register(final CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal(this.command)
            .requires(Permissions.require(this.permission, this.permissionLevel))
            .executes(this::execute)
        );
    }

    private int execute(final CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        final CommandSourceStack src = ctx.getSource();
        final ServerPlayer player = src.getPlayerOrException();

        if (player.level() != src.getLevel()) {
            throw WRONG_DIMENSION_EXCEPTION.create();
        }

        if (PoseHolder.get(player) != ModPose.STANDING) {
            PoseHolder.set(player, ModPose.STANDING);
            return Command.SINGLE_SUCCESS;
        }

        final Vec3 playerPos = player.position();
        final Vec3 sourcePos = src.getPosition();
        final double interactionRange = player.blockInteractionRange();
        if (playerPos.distanceTo(sourcePos) > interactionRange) {
            throw BlockPosArgument.ERROR_OUT_OF_BOUNDS.create();
        }

        PoseHolder.set(player, this.pose, sourcePos);
        return Command.SINGLE_SUCCESS;
    }
}
