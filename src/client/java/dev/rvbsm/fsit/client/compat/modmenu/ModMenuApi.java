package dev.rvbsm.fsit.client.compat.modmenu;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import dev.rvbsm.fsit.client.compat.ClientModCompat;
import dev.rvbsm.fsit.client.compat.yacl3.api.YetAnotherConfigLib;

public class ModMenuApi implements com.terraformersmc.modmenu.api.ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return (screen) -> {
            if (ClientModCompat.YACL.isLoaded()) {
                return YetAnotherConfigLib.generateScreen(screen);
            }

            // todo maybe others too

            return null;
        };
    }
}
