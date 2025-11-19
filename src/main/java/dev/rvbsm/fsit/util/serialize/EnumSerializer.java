package dev.rvbsm.fsit.util.serialize;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.jspecify.annotations.NullMarked;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;
import org.spongepowered.configurate.util.EnumLookup;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Locale;
import java.util.Objects;

@NullMarked
public final class EnumSerializer implements TypeSerializer<Enum<?>> {

    public static final EnumSerializer INSTANCE = new EnumSerializer();

    private static <T extends Enum<T>> String getAvailableConstants(Class<T> enumClass) {
        return Arrays.toString(enumClass.getEnumConstants()).toLowerCase(Locale.ROOT);
    }

    @Override
    public Enum<?> deserialize(final Type type, final ConfigurationNode node) throws SerializationException {
        final String enumName = Objects.requireNonNull(node.getString());
        final Enum<?> enumConstant = EnumLookup.lookupEnum(((Class<?>) type).asSubclass(Enum.class), enumName);
        if (enumConstant == null) {
            final String availableConstants = getAvailableConstants(((Class<?>) type).asSubclass(Enum.class));
            throw new SerializationException(type, "Invalid enum constant: %s. Perhaps you meant %s?".formatted(enumName, availableConstants));
        }

        return enumConstant;
    }

    @Override
    public void serialize(final Type type, final @Nullable Enum<?> obj, final ConfigurationNode node) throws SerializationException {
        if (obj == null) {
            throw new SerializationException(type, "Enum constant cannot be null");
        }

        node.set(obj.name().toLowerCase(Locale.ROOT));
    }
}
