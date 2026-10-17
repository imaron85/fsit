package dev.rvbsm.fsit.config;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.rvbsm.fsit.serialization.CommentCodec;
import dev.rvbsm.fsit.serialization.FSitCodecs;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.block.Block;
import org.slf4j.Logger;

import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;

public record ConfigData(
        boolean useServer,
        SeatTrait seatsTrait,
        boolean seatsCentered,

        boolean blockInteraction,
        // not HolderSet<Block> because we unwrap multiple tags into single HolderSet.Direct and we need to store tags
        // todo: list of BlockPredicate?
        List<ExtraCodecs.TagOrElementLocation> blockInteractionIds,

        int ridingMinimumYaw,
        RidingTeleportMode ridingTeleportMode,

        int sneakWindow,
        int sneakMinimumPitch,
        boolean sneakCrawling,

        CommandConfig commands) {

    // bump on breaking schema change — loader backs up old file and writes fresh defaults
    public static final int CURRENT_VERSION = 2;

    private ConfigData(Boolean useServer, Sitting sitting, Riding riding, Sneak sneak, CommandConfig commands) {
        this(
                useServer,
                sitting.behavior(),
                sitting.centered(),
                sitting.enabled(),
                sitting.blocks(),
                riding.enabled() ? riding.maxYawDifference() : 0,
                riding.teleportMode(),
                sneak.enabled() ? sneak.windowTicks() : 0,
                sneak.minPitchDown(),
                sneak.preferCrawlInTightSpace(),
                commands);
    }

    private record Sitting(
            boolean enabled, SeatTrait behavior, boolean centered, List<ExtraCodecs.TagOrElementLocation> blocks) {

        static final Sitting DEFAULT = new Sitting(true, SeatTrait.MOVEABLE, false, List.of());
        static final List<ExtraCodecs.TagOrElementLocation> DEFAULT_BLOCKS = List.of(
                new ExtraCodecs.TagOrElementLocation(Identifier.parse("minecraft:slabs"), true),
                new ExtraCodecs.TagOrElementLocation(Identifier.parse("minecraft:stairs"), true),
                new ExtraCodecs.TagOrElementLocation(Identifier.parse("minecraft:logs"), true),
                new ExtraCodecs.TagOrElementLocation(Identifier.parse("minecraft:fences"), true),
                new ExtraCodecs.TagOrElementLocation(Identifier.parse("minecraft:walls"), true));

        static final Codec<Sitting> CODEC = RecordCodecBuilder.create(i -> i.group(
                        FSitCodecs.config("enabled", Codec.BOOL, true, Sitting::enabled),
                        FSitCodecs.config("behavior", SeatTrait.CODEC, SeatTrait.MOVEABLE, Sitting::behavior),
                        FSitCodecs.config("centered", Codec.BOOL, false, Sitting::centered),
                        FSitCodecs.config(
                                "blocks", ExtraCodecs.TAG_OR_ELEMENT_ID.listOf(), DEFAULT_BLOCKS, Sitting::blocks))
                .apply(i, Sitting::new));

        Sitting(ConfigData d) {
            this(d.blockInteraction(), d.seatsTrait(), d.seatsCentered(), d.blockInteractionIds());
        }
    }

    private record Riding(boolean enabled, int maxYawDifference, RidingTeleportMode teleportMode) {
        static final Riding DEFAULT = new Riding(true, 180, RidingTeleportMode.ENHANCED);
        static final Codec<Riding> CODEC = RecordCodecBuilder.create(i -> i.group(
                        FSitCodecs.config("enabled", Codec.BOOL, true, Riding::enabled),
                        FSitCodecs.config("max-yaw-difference", Codec.intRange(0, 180), 180, Riding::maxYawDifference),
                        FSitCodecs.config(
                                "teleport-mode",
                                RidingTeleportMode.CODEC,
                                RidingTeleportMode.ENHANCED,
                                Riding::teleportMode))
                .apply(i, Riding::new));

        Riding(ConfigData d) {
            this(
                    d.ridingMinimumYaw() > 0,
                    d.ridingMinimumYaw() > 0 ? d.ridingMinimumYaw() : 180,
                    d.ridingTeleportMode());
        }
    }

    private record Sneak(boolean enabled, int windowTicks, int minPitchDown, boolean preferCrawlInTightSpace) {
        static final Sneak DEFAULT = new Sneak(true, 10, 60, true);

        static final Codec<Sneak> CODEC = RecordCodecBuilder.create(i -> i.group(
                        FSitCodecs.config("enabled", Codec.BOOL, true, Sneak::enabled),
                        FSitCodecs.config("window-ticks", Codec.intRange(1, 20), 10, Sneak::windowTicks),
                        FSitCodecs.config("min-pitch-down", Codec.intRange(0, 90), 60, Sneak::minPitchDown),
                        FSitCodecs.config(
                                "prefer-crawl-in-tight-space", Codec.BOOL, true, Sneak::preferCrawlInTightSpace))
                .apply(i, Sneak::new));

        Sneak(ConfigData d) {
            this(
                    d.sneakWindow() > 0,
                    d.sneakWindow() > 0 ? d.sneakWindow() : 10,
                    d.sneakMinimumPitch(),
                    d.sneakCrawling());
        }
    }

    public static final MapCodec<ConfigData> DATA_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
                    FSitCodecs.config(
                            "use-server-config",
                            Codec.BOOL,
                            false,
                            ConfigData::useServer,
                            "If true, clients with the mod installed will use this server config instead of their"
                                    + " own."),
                    FSitCodecs.config(
                            "sitting",
                            Sitting.CODEC,
                            Sitting.DEFAULT,
                            Sitting::new,
                            "Sitting on blocks (e.g. slabs, stairs)."),
                    FSitCodecs.config(
                            "riding",
                            Riding.CODEC,
                            Riding.DEFAULT,
                            Riding::new,
                            "Riding other players (interact with a player to sit on their head).",
                            "teleport-mode is server-wide (ops only). Possible values:",
                            "  vanilla   -> let teleports happen as-is (passengers ride along" + " same-dim,",
                            "               stay behind on cross-dim — and may crash without" + " 'enhanced')",
                            "  disabled  -> drop passengers before any teleport",
                            "  enhanced  -> carry passengers along, including across dimensions" + " (recommended)"),
                    FSitCodecs.config(
                            "sneak-activation",
                            Sneak.CODEC,
                            Sneak.DEFAULT,
                            Sneak::new,
                            "Double-sneak shortcuts for entering sit/crawl poses."),
                    FSitCodecs.config(
                            "commands",
                            CommandConfig.CODEC,
                            CommandConfig.DEFAULT,
                            ConfigData::commands,
                            "Commands.",
                            "  true       -> enabled with default name",
                            "  false      -> command is not registered",
                            "  \"name\"     -> enabled with a custom name (e.g. lay: sleep)",
                            "A server restart is required for command changes to take effect."))
            .apply(i, ConfigData::new));

    public static final Codec<ConfigData> CODEC = RecordCodecBuilder.create(i -> i.group(
                    CommentCodec.of(Codec.INT.fieldOf("version"), "Do not edit.")
                            .forGetter(_ -> CURRENT_VERSION),
                    DATA_CODEC.forGetter(Function.identity()))
            // TODO: validate version here?
            .apply(i, (_, c) -> c));

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Cache<ConfigData, HolderSet<Block>> INTERACTABLE_BLOCKS_CACHE =
            CacheBuilder.newBuilder().weakKeys().build();

    public HolderSet<Block> collectInteractableBlocks(RegistryAccess registry) {
        try {
            return INTERACTABLE_BLOCKS_CACHE.get(this, () -> {
                var blockRegistry = registry.lookupOrThrow(Registries.BLOCK);
                var blockList = new HashSet<Holder<Block>>();
                for (var location : this.blockInteractionIds) {
                    if (location.tag()) {
                        blockRegistry
                                .getOrThrow(TagKey.create(Registries.BLOCK, location.id()))
                                .forEach(blockList::add);
                    } else {
                        blockList.add(blockRegistry.getOrThrow(ResourceKey.create(Registries.BLOCK, location.id())));
                    }
                }

                return HolderSet.direct(List.copyOf(blockList));
            });
        } catch (ExecutionException e) {
            LOGGER.warn("rip", e);
            return HolderSet.empty();
        }
    }

    public ClientConfig makeClientConfig() {
        return new ClientConfig(this.useServer(), this.seatsTrait());
    }

    public ConfigData withRidingMinimumYaw(int angle) {
        if (angle == this.ridingMinimumYaw) return this;
        return new ConfigData(
                this.useServer,
                this.seatsTrait,
                this.seatsCentered,
                this.blockInteraction,
                this.blockInteractionIds,
                angle,
                this.ridingTeleportMode,
                this.sneakWindow,
                this.sneakMinimumPitch,
                this.sneakCrawling,
                this.commands);
    }

    public ConfigData withRidingTeleportMode(RidingTeleportMode mode) {
        if (mode == this.ridingTeleportMode) return this;
        return new ConfigData(
                this.useServer,
                this.seatsTrait,
                this.seatsCentered,
                this.blockInteraction,
                this.blockInteractionIds,
                this.ridingMinimumYaw,
                mode,
                this.sneakWindow,
                this.sneakMinimumPitch,
                this.sneakCrawling,
                this.commands);
    }
}
