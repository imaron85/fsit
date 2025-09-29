package dev.rvbsm.fsit.server.compat.permissions;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.command.ServerCommandSource;

import java.util.function.Predicate;

public final class Permissions {

    private static final boolean PERMISSIONS_API = FabricLoader.getInstance().isModLoaded("fabric-permissions-api-v0");

    public static Predicate<ServerCommandSource> hasPermissionLevel(final int level) {
        return src -> src.hasPermissionLevel(level);
    }

    public static Predicate<ServerCommandSource> require(final String permission, final int defaultRequiredLevel) {
        if (PERMISSIONS_API) {
            return me.lucko.fabric.api.permissions.v0.Permissions.require(permission, defaultRequiredLevel);
        }

        return hasPermissionLevel(defaultRequiredLevel);
    }
}
