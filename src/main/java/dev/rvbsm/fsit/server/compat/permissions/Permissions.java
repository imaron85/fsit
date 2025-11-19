package dev.rvbsm.fsit.server.compat.permissions;

import dev.rvbsm.fsit.server.compat.ServerModCompat;
import net.minecraft.commands.CommandSourceStack;

import java.util.function.Predicate;

public final class Permissions {

    private Permissions() {
    }

    public static Predicate<CommandSourceStack> hasPermissionLevel(final int level) {
        return src -> src.hasPermission(level);
    }

    public static Predicate<CommandSourceStack> require(final String permission, final int defaultRequiredLevel) {
        if (ServerModCompat.PERMISSIONS_API.isLoaded()) {
            return me.lucko.fabric.api.permissions.v0.Permissions.require(permission, defaultRequiredLevel);
        }

        return hasPermissionLevel(defaultRequiredLevel);
    }
}
