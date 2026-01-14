package dev.rvbsm.fsit.client.gui.screen;

import dev.rvbsm.fsit.client.FSitModClient;
import dev.rvbsm.fsit.config.ConfigBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.OptionsSubScreen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public class ModConfigurationScreen extends OptionsSubScreen {

    private static final Component TITLE = Component.translatable("fsit.config.title");
    private final ConfigBuilder builder;

    public ModConfigurationScreen(final Screen lastScreen) {
        super(lastScreen, Minecraft.getInstance().options, TITLE);

        this.builder = FSitModClient.getInstance().getConfig().toBuilder();
    }

    private static OptionInstance<?>[] options(final ConfigBuilder builder) {
        final List<OptionInstance<?>> options = new ArrayList<>();

        options.add(OptionInstance.createBoolean("fsit.config.general.useServer", builder.useServer(), builder::useServer));

        return options.toArray(OptionInstance[]::new);
    }

    @Override
    protected void addOptions() {
        this.list.addSmall(options(this.builder));
    }

    @Override
    public void removed() {
        FSitModClient.getInstance().setConfig(this.builder.build());
    }
}
