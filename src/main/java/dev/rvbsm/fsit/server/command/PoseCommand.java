package dev.rvbsm.fsit.server.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import dev.rvbsm.fsit.FSitMod;
import dev.rvbsm.fsit.api.access.Poseable;
import dev.rvbsm.fsit.server.compat.permissions.Permissions;
import dev.rvbsm.fsit.entity.ModPose;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;

import java.util.Locale;

public enum PoseCommand {
    SIT(ModPose.SITTING),
    CRAWL(ModPose.CRAWLING),
    // TODO tbd
//    LAY(ModPose.LAYING),
//    SPIN(ModPose.SPINNING),
    ;

    private static final SimpleCommandExceptionType WRONG_DIMENSION_EXCEPTION = new SimpleCommandExceptionType(
        Text.translatable("commands.ride.mount.failure.wrong_dimension")
    );

    private final String command;
    private final ModPose pose;
    private final String permission;
    private final int permissionLevel;

    PoseCommand(ModPose pose) {
        this.command = this.name().toLowerCase(Locale.ROOT);
        this.pose = pose;
        this.permission = "%s.command.%s".formatted(FSitMod.MOD_ID, this.command);
        this.permissionLevel = 0;
    }

    public void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal(this.command)
            .requires(Permissions.require(this.permission, this.permissionLevel))
            .executes(this::execute)
        );
    }

    private int execute(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        final ServerCommandSource src = ctx.getSource();
        final ServerPlayerEntity player = src.getPlayerOrThrow();

        if (player.getEntityWorld() != src.getWorld()) {
            throw WRONG_DIMENSION_EXCEPTION.create();
        }

        if (((Poseable) player).fsit$getPose() != ModPose.STANDING) {
            ((Poseable) player).fsit$setPose(ModPose.STANDING);
            return Command.SINGLE_SUCCESS;
        }

        final Vec3d playerPos = player.getEntityPos();
        final Vec3d sourcePos = src.getPosition();
        final double interactionRange = player.getBlockInteractionRange();
        if (playerPos.distanceTo(sourcePos) > interactionRange) {
            throw BlockPosArgumentType.OUT_OF_BOUNDS_EXCEPTION.create();
        }

        ((Poseable) player).fsit$setPose(this.pose, sourcePos);
        return Command.SINGLE_SUCCESS;
    }
}
