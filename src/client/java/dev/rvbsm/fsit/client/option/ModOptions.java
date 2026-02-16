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
    public final OptionInstance<HybridKeyMapping.Mode> sitMode = new OptionInstance<>(
        "key.fsit.sit",
        OptionInstance.noTooltip(),
        (caption, value) -> Options.genericValueLabel(caption, value.getDisplayName()),
        new OptionInstance.Enum<>(Arrays.asList(HybridKeyMapping.Mode.values()), HybridKeyMapping.Mode.CODEC),
        HybridKeyMapping.Mode.HYBRID,
        (m) -> {}
    );
    public final OptionInstance<HybridKeyMapping.Mode> crawlMode = new OptionInstance<>(
        "key.fsit.crawl",
        OptionInstance.noTooltip(),
        (caption, mode) -> Options.genericValueLabel(caption, mode.getDisplayName()),
        new OptionInstance.Enum<>(Arrays.asList(HybridKeyMapping.Mode.values()), HybridKeyMapping.Mode.CODEC),
        HybridKeyMapping.Mode.HYBRID,
        (m) -> {}
    );
    public final KeyMapping sitKey = new HybridKeyMapping(
        "key.fsit.sit",
        InputConstants.KEY_RCONTROL,
        KeyMapping.Category.MISC,
        sitMode::get,
        hybridModeWindow::get
    );
    public final KeyMapping crawlKey = new HybridKeyMapping(
        "key.fsit.crawl",
        InputConstants.KEY_RALT,
        KeyMapping.Category.MISC,
        crawlMode::get,
        hybridModeWindow::get
    );

    private final Minecraft client;

    public ModOptions(final Minecraft client) {
        this.client = client;
    }
}
