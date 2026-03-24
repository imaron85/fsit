package dev.rvbsm.fsit.server.command;

import com.mojang.brigadier.CommandDispatcher;
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
        } else {
            dispatcher.register(Commands.literal(root)
                .requires(Permissions.require("fsit.command.root", PermissionLevel.GAMEMASTERS))
                .then(Commands.literal("reload")
                    .requires(Permissions.require("fsit.command.root.reload", PermissionLevel.GAMEMASTERS))
                    .executes(ctx -> executeReload(ctx.getSource()))));
        }
    }

    private static int executeReload(CommandSourceStack src) {
        src.sendSuccess(() -> Component.literal("[FSit] ").append(Component.translatable("commands.reload.success")), true);
        FSitMod.getInstance().reloadConfig().thenAcceptAsync(
            success -> {
                if (success) {
                    ServerModPacketListener.broadcastConfig(src.getServer());
                    return;
                }

                src.sendFailure(Component.literal("[FSit] ").append(Component.translatable("commands.reload.failure")));
            }, Runnable::run
        );

        return 0;
    }
}
