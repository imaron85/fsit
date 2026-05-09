package dev.rvbsm.fsit.server.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import dev.rvbsm.fsit.FSitMod;
import dev.rvbsm.fsit.api.FSitPlayerSettings;
import dev.rvbsm.fsit.api.PlayerSettings;
import dev.rvbsm.fsit.config.RidingTeleportMode;
import dev.rvbsm.fsit.server.network.ServerModPacketListener;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ResourceOrTagArgument;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ExtraCodecs.TagOrElementLocation;
import net.minecraft.world.item.Item;

import java.util.ArrayList;
import java.util.List;

public final class PlayerSettingsCommand {

    private static final SimpleCommandExceptionType ERROR_DUPLICATE = new SimpleCommandExceptionType(
        Component.translatableWithFallback("commands.fsit.hands.error.duplicate", "That entry is already in your list"));
    private static final SimpleCommandExceptionType ERROR_NOT_PRESENT = new SimpleCommandExceptionType(
        Component.translatableWithFallback("commands.fsit.hands.error.not_present", "That entry is not in your list"));

    private static final String ITEM_ARG = "item";

    private PlayerSettingsCommand() {}

    public static LiteralArgumentBuilder<CommandSourceStack> hands(CommandBuildContext context) {
        return Commands.literal("hands")
            .then(Commands.literal("add")
                .then(Commands.argument(ITEM_ARG, ResourceOrTagArgument.resourceOrTag(context, Registries.ITEM))
                    .executes(PlayerSettingsCommand::executeHandsAdd)))
            .then(Commands.literal("remove")
                .then(Commands.argument(ITEM_ARG, ResourceOrTagArgument.resourceOrTag(context, Registries.ITEM))
                    .executes(PlayerSettingsCommand::executeHandsRemove)))
            .then(Commands.literal("clear").executes(PlayerSettingsCommand::executeHandsClear))
            .then(Commands.literal("reset").executes(PlayerSettingsCommand::executeHandsReset))
            .then(Commands.literal("list").executes(PlayerSettingsCommand::executeHandsList));
    }

    public static LiteralArgumentBuilder<CommandSourceStack> teleport() {
        // /fsit teleport (no arg) → print status; /fsit teleport <mode> → set
        var builder = Commands.literal("teleport").executes(PlayerSettingsCommand::executeTeleportQuery);
        for (var mode : RidingTeleportMode.values()) {
            builder = builder.then(Commands.literal(mode.getSerializedName())
                .executes(ctx -> executeTeleportSet(ctx.getSource(), mode)));
        }
        return builder;
    }

    private static int executeHandsAdd(com.mojang.brigadier.context.CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        var player = ctx.getSource().getPlayerOrException();
        var entry = parseEntry(ctx);
        var settings = FSitPlayerSettings.get(player);
        if (containsEntry(settings.idleHandItems(), entry)) {
            throw ERROR_DUPLICATE.create();
        }
        var newList = new ArrayList<>(settings.idleHandItems());
        newList.add(entry);
        applySettings(player, settings.withIdleHandItems(newList));
        ctx.getSource().sendSuccess(
            () -> prefixed(tr("commands.fsit.hands.added", "Added %s to your idle-hand list", formatEntry(entry))),
            false);
        return Command.SINGLE_SUCCESS;
    }

    private static int executeHandsRemove(com.mojang.brigadier.context.CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        var player = ctx.getSource().getPlayerOrException();
        var entry = parseEntry(ctx);
        var settings = FSitPlayerSettings.get(player);
        if (!containsEntry(settings.idleHandItems(), entry)) {
            throw ERROR_NOT_PRESENT.create();
        }
        var newList = new ArrayList<>(settings.idleHandItems());
        newList.removeIf(e -> sameEntry(e, entry));
        applySettings(player, settings.withIdleHandItems(newList));
        ctx.getSource().sendSuccess(
            () -> prefixed(tr("commands.fsit.hands.removed", "Removed %s from your idle-hand list", formatEntry(entry))),
            false);
        return Command.SINGLE_SUCCESS;
    }

    private static int executeHandsClear(com.mojang.brigadier.context.CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        var player = ctx.getSource().getPlayerOrException();
        var settings = FSitPlayerSettings.get(player);
        applySettings(player, settings.withIdleHandItems(List.of()));
        ctx.getSource().sendSuccess(
            () -> prefixed(tr("commands.fsit.hands.cleared", "Idle-hand list cleared")),
            false);
        return Command.SINGLE_SUCCESS;
    }

    private static int executeHandsReset(com.mojang.brigadier.context.CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        var player = ctx.getSource().getPlayerOrException();
        var settings = FSitPlayerSettings.get(player);
        applySettings(player, settings.withIdleHandItems(PlayerSettings.DEFAULT_IDLE_HAND_ITEMS));
        ctx.getSource().sendSuccess(
            () -> prefixed(tr("commands.fsit.hands.reset", "Idle-hand list reset to defaults")),
            false);
        return Command.SINGLE_SUCCESS;
    }

    private static int executeHandsList(com.mojang.brigadier.context.CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        var player = ctx.getSource().getPlayerOrException();
        var settings = FSitPlayerSettings.get(player);
        var items = settings.idleHandItems();
        if (items.isEmpty()) {
            ctx.getSource().sendSuccess(
                () -> prefixed(tr("commands.fsit.hands.list.empty",
                    "Idle-hand list is empty — every non-empty hand will block sit/ride")),
                false);
        } else {
            var formatted = Component.empty();
            for (int i = 0; i < items.size(); i++) {
                if (i > 0) formatted.append(Component.literal(", ").withStyle(ChatFormatting.GRAY));
                formatted.append(formatEntry(items.get(i)));
            }
            ctx.getSource().sendSuccess(
                () -> prefixed(tr("commands.fsit.hands.list", "Idle hand items (%s): %s", items.size(), formatted)),
                false);
        }
        return Command.SINGLE_SUCCESS;
    }

    private static int executeTeleportQuery(com.mojang.brigadier.context.CommandContext<CommandSourceStack> ctx) {
        // server-wide setting; works from console too
        var mode = FSitMod.getInstance().getConfig().ridingTeleportMode();
        ctx.getSource().sendSuccess(
            () -> prefixed(tr("commands.fsit.teleport.query", "Riding teleport mode: %s", modeName(mode))),
            false);
        return Command.SINGLE_SUCCESS;
    }

    private static int executeTeleportSet(CommandSourceStack src, RidingTeleportMode mode) {
        var mod = FSitMod.getInstance();
        var updated = mod.getConfig().withRidingTeleportMode(mode);
        if (updated != mod.getConfig()) {
            mod.setConfig(updated);
            mod.saveConfig();
        }
        // broadcast=true so other ops see the change in their console output
        src.sendSuccess(
            () -> prefixed(tr("commands.fsit.teleport.set", "Riding teleport mode set to %s", modeName(mode))),
            true);
        return Command.SINGLE_SUCCESS;
    }

    private static Component tr(String key, String fallback, Object... args) {
        return Component.translatableWithFallback(key, fallback, args);
    }

    private static Component modeName(RidingTeleportMode mode) {
        var name = mode.getSerializedName();
        // capitalized English fallback so vanilla clients see "Vanilla" / "Disabled" / "Enhanced"
        // rather than the raw translation key.
        var fallback = Character.toUpperCase(name.charAt(0)) + name.substring(1);
        return Component.translatableWithFallback("option.fsit.riding_teleport_mode." + name, fallback)
            .withStyle(ChatFormatting.YELLOW);
    }

    private static void applySettings(ServerPlayer player, PlayerSettings settings) {
        FSitPlayerSettings.set(player, settings);
        ServerModPacketListener.sendPlayerSettings(player);
    }

    private static TagOrElementLocation parseEntry(com.mojang.brigadier.context.CommandContext<CommandSourceStack> ctx)
            throws CommandSyntaxException {
        ResourceOrTagArgument.Result<Item> result = ResourceOrTagArgument.getResourceOrTag(ctx, ITEM_ARG, Registries.ITEM);
        return result.unwrap().map(
            holder -> new TagOrElementLocation(holder.key().identifier(), false),
            tag -> new TagOrElementLocation(tag.key().location(), true)
        );
    }

    private static boolean containsEntry(List<TagOrElementLocation> list, TagOrElementLocation entry) {
        for (var e : list) {
            if (sameEntry(e, entry)) return true;
        }
        return false;
    }

    private static boolean sameEntry(TagOrElementLocation a, TagOrElementLocation b) {
        return a.tag() == b.tag() && a.id().equals(b.id());
    }

    private static Component formatEntry(TagOrElementLocation entry) {
        var label = (entry.tag() ? "#" : "") + entry.id();
        return Component.literal(label).withStyle(ChatFormatting.YELLOW);
    }

    private static Component prefixed(Component body) {
        return Component.literal("[FSit] ").append(body);
    }
}
