package dev.rvbsm.fsit.server.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.logging.LogUtils;
import dev.rvbsm.fsit.FSitMod;
import dev.rvbsm.fsit.server.network.ServerModPacketListener;
import net.fabricmc.fabric.api.permission.v1.PermissionNode;
import net.fabricmc.fabric.api.permission.v1.PermissionPredicates;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.permissions.PermissionLevel;
import org.slf4j.Logger;

public final class FSitCommand {

    private static final Logger LOGGER = LogUtils.getLogger();

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext context) {
        var root = FSitMod.getInstance().getConfig().commands().root();
        if (root.isEmpty()) {
            LOGGER.info("Not registering root command!");
            return;
        }

        dispatcher.register(Commands.literal(root)
                .requires(PermissionPredicates.require(PermissionNode.of(Identifier.fromNamespaceAndPath("fsit","command.root")), PermissionLevel.ALL))
                .then(Commands.literal("reload")
                        .requires(PermissionPredicates.require(PermissionNode.of(Identifier.fromNamespaceAndPath("fsit","command.root.reload")), PermissionLevel.GAMEMASTERS))
                        .executes(ctx -> executeReload(ctx.getSource())))
                .then(Commands.literal("set")
                        .requires(PermissionPredicates.require(PermissionNode.of(Identifier.fromNamespaceAndPath("fsit","command.root.set")), PermissionLevel.GAMEMASTERS))
                        .then(Commands.literal("riding-angle")
                                .then(Commands.argument("degrees", IntegerArgumentType.integer(0, 180))
                                        .executes(ctx -> executeSetRidingAngle(
                                                ctx.getSource(),
                                                IntegerArgumentType.getInteger(ctx, "degrees")))))
                        .then(Commands.literal("query")
                                .executes(ctx -> executeQuery(ctx.getSource()))))
                .then(PlayerSettingsCommand.hands(context)
                        .requires(PermissionPredicates.require(PermissionNode.of(Identifier.fromNamespaceAndPath("fsit","command.root.hands")), PermissionLevel.ALL)))
                .then(PlayerSettingsCommand.teleport()
                        .requires(PermissionPredicates.require(PermissionNode.of(Identifier.fromNamespaceAndPath("fsit","command.root.teleport")), PermissionLevel.GAMEMASTERS))));
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
                () -> prefixed("Mount angle set to " + degrees + "°" + describeAngle(degrees)),
                true);
        return Command.SINGLE_SUCCESS;
    }

    private static int executeQuery(CommandSourceStack src) {
        int angle = FSitMod.getInstance().getConfig().ridingMinimumYaw();
        src.sendSuccess(
                () -> prefixed("Mount angle: " + angle + "°" + describeAngle(angle)),
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
