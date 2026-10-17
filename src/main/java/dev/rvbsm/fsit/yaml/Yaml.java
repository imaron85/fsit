package dev.rvbsm.fsit.yaml;

import org.snakeyaml.engine.v2.api.DumpSettings;
import org.snakeyaml.engine.v2.api.LoadSettings;
import org.snakeyaml.engine.v2.api.lowlevel.Compose;
import org.snakeyaml.engine.v2.api.lowlevel.Present;
import org.snakeyaml.engine.v2.api.lowlevel.Serialize;
import org.snakeyaml.engine.v2.common.FlowStyle;
import org.snakeyaml.engine.v2.common.ScalarStyle;
import org.snakeyaml.engine.v2.nodes.MappingNode;
import org.snakeyaml.engine.v2.nodes.Node;
import org.snakeyaml.engine.v2.nodes.NodeTuple;
import org.snakeyaml.engine.v2.nodes.ScalarNode;
import org.snakeyaml.engine.v2.nodes.Tag;

import java.io.Reader;
import java.util.List;
import java.util.Optional;

public final class Yaml {

    private static final LoadSettings LOAD_SETTINGS = LoadSettings.builder().build();
    private static final DumpSettings DUMP_SETTINGS = DumpSettings.builder().setDumpComments(true).build();

    public static Optional<Node> compose(Reader reader) {
        return new Compose(LOAD_SETTINGS).composeReader(reader);
    }

    public static String serialize(Node node) {
        return new Present(DUMP_SETTINGS).emitToString(new Serialize(DUMP_SETTINGS).serializeOne(node).iterator());
    }

    /**
     * Used only for {@link com.mojang.serialization.DataResult.Error} messages.
     * Root level nodes do not get emitted to flow style
     * **/
    static String serializeToBlock(Node node) {
        return serialize(new MappingNode(Tag.MAP, List.of(new NodeTuple(new ScalarNode(Tag.STR, "", ScalarStyle.PLAIN), node)), FlowStyle.FLOW));
    }
}
