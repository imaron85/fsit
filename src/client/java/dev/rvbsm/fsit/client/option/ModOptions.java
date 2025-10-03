//~ keybinding_category

package dev.rvbsm.fsit.client.option;

import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.option.SimpleOption;
import net.minecraft.client.util.InputUtil;

import java.util.Arrays;

public class ModOptions {

    public static final SimpleOption<Integer> hybridModeWindow = new SimpleOption<>(
        "key.fsit.hybrid_window",
        SimpleOption.emptyTooltip(),
        GameOptions::getGenericValueOrOffText,
        new SimpleOption.ValidatingIntSliderCallbacks(0, 10),
        7,
        (t) -> {}
    );
    public static final SimpleOption<HybridKeyBinding.Mode> sitMode = new SimpleOption<>(
        "key.fsit.sit",
        SimpleOption.emptyTooltip(),
        SimpleOption.enumValueText(),
        new SimpleOption.PotentialValuesBasedCallbacks<>(Arrays.asList(HybridKeyBinding.Mode.values()), HybridKeyBinding.Mode.CODEC),
        HybridKeyBinding.Mode.HYBRID,
        (m) -> {}
    );
    public static final SimpleOption<HybridKeyBinding.Mode> crawlMode = new SimpleOption<>(
        "key.fsit.crawl",
        SimpleOption.emptyTooltip(),
        SimpleOption.enumValueText(),
        new SimpleOption.PotentialValuesBasedCallbacks<>(Arrays.asList(HybridKeyBinding.Mode.values()), HybridKeyBinding.Mode.CODEC),
        HybridKeyBinding.Mode.HYBRID,
        (m) -> {}
    );

    public static final KeyBinding sitKey = new HybridKeyBinding(
        "key.fsit.sit",
        InputUtil.GLFW_KEY_RIGHT_CONTROL,
        KeyBinding.Category.MISC,
        sitMode::getValue,
        hybridModeWindow::getValue
    );
    public static final KeyBinding crawlKey = new HybridKeyBinding(
        "key.fsit.crawl",
        InputUtil.GLFW_KEY_RIGHT_CONTROL,
        KeyBinding.Category.MISC,
        crawlMode::getValue,
        hybridModeWindow::getValue
    );
}
