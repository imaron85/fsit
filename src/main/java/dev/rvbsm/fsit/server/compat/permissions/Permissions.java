package dev.rvbsm.fsit.server.compat.permissions;

import dev.rvbsm.fsit.server.compat.ServerModCompat;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.permissions.Permission;
import net.minecraft.server.permissions.PermissionLevel;

import java.util.function.Predicate;

public final class Permissions {

    private Permissions() {
    }

    public static Predicate<CommandSourceStack> require(String permission, PermissionLevel defaultRequiredLevel) {
        if (ServerModCompat.PERMISSIONS_API.isLoaded()) {
            return me.lucko.fabric.api.permissions.v0.Permissions.require(permission, defaultRequiredLevel);
        }

        return src -> src.permissions().hasPermission(new Permission.HasCommandLevel(defaultRequiredLevel));
    }
}
