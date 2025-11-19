package dev.rvbsm.fsit.util.serialize;

import dev.rvbsm.fsit.util.registry.RegistrySet;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.jspecify.annotations.NullMarked;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;

import java.lang.reflect.Type;
import java.util.List;

@NullMarked
public abstract class RegistrySetSerializer<S extends RegistrySet<?>> implements TypeSerializer<S> {

    protected RegistrySetSerializer() {
    }

    protected abstract S construct(final List<String> ids);

    @Override
    public S deserialize(final Type type, final ConfigurationNode node) throws SerializationException {
        final List<String> ids = node.getList(String.class);
        return this.construct(ids);
    }

    @Override
    public void serialize(final Type type, final @Nullable S obj, final ConfigurationNode node) throws SerializationException {
        if (obj == null) {
            throw new SerializationException("Set cannot be null");
        }

        node.setList(String.class, List.copyOf(obj));
    }
}
