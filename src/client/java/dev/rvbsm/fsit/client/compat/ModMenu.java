package dev.rvbsm.fsit.client.compat;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import dev.rvbsm.fsit.client.compat.yacl3.api.YetAnotherConfigLib;
import net.fabricmc.loader.api.FabricLoader;

public class ModMenu implements ModMenuApi {

    public static final boolean YACL = FabricLoader.getInstance().isModLoaded("yet_another_config_lib_v3");

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return (screen) -> {
            if (YACL) {
                return YetAnotherConfigLib.generateScreen(screen);
            }

            // todo maybe others too

            return null;
        };
    }
}
