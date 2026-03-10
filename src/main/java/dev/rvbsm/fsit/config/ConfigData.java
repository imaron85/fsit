package dev.rvbsm.fsit.config;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
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
    SeatTrait seatsTrait,
    boolean seatsCentered,

    boolean blockInteraction,
    // not HolderSet<Block> because we unwrap multiple tags into single HolderSet.Direct and we need to store tags
    // todo: list of BlockPredicate?
    List<ExtraCodecs.TagOrElementLocation> blockInteractionIds,

    int ridingMinimumYaw,

    int sneakWindow,
    int sneakMinimumPitch,
    boolean sneakCrawling,

    CommandConfig commands
) {

    public static final Codec<ConfigData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        SeatTrait.CODEC.fieldOf("seats-trait").forGetter(ConfigData::seatsTrait),
        Codec.BOOL.fieldOf("seats-centered").forGetter(ConfigData::seatsCentered),

        Codec.BOOL.fieldOf("block-interaction").forGetter(ConfigData::blockInteraction),
        ExtraCodecs.TAG_OR_ELEMENT_ID.listOf().fieldOf("block-interaction-ids").forGetter(ConfigData::blockInteractionIds),

        Codec.intRange(0, 360).fieldOf("riding-minimum-yaw").forGetter(ConfigData::ridingMinimumYaw),

        Codec.intRange(0, 20).fieldOf("sneak-window").forGetter(ConfigData::sneakWindow),
        Codec.intRange(-90, 90).fieldOf("sneak-minimum-pitch").forGetter(ConfigData::sneakMinimumPitch),
        Codec.BOOL.fieldOf("sneak-crawling").forGetter(ConfigData::sneakCrawling),

        CommandConfig.CODEC.fieldOf("commands").forGetter(ConfigData::commands)
    ).apply(instance, ConfigData::new));

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
}
