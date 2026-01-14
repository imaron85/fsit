package dev.rvbsm.fsit.core.registry;

import com.google.common.collect.ImmutableSet;
import com.google.errorprone.annotations.DoNotCall;
import com.google.errorprone.annotations.Immutable;
import net.minecraft.core.DefaultedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.ItemLike;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

@Immutable
public abstract class RegistrySet<T extends ItemLike> implements Set<String>, Predicate<T> {

    protected final Set<T> entries;
    protected final Set<TagKey<T>> tags;
    private final DefaultedRegistry<T> registry;
    private final ImmutableSet<String> ids;

    protected RegistrySet(final DefaultedRegistry<T> registry, final Collection<String> ids) {
        this.registry = registry;
        this.ids = ImmutableSet.copyOf(ids);

        final var entriesBuilder = ImmutableSet.<T>builder();
        final var tagsBuilder = ImmutableSet.<TagKey<T>>builderWithExpectedSize(this.size());
        final ResourceKey<? extends Registry<T>> registryKey = registry.key();

        this.forEach((sid) -> {
            final boolean isTag = sid.charAt(0) == '#';
            if (isTag) {
                sid = sid.substring(1);
            }

            final var id = Identifier.parse(sid);
            if (!isTag) {
                final T entry = registry.getValue(id);
                entriesBuilder.add(entry);
            } else {
                final TagKey<T> tag = TagKey.create(registryKey, id);
                tagsBuilder.add(tag);
            }
        });

        this.entries = entriesBuilder.build();
        this.tags = tagsBuilder.build();
    }

    public static BlockSet ofBlocks(final String... ids) {
        return new BlockSet(ImmutableSet.copyOf(ids));
    }

    public static BlockSet ofBlocks(final Collection<String> ids) {
        return new BlockSet(ids);
    }

    @Override
    public int size() {
        return this.ids.size();
    }

    @Override
    public boolean isEmpty() {
        return this.ids.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return this.ids.contains(o);
    }

    @Override
    public boolean containsAll(Collection<?> collection) {
        return false;
    }

    @Override
    public Iterator<String> iterator() {
        return this.ids.iterator();
    }

    @Override
    public Object[] toArray() {
        return this.ids.toArray();
    }

    @Override
    public <T> T[] toArray(T[] ts) {
        return this.ids.toArray(ts);
    }

    @Deprecated
    @Override
    @DoNotCall
    public boolean add(String s) {
        throw new UnsupportedOperationException();
    }

    @Deprecated
    @Override
    @DoNotCall
    public boolean remove(Object o) {
        throw new UnsupportedOperationException();
    }

    @Deprecated
    @Override
    @DoNotCall
    public boolean addAll(Collection<? extends String> collection) {
        throw new UnsupportedOperationException();
    }

    @Deprecated
    @Override
    @DoNotCall
    public boolean removeAll(Collection<?> collection) {
        throw new UnsupportedOperationException();
    }

    @Deprecated
    @Override
    @DoNotCall
    public boolean retainAll(Collection<?> collection) {
        throw new UnsupportedOperationException();
    }

    @Deprecated
    @Override
    @DoNotCall
    public void clear() {
        throw new UnsupportedOperationException();
    }

    public DefaultedRegistry<T> getRegistry() {
        return this.registry;
    }

    public List<String> asList() {
        return this.ids.asList();
    }
}
