package dev.rvbsm.fsit.config;

import dev.rvbsm.fsit.yaml.Yaml;
import dev.rvbsm.fsit.yaml.YamlOps;
import net.minecraft.util.NullOps;
import org.snakeyaml.engine.v2.nodes.MappingNode;
import org.snakeyaml.engine.v2.nodes.ScalarNode;

import java.io.IOException;
import java.io.Serial;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

public final class ConfigLoader {

    // it is not that bad tbh
    public static final ConfigData INIT_DATA = ConfigData.DATA_CODEC
            .codec()
            .decode(NullOps.INSTANCE, NullOps.INSTANCE.empty())
            .getOrThrow()
            .getFirst();

    public static final class VersionMismatchException extends RuntimeException {
        @Serial
        private static final long serialVersionUID = 1L;

        public final int found;

        public VersionMismatchException(int found) {
            super("Config version mismatch: found " + found + ", expected " + ConfigData.CURRENT_VERSION);
            this.found = found;
        }
    }

    private ConfigLoader() {}

    private static int peekVersion(org.snakeyaml.engine.v2.nodes.Node node) {
        if (!(node instanceof MappingNode map)) return -1;
        for (var tuple : map.getValue()) {
            if (tuple.getKeyNode() instanceof ScalarNode k
                    && "version".equals(k.getValue())
                    && tuple.getValueNode() instanceof ScalarNode v) {
                try {
                    return Integer.parseInt(v.getValue());
                } catch (NumberFormatException ignored) {
                    return -1;
                }
            }
        }
        return -1;
    }

    public static CompletableFuture<ConfigData> readConfig(Path path) {
        return CompletableFuture.supplyAsync(() -> {
            if (Files.notExists(path)) {
                throw new IllegalArgumentException("File " + path + " does not exist");
            }

            try (var reader = Files.newBufferedReader(path)) {
                var node = Yaml.compose(reader).orElse(null);
                if (node == null) {
                    throw new IllegalArgumentException("Could not parse configuration file.");
                }

                int version = peekVersion(node);
                if (version != ConfigData.CURRENT_VERSION) {
                    throw new VersionMismatchException(version);
                }

                var result = ConfigData.CODEC.parse(YamlOps.INSTANCE, node);
                if (result.isError()) {
                    throw new IllegalArgumentException(
                            "Could not decode configuration: " + result.error().orElse(null));
                }

                return result.getOrThrow();
            } catch (IOException e) {
                throw new UncheckedIOException("Error occurred while trying to read configuration file", e);
            }
        });
    }

    public static CompletableFuture<Void> writeConfig(ConfigData config, Path path) {
        return CompletableFuture.runAsync(() -> {
            try (var writer = Files.newBufferedWriter(path)) {
                var result = ConfigData.CODEC.encodeStart(YamlOps.INSTANCE, config);
                if (result.isError()) {
                    throw new IllegalArgumentException(
                            "Could not encode configuration: " + result.error().orElse(null));
                }

                writer.write(Yaml.serialize(result.getOrThrow()));
            } catch (IOException e) {
                throw new UncheckedIOException("Error occurred while trying to write configuration file", e);
            }
        });
    }
}
