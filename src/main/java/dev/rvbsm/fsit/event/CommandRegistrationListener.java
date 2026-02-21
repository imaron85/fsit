package dev.rvbsm.fsit.event;

import com.mojang.brigadier.CommandDispatcher;
import dev.rvbsm.fsit.server.command.FSitCommand;
import dev.rvbsm.fsit.server.command.PoseCommand;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public final class CommandRegistrationListener implements CommandRegistrationCallback {

    public static final CommandRegistrationListener INSTANCE = new CommandRegistrationListener();

    private CommandRegistrationListener() {}

    @Override
    public void register(
            CommandDispatcher<CommandSourceStack> dispatcher,
            CommandBuildContext context,
            Commands.CommandSelection selection) {

        FSitCommand.register(dispatcher);
        PoseCommand.register(dispatcher);
    }
}
