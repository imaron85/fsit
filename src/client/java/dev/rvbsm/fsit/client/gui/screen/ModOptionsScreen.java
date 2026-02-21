package dev.rvbsm.fsit.client.gui.screen;

import dev.rvbsm.fsit.client.FSitModClient;
import dev.rvbsm.fsit.client.option.ModOptions;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.OptionsSubScreen;
import net.minecraft.network.chat.Component;

public final class ModOptionsScreen extends OptionsSubScreen {

    private static final Component TITLE = Component.translatable("option.fsit.title");

    private final ModOptions modOptions;

    public ModOptionsScreen(Screen lastScreen) {
        super(lastScreen, Minecraft.getInstance().options, TITLE);
        this.modOptions = FSitModClient.getInstance().getOptions();
    }

    @Override
    protected void addOptions() {
        if (this.list != null) {
            this.list.addBig(this.modOptions.useServer);

            // todo: disable/change adjacent options.
            //  e.g. sneak_window = 0 -> disable sneak_minimum_pitch
            this.list.addHeader(Component.translatable("option.fsit.sitting.title"));
            this.list.addSmall(this.modOptions.seatsTrait, this.modOptions.seatsCentered);

            this.list.addHeader(Component.translatable("option.fsit.interaction.title"));
            this.list.addSmall(this.modOptions.blockInteractionDistance, this.modOptions.playerInteractionDistance);

            this.list.addHeader(Component.translatable("option.fsit.sneak.title"));
            this.list.addSmall(
                this.modOptions.sneakWindow, this.modOptions.sneakMinimumPitch,
                this.modOptions.sneakCrawlingDetection
            );

            this.list.addHeader(Component.translatable("option.fsit.control.title"));
            this.list.addSmall(
                this.modOptions.hybridSit, this.modOptions.hybridCrawl,
                this.modOptions.hybridKeyWindow
            );
        }
    }

    @Override
    public void removed() {
        this.modOptions.save();
    }
}
