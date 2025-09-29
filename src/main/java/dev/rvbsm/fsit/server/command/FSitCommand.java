package dev.rvbsm.fsit.server.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.logging.LogUtils;
import dev.rvbsm.fsit.FSitMod;
import dev.rvbsm.fsit.config.ConfigLoader;
import dev.rvbsm.fsit.server.compat.permissions.Permissions;
import net.minecraft.command.CommandSource;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import org.slf4j.Logger;

public final class FSitCommand {

    private static final Logger LOGGER = LogUtils.getLogger();

    private static final String PERMISSION_ROOT = "fsit.command.root";
    private static final String PERMISSION_RELOAD = "fsit.command.root.reload";
    private static final String PERMISSION_GET = "fsit.command.root.get";
    private static final String PERMISSION_SET = "fsit.command.root.set";
    private static final int PERMISSION_ROOT_LEVEL = 2;
    private static final int PERMISSION_RELOAD_LEVEL = 2;
    private static final int PERMISSION_GET_LEVEL = 2;
    private static final int PERMISSION_SET_LEVEL = 2;

    private static final Text PREFIX = Text.literal("[/fsit] ");
    private static final Text RELOAD_SUCCESS_MESSAGE = PREFIX.copy().append(Text.translatable("commands.reload.success"));
    private static final Text RELOAD_FAILURE_MESSAGE = PREFIX.copy().append(Text.translatable("commands.reload.failure"));

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("fsit")
            .requires(Permissions.require(PERMISSION_ROOT, PERMISSION_ROOT_LEVEL))
            .then(CommandManager.literal("reload")
                .requires(Permissions.require(PERMISSION_RELOAD, PERMISSION_RELOAD_LEVEL))
                .executes(FSitCommand::executeReload))

            .then(CommandManager.literal("config")
                .requires(Permissions.require(PERMISSION_GET, PERMISSION_GET_LEVEL))
                .executes(FSitCommand::executeConfig)
                .then(CommandManager.argument("key", StringArgumentType.string())
                    .suggests((ctx, b) -> CommandSource.suggestMatching(ConfigLoader.CONFIG_KEYS, b))
                    .executes(FSitCommand::executeGet)
                    .then(CommandManager.argument("value", StringArgumentType.string())
                        // todo smarty pants suggestions
                        .requires(Permissions.require(PERMISSION_SET, PERMISSION_SET_LEVEL))
                        .executes(FSitCommand::executeSet))
                )
            )
        );
    }

    private static int executeReload(CommandContext<ServerCommandSource> ctx) {
        ctx.getSource().sendFeedback(() -> RELOAD_SUCCESS_MESSAGE, true);
        FSitMod.getInstance().reloadConfig().exceptionally(e -> {
            LOGGER.warn("Failed to execute configuration reload", e);
            ctx.getSource().sendError(RELOAD_FAILURE_MESSAGE);

            return null;
        });

        return 0;
    }

    // todo some syntax highlighting would be nice
    private static int executeConfig(CommandContext<ServerCommandSource> ctx) {
        ConfigLoader.saveJson(FSitMod.getInstance().getConfig()).thenAccept(configJson -> {
            ctx.getSource().sendFeedback(() -> Text.of(configJson), false);
        });

        return 0;
    }

    private static int executeGet(CommandContext<ServerCommandSource> ctx) {
        final String key = ctx.getArgument("key", String.class);

        ConfigLoader.getJson(FSitMod.getInstance().getConfig(), key).thenAccept(value -> {
            if (value == null) {
                ctx.getSource().sendError(Text.translatable("arguments.nbtpath.nothing_found", key));
                return;
            }

            ctx.getSource().sendFeedback(() -> Text.of("Config option %s is %s".formatted(key, value)), false);
        });

        return 0;
    }

    // todo appending/removing for lists? sounds like overengineering for one field lol
    private static int executeSet(CommandContext<ServerCommandSource> ctx) {
        final String key = ctx.getArgument("key", String.class);
        final String value =  ctx.getArgument("value", String.class);

        ConfigLoader.setJson(FSitMod.getInstance().getConfig(), key, value).thenAccept(config -> {
            if (config == null) {
                ctx.getSource().sendError(Text.translatable("argument.enum.invalid", value));
                return;
            }

            ctx.getSource().sendFeedback(() -> Text.of("Config option %s set to %s".formatted(key, value)), true);
            FSitMod.getInstance().setConfig(config);
        });

        return 0;
    }
}
