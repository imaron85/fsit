package dev.rvbsm.fsit.server.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.logging.LogUtils;
import dev.rvbsm.fsit.FSitMod;
import dev.rvbsm.fsit.server.compat.permissions.Permissions;
import dev.rvbsm.fsit.server.network.ServerModPacketListener;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.permissions.PermissionLevel;
import org.slf4j.Logger;

public final class FSitCommand {

    private static final Logger LOGGER = LogUtils.getLogger();

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        var root = FSitMod.getInstance().getConfig().commands().root();
        if (root.isEmpty()) {
            LOGGER.info("Not registering root command!");
            return;
        }

        dispatcher.register(Commands.literal(root)
                .requires(Permissions.require("fsit.command.root", PermissionLevel.GAMEMASTERS))
                .then(Commands.literal("reload")
                        .requires(Permissions.require("fsit.command.root.reload", PermissionLevel.GAMEMASTERS))
                        .executes(ctx -> executeReload(ctx.getSource())))
                .then(Commands.literal("set")
                        .requires(Permissions.require("fsit.command.root.set", PermissionLevel.GAMEMASTERS))
                        .then(Commands.literal("riding-angle")
                                .then(Commands.argument("degrees", IntegerArgumentType.integer(0, 180))
                                        .executes(ctx -> executeSetRidingAngle(
                                                ctx.getSource(),
                                                IntegerArgumentType.getInteger(ctx, "degrees")))))
                        .then(Commands.literal("query")
                                .executes(ctx -> executeQuery(ctx.getSource())))));
    }

    private static int executeReload(CommandSourceStack src) {
        src.sendSuccess(() -> prefixedComponent(Component.translatable("commands.reload.success")), true);
        FSitMod.getInstance().reloadConfig().thenAcceptAsync(success -> {
                if (success) {
                    ServerModPacketListener.broadcastConfig(src.getServer(), FSitMod.getInstance().getConfig());
                    return;
                }

                src.sendFailure(prefixedComponent(Component.translatable("commands.reload.failure")));
            },
            Runnable::run);

        return 0;
    }

    private static int executeSetRidingAngle(CommandSourceStack src, int degrees) {
        var mod = FSitMod.getInstance();
        var updated = mod.getConfig().withRidingMinimumYaw(degrees);
        mod.setConfig(updated);
        mod.saveConfig();
        ServerModPacketListener.broadcastConfig(src.getServer(), updated);

        src.sendSuccess(
                () -> prefixed("Mount angle set to " + degrees + "\u00B0" + describeAngle(degrees)),
                true);
        return Command.SINGLE_SUCCESS;
    }

    private static int executeQuery(CommandSourceStack src) {
        int angle = FSitMod.getInstance().getConfig().ridingMinimumYaw();
        src.sendSuccess(
                () -> prefixed("Mount angle: " + angle + "\u00B0" + describeAngle(angle)),
                false);
        return Command.SINGLE_SUCCESS;
    }

    private static String describeAngle(int degrees) {
        if (degrees == 0) return " (mounting disabled)";
        if (degrees == 180) return " (any side)";
        return "";
    }

    private static Component prefixed(String body) {
        return Component.literal("[FSit] " + body);
    }

    private static Component prefixedComponent(Component body) {
        return Component.literal("[FSit] ").append(body);
    }
}
