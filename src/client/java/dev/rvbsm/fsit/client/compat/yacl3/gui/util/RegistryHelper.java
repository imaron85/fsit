package dev.rvbsm.fsit.client.compat.yacl3.gui.util;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ResourceLocationException;
import net.minecraft.core.DefaultedRegistry;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ItemLike;
import org.jspecify.annotations.Nullable;

import java.util.Comparator;
import java.util.Locale;
import java.util.function.Predicate;
import java.util.stream.Stream;

// TODO use `IdentifierSearcher`?
@Environment(EnvType.CLIENT)
public final class RegistryHelper {

    private static final char HASH = '#';
    private static final RandomSource RANDOM = RandomSource.create();

    private RegistryHelper() {
    }

    public static <T> @Nullable TagKey<T> findTag(Registry<T> registry, final ResourceLocation id) {
        final Iterable<TagKey<T>> tags = registry.getTags()
            //? if >=1.21.2
            .map(HolderSet.Named::key)
            ::iterator;

        for (final var tag : tags) {
            if (tag.location().equals(id)) {
                return tag;
            }
        }

        return null;
    }

    public static <T> boolean isRegistered(final Registry<T> registry, final String stringId) {
        final boolean isTag = !stringId.isEmpty() && stringId.charAt(0) == HASH;

        final ResourceLocation id;
        try {
            id = ResourceLocation.parse(isTag ? stringId.substring(1) : stringId);
        } catch (ResourceLocationException e) {
            return false;
        }

        if (isTag) {
            return registry.getTags()
                //? if >=1.21.2
                .map(HolderSet.Named::key)
                .anyMatch(tag -> tag.location().equals(id));
        } else {
            return registry.containsKey(id);
        }
    }

    public static <T> T getFromId(Registry<T> registry, String stringId, @Nullable T def) {
        try {
            if (!stringId.isEmpty() && stringId.charAt(0) != HASH) {
                final ResourceLocation entryId = ResourceLocation.parse(stringId);
                if (registry.containsKey(entryId)) {
                    return registry.getValue(entryId);
                }
            } else {
                final ResourceLocation tagId = ResourceLocation.parse(stringId.substring(1));
                final TagKey<T> tag = findTag(registry, tagId);

                if (tag != null) {
                    return registry.getRandomElementOf(tag, RANDOM).map(Holder::value).orElse(def);
                }
            }

        } catch (ResourceLocationException ignored) {
        }

        return def;
    }

    public static <T> T getFromId(DefaultedRegistry<T> registry, String stringId) {
        return getFromId(registry, stringId, registry.getValue(registry.getDefaultKey()));
    }

    public static <T extends ItemLike> Stream<String> getMatchingIdentifiers(final DefaultedRegistry<T> registry, String input) {
        if (!input.isEmpty() && input.charAt(0) == HASH) {
            input = input.substring(1);

            return registry.getTags()
                //? if >=1.21.2
                .map(HolderSet.Named::key)
                .map(TagKey::location)
                .filter(getIdentifierPredicate(input))
                .sorted(compareIds(input))
                .map(id -> HASH + id.toString());
        } else {
            return Stream.concat(
                registry.keySet().stream()
                    .filter(getIdentifierPredicate(input))
                    .sorted(compareIds(input))
                    .map(ResourceLocation::toString),
                registry.getTags()
                    //? if >=1.21.2
                    .map(HolderSet.Named::key)
                    .map(TagKey::location)
                    .filter(getIdentifierPredicate(input))
                    .sorted(compareIds(input))
                    .map(id -> HASH + id.toString())
            );
        }
    }

    private static Predicate<ResourceLocation> getIdentifierPredicate(String id) {
        final int sep = id.indexOf(ResourceLocation.NAMESPACE_SEPARATOR);

        if (sep == -1) {
            return idx -> idx.getPath().contains(id);
        } else {
            final String string = id.substring(0, sep).trim();
            final String string2 = id.substring(sep + 1).trim();

            return idx -> idx.getNamespace().contains(string) && idx.getPath().contains(string2);
        }
    }

    private static Comparator<ResourceLocation> compareIds(final String input) {
        final int sep = input.indexOf(ResourceLocation.NAMESPACE_SEPARATOR);

        return (left, right) -> {
            final String path = (sep == -1 ? input : input.substring(sep + 1)).toLowerCase(Locale.ROOT);
            final boolean id1StartsWith = left.getPath().toLowerCase(Locale.ROOT).startsWith(path);
            final boolean id2StartsWith = right.getPath().toLowerCase(Locale.ROOT).startsWith(path);
            if (id1StartsWith) {
                if (id2StartsWith) {
                    return left.compareTo(right);
                }

                return -1;
            }

            if (id2StartsWith) {
                return 1;
            }

            return left.compareTo(right);
        };
    }
}
