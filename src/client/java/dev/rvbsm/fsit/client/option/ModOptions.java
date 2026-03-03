package dev.rvbsm.fsit.client.option;

import com.mojang.blaze3d.platform.InputConstants;
import dev.rvbsm.fsit.client.FSitModClient;
import dev.rvbsm.fsit.config.SeatTrait;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ExtraCodecs.TagOrElementLocation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class ModOptions {

    // todo: hear me out, generate from Codec?

    // @formatter:off
    private static final Component USE_SERVER_TOOLTIP = Component.translatable("option.fsit.use_server.tooltip");
    private static final Component SEATS_TRAIT_STATIC_TOOLTIP =  Component.translatable("option.fsit.seats_trait.static.tooltip");
    private static final Component SEATS_TRAIT_ATTACHED_TOOLTIP =  Component.translatable("option.fsit.seats_trait.attached.tooltip");
    private static final Component SEATS_TRAIT_MOVEABLE_TOOLTIP =  Component.translatable("option.fsit.seats_trait.moveable.tooltip");
    private static final Component SEATS_CENTERED_TOOLTIP =  Component.translatable("option.fsit.seats_centered.tooltip");
    private static final Component USE_RANGE_TOOLTIP =  Component.translatable("option.fsit.block_interaction_distance.tooltip");
    private static final Component RIDE_ON_USE_TOOLTIP =  Component.translatable("option.fsit.ride_on_use.tooltip");
    // TODO: list option somehow
    private static final Component USABLE_BLOCKS_TOOLTIP =  Component.translatable("option.fsit.block_interaction_ids.tooltip");
    private static final Component SNEAK_WINDOW_TOOLTIP =  Component.translatable("option.fsit.sneak_window.tooltip");
    private static final Component SNEAK_MINIMUM_PITCH_TOOLTIP =  Component.translatable("option.fsit.sneak_minimum_pitch.tooltip");
    private static final Component SNEAK_CRAWLING_DETECTION_TOOLTIP =  Component.translatable("option.fsit.sneak_crawling.tooltip");

    // todo: tell the server abt it
    public final OptionInstance<Boolean> useServer = OptionInstance.createBoolean(
        "option.fsit.use_server",
        OptionInstance.cachedConstantTooltip(USE_SERVER_TOOLTIP),
        false,
        _ -> {}
    );

    public final OptionInstance<SeatTrait> seatsTrait = new OptionInstance<>(
        "option.fsit.seats_trait",
        value -> switch (value) {
            case STATIC -> Tooltip.create(SEATS_TRAIT_STATIC_TOOLTIP);
            case ATTACHED -> Tooltip.create(SEATS_TRAIT_ATTACHED_TOOLTIP);
            case MOVEABLE ->  Tooltip.create(SEATS_TRAIT_MOVEABLE_TOOLTIP);
        },
        (caption, value) -> Component.translatable("option.fsit.seats_trait." + value.getSerializedName()),
        new OptionInstance.Enum<>(Arrays.asList(SeatTrait.values()), SeatTrait.CODEC),
        SeatTrait.MOVEABLE,
        _ -> {}
    );

    public final OptionInstance<Boolean> seatsCentered = OptionInstance.createBoolean(
        "option.fsit.seats_centered",
        OptionInstance.cachedConstantTooltip(SEATS_CENTERED_TOOLTIP),
        false,
        _ -> {}
    );

    public final OptionInstance<Integer> blockInteractionDistance = new OptionInstance<>(
        "option.fsit.block_interaction_distance",
        OptionInstance.cachedConstantTooltip(USE_RANGE_TOOLTIP),
        Options::genericValueOrOffLabel,
        new OptionInstance.IntRange(0, 5),
        3,
        _ -> {}
    );

    public final OptionInstance<Integer> playerInteractionDistance = new OptionInstance<>(
        "option.fsit.player_interaction_distance",
        OptionInstance.cachedConstantTooltip(RIDE_ON_USE_TOOLTIP),
        Options::genericValueOrOffLabel,
        new OptionInstance.IntRange(0, 5),
        2,
        _ -> {}
    );

    public final List<TagOrElementLocation> blockInteractionIds = new ArrayList<>();

    public final OptionInstance<Integer> sneakWindow = new OptionInstance<>(
        "option.fsit.sneak_window",
        OptionInstance.cachedConstantTooltip(SNEAK_WINDOW_TOOLTIP),
        (caption, value) -> value == 0
            ? Options.genericValueLabel(caption, CommonComponents.OPTION_OFF)
            : Options.genericValueLabel(caption, value),
        new OptionInstance.IntRange(0, 20),
        10,
        _ -> {}
    );

    public final OptionInstance<Integer> sneakMinimumPitch = new OptionInstance<>(
        "option.fsit.sneak_minimum_pitch",
        OptionInstance.cachedConstantTooltip(SNEAK_MINIMUM_PITCH_TOOLTIP),
        (caption, value) -> switch (value) {
            case -90 -> Options.genericValueLabel(caption, Component.translatable("option.fsit.sneak_minimum_pitch.down"));
            case 90 -> Options.genericValueLabel(caption, Component.translatable("options.fsit.sneak_minimum_pitch.always"));
            default -> Options.genericValueLabel(caption, value);
        },
        new OptionInstance.IntRange(-90, 90),
        60,
        _ -> {}
    );

    public final OptionInstance<Boolean> sneakCrawlingDetection = OptionInstance.createBoolean(
        "option.fsit.sneak_crawling_detection",
        OptionInstance.cachedConstantTooltip(SNEAK_CRAWLING_DETECTION_TOOLTIP),
        true,
        _ -> {}
    );

    public final OptionInstance<Integer> hybridKeyWindow = new OptionInstance<>(
        "option.fsit.hybrid_window",
        OptionInstance.noTooltip(),
        Options::genericValueOrOffLabel,
        new OptionInstance.IntRange(0, 10),
        7,
        _ -> {}
    );

    public final OptionInstance<HybridKeyMapping.Mode> hybridSit = new OptionInstance<>(
        "key.fsit.sit",
        OptionInstance.noTooltip(),
        (caption, mode) -> mode.getDisplayName(),
        new OptionInstance.Enum<>(Arrays.asList(HybridKeyMapping.Mode.values()), HybridKeyMapping.Mode.CODEC),
        HybridKeyMapping.Mode.HYBRID,
        _ -> {}
    );

    public final OptionInstance<HybridKeyMapping.Mode> hybridCrawl = new OptionInstance<>(
        "key.fsit.crawl",
        OptionInstance.noTooltip(),
        (caption, mode) -> mode.getDisplayName(),
        new OptionInstance.Enum<>(Arrays.asList(HybridKeyMapping.Mode.values()), HybridKeyMapping.Mode.CODEC),
        HybridKeyMapping.Mode.HYBRID,
        _ -> {}
    );

    public final HybridKeyMapping sitKey = new HybridKeyMapping(
        "key.fsit.sit",
        InputConstants.KEY_RCONTROL,
        KeyMapping.Category.MISC,
        hybridSit::get,
        hybridKeyWindow::get
    );

    public final HybridKeyMapping crawlKey = new HybridKeyMapping(
        "key.fsit.crawl",
        InputConstants.KEY_RALT,
        KeyMapping.Category.MISC,
        hybridCrawl::get,
        hybridKeyWindow::get
    );
    // @formatter:on

    private final Minecraft client;
    private final FSitModClient modClient;
    private boolean serverUseServer = false;

    public ModOptions(Minecraft client, FSitModClient modClient) {
        this.client = client;
        this.modClient = modClient;
    }

    public void load() {
        this.useServer.set(this.modClient.getConfig().useServer());
        this.seatsCentered.set(this.modClient.getConfig().seatsCentered());
        this.blockInteractionIds.addAll(this.modClient.getConfig().blockInteractionIds());
        this.sneakWindow.set(this.modClient.getConfig().sneakWindow());
        this.sneakMinimumPitch.set(this.modClient.getConfig().sneakMinimumPitch());
        this.sneakCrawlingDetection.set(this.modClient.getConfig().sneakCrawlingDetection());
    }

    public void save() {
        // todo: save
    }
}
