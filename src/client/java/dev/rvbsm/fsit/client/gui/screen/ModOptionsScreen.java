package dev.rvbsm.fsit.client.gui.screen;

import dev.rvbsm.fsit.client.FSitModClient;
import dev.rvbsm.fsit.client.option.ModOptions;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.OptionsSubScreen;
import net.minecraft.network.chat.Component;

public final class ModOptionsScreen extends OptionsSubScreen {

    static final Component TITLE = Component.translatable("option.fsit.title");
    static final Component SITTING = Component.translatable("option.fsit.sitting.title");
    static final Component INTERACTION = Component.translatable("option.fsit.interaction.title");
    static final Component SNEAK = Component.translatable("option.fsit.sneak.title");
    static final Component CONTROL = Component.translatable("option.fsit.control.title");

    private final ModOptions modOptions;

    public ModOptionsScreen(Screen lastScreen) {
        super(lastScreen, Minecraft.getInstance().options, TITLE);
        this.modOptions = FSitModClient.getInstance().getOptions();
    }

    @Override
    protected void addOptions() {
        if (this.list != null) {
            this.list.addBig(this.modOptions.useServer);
            this.list.addHeader(SITTING);
            this.list.addSmall(this.modOptions.seatsTrait, this.modOptions.seatsCentered);

            this.list.addHeader(INTERACTION);
            this.list.addSmall(this.modOptions.blockInteraction, this.modOptions.ridingMinimumYaw);
            // TODO: block ids list, probably separate screen

            this.list.addHeader(SNEAK);
            this.list.addSmall(
                this.modOptions.sneakWindow, this.modOptions.sneakMinimumPitch,
                this.modOptions.sneakCrawling
            );

            this.list.addHeader(CONTROL);
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
