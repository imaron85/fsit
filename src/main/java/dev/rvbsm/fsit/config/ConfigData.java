package dev.rvbsm.fsit.config;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.block.Block;
import org.slf4j.Logger;

import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ExecutionException;

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

    CommandConfig commands
) {

    // bump on breaking schema change — loader backs up old file and writes fresh defaults
    public static final int CURRENT_VERSION = 2;

    // like optionalFieldOf but always emits the field on encode (self-documenting yaml)
    private static <A> MapCodec<A> field(Codec<A> codec, String name, A def) {
        return codec.optionalFieldOf(name).xmap(opt -> opt.orElse(def), Optional::of);
    }

    private record Sitting(boolean enabled, SeatTrait behavior, boolean centered, List<ExtraCodecs.TagOrElementLocation> blocks) {
        static final Codec<Sitting> CODEC = RecordCodecBuilder.create(i -> i.group(
            field(Codec.BOOL, "enabled", true).forGetter(Sitting::enabled),
            field(SeatTrait.CODEC, "behavior", SeatTrait.MOVEABLE).forGetter(Sitting::behavior),
            field(Codec.BOOL, "centered", false).forGetter(Sitting::centered),
            field(ExtraCodecs.TAG_OR_ELEMENT_ID.listOf(), "blocks", List.of(
                new ExtraCodecs.TagOrElementLocation(net.minecraft.resources.Identifier.parse("minecraft:slabs"), true),
                new ExtraCodecs.TagOrElementLocation(net.minecraft.resources.Identifier.parse("minecraft:stairs"), true),
                new ExtraCodecs.TagOrElementLocation(net.minecraft.resources.Identifier.parse("minecraft:logs"), true),
                new ExtraCodecs.TagOrElementLocation(net.minecraft.resources.Identifier.parse("minecraft:fences"), true),
                new ExtraCodecs.TagOrElementLocation(net.minecraft.resources.Identifier.parse("minecraft:walls"), true)
            )).forGetter(Sitting::blocks)
        ).apply(i, Sitting::new));
    }

    private record Riding(boolean enabled, int maxYawDifference, RidingTeleportMode teleportMode) {
        static final Codec<Riding> CODEC = RecordCodecBuilder.create(i -> i.group(
            field(Codec.BOOL, "enabled", true).forGetter(Riding::enabled),
            field(Codec.intRange(0, 180), "max-yaw-difference", 180).forGetter(Riding::maxYawDifference),
            field(RidingTeleportMode.CODEC, "teleport-mode", RidingTeleportMode.ENHANCED).forGetter(Riding::teleportMode)
        ).apply(i, Riding::new));
    }

    private record Sneak(boolean enabled, int windowTicks, int minPitchDown, boolean preferCrawlInTightSpace) {
        static final Codec<Sneak> CODEC = RecordCodecBuilder.create(i -> i.group(
            field(Codec.BOOL, "enabled", true).forGetter(Sneak::enabled),
            field(Codec.intRange(1, 20), "window-ticks", 10).forGetter(Sneak::windowTicks),
            field(Codec.intRange(0, 90), "min-pitch-down", 60).forGetter(Sneak::minPitchDown),
            field(Codec.BOOL, "prefer-crawl-in-tight-space", true).forGetter(Sneak::preferCrawlInTightSpace)
        ).apply(i, Sneak::new));
    }

    public static final Codec<ConfigData> CODEC = RecordCodecBuilder.create(i -> i.group(
        Codec.INT.fieldOf("version").forGetter(_ -> CURRENT_VERSION),
        field(Codec.BOOL, "use-server-config", false).forGetter(ConfigData::useServer),
        field(Sitting.CODEC, "sitting", new Sitting(true, SeatTrait.MOVEABLE, false, List.of())).forGetter(d ->
            new Sitting(d.blockInteraction(), d.seatsTrait(), d.seatsCentered(), d.blockInteractionIds())),
        field(Riding.CODEC, "riding", new Riding(true, 180, RidingTeleportMode.ENHANCED)).forGetter(d ->
            new Riding(d.ridingMinimumYaw() > 0, d.ridingMinimumYaw() > 0 ? d.ridingMinimumYaw() : 180, d.ridingTeleportMode())),
        field(Sneak.CODEC, "sneak-activation", new Sneak(true, 10, 60, true)).forGetter(d ->
            new Sneak(d.sneakWindow() > 0, d.sneakWindow() > 0 ? d.sneakWindow() : 10, d.sneakMinimumPitch(), d.sneakCrawling())),
        field(CommandConfig.CODEC, "commands", new CommandConfig("fsit", "sit", "crawl", "lay"))
            .forGetter(ConfigData::commands)
    ).apply(i, (_, useServer, sitting, riding, sneak, commands) -> new ConfigData(
        useServer,
        sitting.behavior(), sitting.centered(),
        sitting.enabled(), sitting.blocks(),
        riding.enabled() ? riding.maxYawDifference() : 0, riding.teleportMode(),
        sneak.enabled() ? sneak.windowTicks() : 0, sneak.minPitchDown(), sneak.preferCrawlInTightSpace(),
        commands
    )));

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Cache<ConfigData, HolderSet<Block>> INTERACTABLE_BLOCKS_CACHE = CacheBuilder.newBuilder()
        .weakKeys()
        .build();

    public HolderSet<Block> collectInteractableBlocks(RegistryAccess registry) {
        try {
            return INTERACTABLE_BLOCKS_CACHE.get(
                this, () -> {
                    var blockRegistry = registry.lookupOrThrow(Registries.BLOCK);
                    var blockList = new HashSet<Holder<Block>>();
                    for (var location : this.blockInteractionIds) {
                        if (location.tag()) {
                            blockRegistry.getOrThrow(TagKey.create(Registries.BLOCK, location.id())).forEach(
                                blockList::add);
                        } else {
                            blockList.add(
                                blockRegistry.getOrThrow(ResourceKey.create(Registries.BLOCK, location.id())));
                        }
                    }

                    return HolderSet.direct(List.copyOf(blockList));
                }
            );
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
            this.useServer, this.seatsTrait, this.seatsCentered,
            this.blockInteraction, this.blockInteractionIds,
            angle, this.ridingTeleportMode,
            this.sneakWindow, this.sneakMinimumPitch, this.sneakCrawling,
            this.commands
        );
    }

    public ConfigData withRidingTeleportMode(RidingTeleportMode mode) {
        if (mode == this.ridingTeleportMode) return this;
        return new ConfigData(
            this.useServer, this.seatsTrait, this.seatsCentered,
            this.blockInteraction, this.blockInteractionIds,
            this.ridingMinimumYaw, mode,
            this.sneakWindow, this.sneakMinimumPitch, this.sneakCrawling,
            this.commands
        );
    }
}
