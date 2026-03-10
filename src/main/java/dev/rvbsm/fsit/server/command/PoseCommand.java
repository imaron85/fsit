package dev.rvbsm.fsit.server.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.logging.LogUtils;
import dev.rvbsm.fsit.FSitMod;
import dev.rvbsm.fsit.api.FSitPoseHolder;
import dev.rvbsm.fsit.server.compat.permissions.Permissions;
import dev.rvbsm.fsit.world.entity.FSitPose;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.server.permissions.PermissionLevel;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;

public final class PoseCommand {

    private static final Logger LOGGER = LogUtils.getLogger();

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        var sit = FSitMod.getInstance().getConfig().commands().sit();
        if (!register(dispatcher, sit, FSitPose.SITTING, "fsit.command.sit", PermissionLevel.ALL)) {
            LOGGER.warn("Not registering sit command!");
        }

        var crawl = FSitMod.getInstance().getConfig().commands().crawl();
        if (!register(dispatcher, crawl, FSitPose.CRAWLING, "fsit.command.crawl", PermissionLevel.ALL)) {
            LOGGER.warn("Not registering crawl command!");
        }

        var lay = FSitMod.getInstance().getConfig().commands().lay();
        if (!register(dispatcher, lay, FSitPose.LAYING, "fsit.command.lay", PermissionLevel.ALL)) {
            LOGGER.warn("Not registering lay command!");
        }
    }

    private static boolean register(
            CommandDispatcher<CommandSourceStack> dispatcher,
            String name,
            FSitPose pose,
            String permission,
            PermissionLevel level) {
        if (name.isEmpty()) {
            return false;
        }

        dispatcher.register(Commands.literal(name)
                .requires(Permissions.require(permission, level))
                .executes(ctx -> executePose(ctx.getSource(), pose)));

        return true;
    }

    private static int executePose(CommandSourceStack src, FSitPose pose) throws CommandSyntaxException {
        var player = src.getPlayerOrException();
        if (player.level() != src.getLevel()) {
            throw BlockPosArgument.ERROR_OUT_OF_WORLD.create();
        }

        Vec3 srcPosition = src.getPosition();
        if (!srcPosition.closerThan(player.getEyePosition(), player.blockInteractionRange())) {
            throw BlockPosArgument.ERROR_OUT_OF_BOUNDS.create();
        }

        var resultPose = FSitPoseHolder.get(player) != FSitPose.STANDING ? FSitPose.STANDING : pose;
        FSitPoseHolder.set(player, resultPose, new FSitPose.Context.Positioned(srcPosition));

        return Command.SINGLE_SUCCESS;
    }
}
