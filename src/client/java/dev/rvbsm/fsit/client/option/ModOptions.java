//~ keybinding_category

package dev.rvbsm.fsit.client.option;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.option.SimpleOption;
import net.minecraft.client.util.InputUtil;

import java.util.Arrays;

public class ModOptions {

    private final MinecraftClient client;

    public ModOptions(MinecraftClient client) {
        this.client = client;
    }

    public final SimpleOption<Integer> hybridModeWindow = new SimpleOption<>(
        "key.fsit.hybrid_window",
        SimpleOption.emptyTooltip(),
        GameOptions::getGenericValueOrOffText,
        new SimpleOption.ValidatingIntSliderCallbacks(0, 10),
        7,
        (t) -> {}
    );
    public final SimpleOption<HybridKeyBinding.Mode> sitMode = new SimpleOption<>(
        "key.fsit.sit",
        SimpleOption.emptyTooltip(),
        SimpleOption.enumValueText(),
        new SimpleOption.PotentialValuesBasedCallbacks<>(Arrays.asList(HybridKeyBinding.Mode.values()), HybridKeyBinding.Mode.CODEC),
        HybridKeyBinding.Mode.HYBRID,
        (m) -> {}
    );
    public final SimpleOption<HybridKeyBinding.Mode> crawlMode = new SimpleOption<>(
        "key.fsit.crawl",
        SimpleOption.emptyTooltip(),
        SimpleOption.enumValueText(),
        new SimpleOption.PotentialValuesBasedCallbacks<>(Arrays.asList(HybridKeyBinding.Mode.values()), HybridKeyBinding.Mode.CODEC),
        HybridKeyBinding.Mode.HYBRID,
        (m) -> {}
    );

    public final KeyBinding sitKey = new HybridKeyBinding(
        "key.fsit.sit",
        InputUtil.GLFW_KEY_RIGHT_CONTROL,
        KeyBinding.Category.MISC,
        sitMode::getValue,
        hybridModeWindow::getValue
    );
    public final KeyBinding crawlKey = new HybridKeyBinding(
        "key.fsit.crawl",
        InputUtil.GLFW_KEY_RIGHT_ALT,
        KeyBinding.Category.MISC,
        crawlMode::getValue,
        hybridModeWindow::getValue
    );
}
