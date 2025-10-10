package dev.rvbsm.fsit.client.compat.yacl3.gui.util;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.item.ItemConvertible;
import net.minecraft.registry.DefaultedRegistry;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.InvalidIdentifierException;
import net.minecraft.util.math.random.Random;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.Locale;
import java.util.function.Predicate;
import java.util.stream.Stream;

// TODO use `IdentifierSearcher`?
@Environment(EnvType.CLIENT)
public final class RegistryHelper {

    private static final char HASH = '#';
    private static final Random RANDOM = Random.create();

    private RegistryHelper() {
    }

    public static <T> @Nullable TagKey<T> findTag(Registry<T> registry, final Identifier id) {
        final Iterable<TagKey<T>> tags = registry.streamTags()
            //? if >=1.21.2
            .map(RegistryEntryList.Named::getTag)
            ::iterator;

        for (final var tag : tags) {
            if (tag.id().equals(id)) {
                return tag;
            }
        }

        return null;
    }

    public static <T> boolean isRegistered(final Registry<T> registry, final String stringId) {
        final boolean isTag = !stringId.isEmpty() && stringId.charAt(0) == HASH;

        final Identifier id;
        try {
            id = Identifier.of(isTag ? stringId.substring(1) : stringId);
        } catch (InvalidIdentifierException e) {
            return false;
        }

        if (isTag) {
            return registry.streamTags()
                //? if >=1.21.2
                .map(RegistryEntryList.Named::getTag)
                .anyMatch(tag -> tag.id().equals(id));
        } else {
            return registry.containsId(id);
        }
    }

    public static <T> T getFromId(Registry<T> registry, String stringId, @Nullable T def) {
        try {
            if (!stringId.isEmpty() && stringId.charAt(0) != HASH) {
                final Identifier entryId = Identifier.of(stringId);
                if (registry.containsId(entryId)) {
                    return registry.get(entryId);
                }
            } else {
                final Identifier tagId = Identifier.of(stringId.substring(1));
                final TagKey<T> tag = findTag(registry, tagId);

                if (tag != null) {
                    return registry.getRandomEntry(tag, RANDOM).map(RegistryEntry::value).orElse(def);
                }
            }

        } catch (InvalidIdentifierException ignored) {
        }

        return def;
    }

    public static <T> T getFromId(DefaultedRegistry<T> registry, String stringId) {
        return getFromId(registry, stringId, registry.get(registry.getDefaultId()));
    }

    public static <T extends ItemConvertible> Stream<String> getMatchingIdentifiers(final DefaultedRegistry<T> registry, String input) {
        if (!input.isEmpty() && input.charAt(0) == HASH) {
            input = input.substring(1);

            return registry.streamTags()
                //? if >=1.21.2
                .map(RegistryEntryList.Named::getTag)
                .map(TagKey::id)
                .filter(getIdentifierPredicate(input))
                .sorted(compareIds(input))
                .map(id -> HASH + id.toString());
        } else {
            return Stream.concat(
                registry.getIds().stream()
                    .filter(getIdentifierPredicate(input))
                    .sorted(compareIds(input))
                    .map(Identifier::toString),
                registry.streamTags()
                    //? if >=1.21.2
                    .map(RegistryEntryList.Named::getTag)
                    .map(TagKey::id)
                    .filter(getIdentifierPredicate(input))
                    .sorted(compareIds(input))
                    .map(id -> HASH + id.toString())
            );
        }
    }

    private static Predicate<Identifier> getIdentifierPredicate(String id) {
        final int sep = id.indexOf(Identifier.NAMESPACE_SEPARATOR);

        if (sep == -1) {
            return idx -> idx.getPath().contains(id);
        } else {
            final String string = id.substring(0, sep).trim();
            final String string2 = id.substring(sep + 1).trim();

            return idx -> idx.getNamespace().contains(string) && idx.getPath().contains(string2);
        }
    }

    private static Comparator<Identifier> compareIds(final String input) {
        final int sep = input.indexOf(Identifier.NAMESPACE_SEPARATOR);

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
