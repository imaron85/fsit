package dev.rvbsm.fsit.config;

import com.google.common.collect.ImmutableList;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.mojang.logging.LogUtils;
import dev.rvbsm.fsit.util.registry.BlockSet;
import dev.rvbsm.fsit.util.serialize.BlockSetSerializer;
import dev.rvbsm.fsit.util.serialize.EnumSerializer;
import io.leangen.geantyref.TypeToken;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.Util;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.ConfigurationOptions;
import org.spongepowered.configurate.gson.GsonConfigurationLoader;
import org.spongepowered.configurate.yaml.NodeStyle;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public final class ConfigLoader {

    public static final List<String> CONFIG_KEYS;

    private static final Logger LOGGER = LogUtils.getLogger();

    private static final ConfigurationOptions LOADER_OPTIONS = ConfigurationOptions.defaults()
        .shouldCopyDefaults(true)
        .serializers(builder -> builder
            .register(new TypeToken<>() {}, EnumSerializer.INSTANCE) // do not tag enums in YAML; use lowercase
            .register(BlockSet.class, BlockSetSerializer.INSTANCE)
        );

    private static final YamlConfigurationLoader YAML_LOADER = YamlConfigurationLoader.builder()
        .defaultOptions(LOADER_OPTIONS)
        .nodeStyle(NodeStyle.BLOCK)
        .indent(2)
        .path(FabricLoader.getInstance().getConfigDir().resolve("fsit.yml"))
        .build();

    private static final ConfigurationNode DEFAULT_NODE = YAML_LOADER.createNode();
    private static final GsonConfigurationLoader.Builder GSON_LOADER_BUILDER = GsonConfigurationLoader.builder()
        .defaultOptions(LOADER_OPTIONS);

    static {
        try {
            DEFAULT_NODE.set(ConfigData.DEFAULT);
        } catch (ConfigurateException e) {
            LOGGER.warn("Could not load default configuration node", e);
        }

        CONFIG_KEYS = getKeys(DEFAULT_NODE, "").reversed();
    }

    // fixme hacky
    private static List<String> getKeys(final ConfigurationNode node, final String prefix) {
        final var paths = ImmutableList.<String>builder();

        if (!node.isMap()) {
            return paths.add(prefix).build();
        }

        for (final var child : node.childrenMap().entrySet()) {
            final String childPrefix = prefix.isEmpty() ? child.getKey().toString() : prefix + '.' + child.getKey();
            paths.addAll(getKeys(child.getValue(), childPrefix));
        }

        return paths.build();
    }

    public static CompletableFuture<ConfigData> loadYaml() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                final ConfigurationNode node = YAML_LOADER.load();

                ConfigTransformation.apply(node);
                node.mergeFrom(DEFAULT_NODE);

                return Objects.requireNonNullElse(node.get(ConfigData.class), ConfigData.DEFAULT);
            } catch (ConfigurateException e) {
                LOGGER.warn("Could not load a configuration file", e);
                return ConfigData.DEFAULT; // TODO or rethrow as a runtime exception?
            }
        }, Util.ioPool());
    }

    @CanIgnoreReturnValue
    public static CompletableFuture<Void> saveYaml(final ConfigData config) {
        return CompletableFuture.runAsync(() -> {
            final ConfigurationNode node = YAML_LOADER.createNode();

            try {
                node.set(config);
                YAML_LOADER.save(node);
            } catch (ConfigurateException e) {
                LOGGER.warn("Could not save a configuration file", e);
            }
        }, Util.ioPool());
    }

    public static CompletableFuture<ConfigData> loadJson(final String input) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                final ConfigurationNode node = GSON_LOADER_BUILDER.buildAndLoadString(input);

                ConfigTransformation.apply(node);
                node.mergeFrom(DEFAULT_NODE);

                return Objects.requireNonNullElse(node.get(ConfigData.class), ConfigData.DEFAULT);
            } catch (ConfigurateException e) {
                LOGGER.warn("Could not decode JSON configuration", e);
                return ConfigData.DEFAULT;
            }
        });
    }

    public static CompletableFuture<String> saveJson(final ConfigData config) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                final StringWriter sink = new StringWriter();
                final GsonConfigurationLoader loader = GSON_LOADER_BUILDER
                    .sink(() -> new BufferedWriter(sink))
                    .build();

                final ConfigurationNode node = loader.createNode();
                node.set(ConfigData.class, config);
                loader.save(node);

                return sink.toString();
            } catch (ConfigurateException e) {
                LOGGER.warn("Could not encode JSON configuration", e);
                return "{}"; // TODO silly
            }
        });
    }

    // todo use result of some sort?
    public static CompletableFuture<@Nullable String> getJson(final ConfigData config, final String key) {
        return CompletableFuture.supplyAsync(() -> {
            final StringWriter sink = new StringWriter();
            final GsonConfigurationLoader loader = GSON_LOADER_BUILDER
                .sink(() -> new BufferedWriter(sink))
                .build();

            final ConfigurationNode node = loader.createNode();

            try {
                node.set(config);

                // String[] could not be used as vararg
                final Object[] path = key.split("\\.");
                final ConfigurationNode dataNode = node.node(path);
                loader.save(dataNode);

                return sink.toString();
            } catch (ConfigurateException e) {
                LOGGER.warn("Could not retrieve configuration data at `{}`", key, e);
                return null;
            }
        });
    }

    public static CompletableFuture<@Nullable ConfigData> setJson(final ConfigData config, final String key, final String data) {
        return CompletableFuture.supplyAsync(() -> {
            final GsonConfigurationLoader loader = GSON_LOADER_BUILDER
                .source(() -> new BufferedReader(new StringReader(data)))
                .build();

            final ConfigurationNode node = loader.createNode();

            try {
                node.set(config);

                // String[] could not be used as vararg
                final Object[] path = key.split("\\.");
                if (!node.hasChild(path)) {
                    return null;
                }

                final ConfigurationNode dataNode = node.node(path);
                dataNode.from(loader.load());

                return node.get(ConfigData.class);
            } catch (ConfigurateException e) {
                LOGGER.warn("Could not update configuration data at `{}` with `{}`", key, data, e);
                return null;
            }
        });
    }
}
