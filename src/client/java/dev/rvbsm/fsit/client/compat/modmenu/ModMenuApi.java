package dev.rvbsm.fsit.client.compat.modmenu;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import dev.rvbsm.fsit.client.gui.screen.ModOptionsScreen;

public final class ModMenuApi implements com.terraformersmc.modmenu.api.ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return ModOptionsScreen::new;
    }
}
