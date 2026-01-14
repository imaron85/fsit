package dev.rvbsm.fsit.client.compat.modmenu;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;

public class ModMenuApi implements com.terraformersmc.modmenu.api.ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return (screen) -> null;
    }
}
