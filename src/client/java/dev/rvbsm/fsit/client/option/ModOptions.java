//~ keybinding_category

package dev.rvbsm.fsit.client.option;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;

import java.util.Arrays;

public class ModOptions {

    public final OptionInstance<Integer> hybridModeWindow = new OptionInstance<>(
        "key.fsit.hybrid_window",
        OptionInstance.noTooltip(),
        Options::genericValueOrOffLabel,
        new OptionInstance.IntRange(0, 10),
        7,
        (t) -> {}
    );
    public final OptionInstance<HybridKeyBinding.Mode> sitMode = new OptionInstance<>(
        "key.fsit.sit",
        OptionInstance.noTooltip(),
        OptionInstance.forOptionEnum(),
        new OptionInstance.Enum<>(Arrays.asList(HybridKeyBinding.Mode.values()), HybridKeyBinding.Mode.CODEC),
        HybridKeyBinding.Mode.HYBRID,
        (m) -> {}
    );
    public final OptionInstance<HybridKeyBinding.Mode> crawlMode = new OptionInstance<>(
        "key.fsit.crawl",
        OptionInstance.noTooltip(),
        OptionInstance.forOptionEnum(),
        new OptionInstance.Enum<>(Arrays.asList(HybridKeyBinding.Mode.values()), HybridKeyBinding.Mode.CODEC),
        HybridKeyBinding.Mode.HYBRID,
        (m) -> {}
    );
    public final KeyMapping sitKey = new HybridKeyBinding(
        "key.fsit.sit",
        InputConstants.KEY_RCONTROL,
        KeyMapping.Category.MISC,
        sitMode::get,
        hybridModeWindow::get
    );
    public final KeyMapping crawlKey = new HybridKeyBinding(
        "key.fsit.crawl",
        InputConstants.KEY_RALT,
        KeyMapping.Category.MISC,
        crawlMode::get,
        hybridModeWindow::get
    );

    private final Minecraft client;

    public ModOptions(Minecraft client) {
        this.client = client;
    }
}
