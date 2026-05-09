package dev.rvbsm.fsit.api;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.ExtraCodecs.TagOrElementLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.slf4j.Logger;

import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ExecutionException;

public record PlayerSettings(
    List<TagOrElementLocation> idleHandItems
) {

    private static final Logger LOGGER = LogUtils.getLogger();

    public static final List<TagOrElementLocation> DEFAULT_IDLE_HAND_ITEMS = List.of(
        new TagOrElementLocation(Identifier.parse("minecraft:torch"), false),
        new TagOrElementLocation(Identifier.parse("minecraft:totem_of_undying"), false)
    );

    public static final PlayerSettings DEFAULT = new PlayerSettings(DEFAULT_IDLE_HAND_ITEMS);

    public static final Codec<PlayerSettings> CODEC = RecordCodecBuilder.create(i -> i.group(
        ExtraCodecs.TAG_OR_ELEMENT_ID.listOf()
            .optionalFieldOf("idle_hand_items", DEFAULT_IDLE_HAND_ITEMS)
            .forGetter(PlayerSettings::idleHandItems)
    ).apply(i, PlayerSettings::new));

    private static final StreamCodec<ByteBuf, TagOrElementLocation> ENTRY_STREAM_CODEC = StreamCodec.composite(
        Identifier.STREAM_CODEC, TagOrElementLocation::id,
        ByteBufCodecs.BOOL, TagOrElementLocation::tag,
        TagOrElementLocation::new
    );

    public static final StreamCodec<ByteBuf, PlayerSettings> STREAM_CODEC = StreamCodec.composite(
        ENTRY_STREAM_CODEC.apply(ByteBufCodecs.list()), PlayerSettings::idleHandItems,
        PlayerSettings::new
    );

    private static final Cache<PlayerSettings, HolderSet<Item>> IDLE_ITEMS_CACHE = CacheBuilder.newBuilder()
        .weakKeys()
        .build();

    public PlayerSettings withIdleHandItems(List<TagOrElementLocation> items) {
        return new PlayerSettings(List.copyOf(items));
    }

    public HolderSet<Item> collectIdleHandItems(RegistryAccess registry) {
        try {
            return IDLE_ITEMS_CACHE.get(
                this, () -> {
                    if (this.idleHandItems.isEmpty()) {
                        return HolderSet.empty();
                    }
                    var itemRegistry = registry.lookupOrThrow(Registries.ITEM);
                    var items = new HashSet<Holder<Item>>();
                    for (var location : this.idleHandItems) {
                        if (location.tag()) {
                            itemRegistry.get(TagKey.create(Registries.ITEM, location.id()))
                                .ifPresent(set -> set.forEach(items::add));
                        } else {
                            itemRegistry.get(ResourceKey.create(Registries.ITEM, location.id()))
                                .ifPresent(items::add);
                        }
                    }
                    return HolderSet.direct(List.copyOf(items));
                }
            );
        } catch (ExecutionException e) {
            LOGGER.warn("Failed to resolve idle hand items", e);
            return HolderSet.empty();
        }
    }

    public boolean handsAreIdle(Player player, RegistryAccess registry) {
        var idle = this.collectIdleHandItems(registry);
        return isHandIdle(player.getMainHandItem(), idle) && isHandIdle(player.getOffhandItem(), idle);
    }

    private static boolean isHandIdle(ItemStack stack, HolderSet<Item> idle) {
        return stack.isEmpty() || idle.contains(stack.typeHolder());
    }
}
