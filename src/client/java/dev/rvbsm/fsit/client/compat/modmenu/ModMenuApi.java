package dev.rvbsm.fsit.client.compat.modmenu;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import dev.rvbsm.fsit.client.compat.ClientModCompat;
import dev.rvbsm.fsit.client.gui.screen.ModOptionsScreen;
import dev.rvbsm.fsit.client.gui.screen.YetAnotherConfigLibScreen;

public final class ModMenuApi implements com.terraformersmc.modmenu.api.ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        if (ClientModCompat.YACL.isLoaded()) {
            return YetAnotherConfigLibScreen::generateScreen;
        }

        return ModOptionsScreen::new;
    }
}
