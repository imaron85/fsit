package dev.rvbsm.fsit.yaml;

import com.google.gson.internal.LazilyParsedNumber;
import com.google.gson.internal.NumberLimits;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.ListBuilder;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import org.jspecify.annotations.Nullable;
import org.snakeyaml.engine.v2.common.FlowStyle;
import org.snakeyaml.engine.v2.common.ScalarStyle;
import org.snakeyaml.engine.v2.nodes.MappingNode;
import org.snakeyaml.engine.v2.nodes.Node;
import org.snakeyaml.engine.v2.nodes.NodeTuple;
import org.snakeyaml.engine.v2.nodes.ScalarNode;
import org.snakeyaml.engine.v2.nodes.SequenceNode;
import org.snakeyaml.engine.v2.nodes.Tag;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

// TODO: minimize creation of immutable objects
public final class YamlOps implements DynamicOps<Node> {

    public static final YamlOps INSTANCE = new YamlOps();

    private static final Node EMPTY =
            new ScalarNode(Tag.NULL, false, "null", ScalarStyle.PLAIN, Optional.empty(), Optional.empty());

    private YamlOps() {}

    @Override
    public Node empty() {
        return EMPTY;
    }

    @Override
    public <U> U convertTo(DynamicOps<U> outOps, Node input) {
        return switch (input) {
            case SequenceNode sequence -> convertList(outOps, sequence);
            case MappingNode mapping -> convertMap(outOps, mapping);

            case ScalarNode node -> {
                var tag = node.getTag();

                if (tag == Tag.NULL) {
                    yield outOps.empty();
                }

                if (tag == Tag.STR) {
                    yield outOps.createString(node.getValue());
                }

                if (tag == Tag.BOOL) {
                    yield outOps.createBoolean(Boolean.parseBoolean(node.getValue()));
                }

                if (tag == Tag.INT) {
                    var decimal = NumberLimits.parseBigDecimal(node.getValue());
                    var l = decimal.longValueExact();

                    if ((byte) l == l) {
                        yield outOps.createByte((byte) l);
                    } else if ((short) l == l) {
                        yield outOps.createShort((short) l);
                    } else if ((int) l == l) {
                        yield outOps.createInt((int) l);
                    }

                    yield outOps.createLong(l);
                }

                if (tag == Tag.FLOAT) {
                    var d = Double.parseDouble(node.getValue());
                    if ((float) d == d) {
                        yield outOps.createFloat((float) d);
                    }

                    yield outOps.createDouble(d);
                }

                if (tag == Tag.BINARY) {
                    var array = Base64.getDecoder().decode(node.getValue());
                    yield outOps.createByteList(ByteBuffer.wrap(array));
                }

                throw new IllegalArgumentException("Unexpected value: " + input);
            }

            default -> throw new IllegalArgumentException("Unexpected value: " + input);
        };
    }

    @Override
    public DataResult<Number> getNumberValue(Node input) {
        if (input instanceof ScalarNode node) {
            if (node.getTag() == Tag.INT || node.getTag() == Tag.FLOAT) {
                return DataResult.success(new LazilyParsedNumber(node.getValue()));
            }
        }

        return DataResult.error(() -> "Not a number: " + Yaml.serializeToBlock(input));
    }

    @Override
    public Node createNumeric(Number i) {
        return new ScalarNode(Tag.FLOAT, i.toString(), ScalarStyle.PLAIN);
    }

    @Override
    public Node createByte(byte value) {
        return new ScalarNode(Tag.INT, Byte.toString(value), ScalarStyle.PLAIN);
    }

    @Override
    public Node createShort(short value) {
        return new ScalarNode(Tag.INT, Short.toString(value), ScalarStyle.PLAIN);
    }

    @Override
    public Node createInt(int value) {
        return new ScalarNode(Tag.INT, Integer.toString(value), ScalarStyle.PLAIN);
    }

    @Override
    public Node createLong(long value) {
        return new ScalarNode(Tag.INT, Long.toString(value), ScalarStyle.PLAIN);
    }

    @Override
    public Node createFloat(float value) {
        return new ScalarNode(Tag.FLOAT, Float.toString(value), ScalarStyle.PLAIN);
    }

    @Override
    public Node createDouble(double value) {
        return new ScalarNode(Tag.FLOAT, Double.toString(value), ScalarStyle.PLAIN);
    }

    @Override
    public DataResult<Boolean> getBooleanValue(Node input) {
        if (input instanceof ScalarNode node && node.getTag() == Tag.BOOL) {
            return DataResult.success(Boolean.parseBoolean(node.getValue()));
        }

        return DataResult.error(() -> "Not a boolean: " + Yaml.serializeToBlock(input));
    }

    @Override
    public Node createBoolean(boolean value) {
        return new ScalarNode(Tag.BOOL, Boolean.toString(value), ScalarStyle.PLAIN);
    }

    @Override
    public DataResult<String> getStringValue(Node input) {
        if (input instanceof ScalarNode scalar && scalar.getTag() == Tag.STR) {
            return DataResult.success(scalar.getValue());
        }

        return DataResult.error(() -> "Not a string: " + Yaml.serializeToBlock(input));
    }

    @Override
    public Node createString(String value) {
        return new ScalarNode(Tag.STR, value, ScalarStyle.PLAIN);
    }

    @Override
    public DataResult<Node> mergeToList(Node list, Node value) {
        if (!(list instanceof SequenceNode sequence)) {
            if (list.getTag() != Tag.NULL) {
                return DataResult.error(() -> "mergeList called with not a list: " + Yaml.serializeToBlock(list), list);
            }

            return DataResult.success(createList(Stream.of(value)));
        }

        if (list.getTag() == Tag.NULL) {
            return DataResult.success(createList(Stream.of(value)));
        }

        return DataResult.success(createList(Stream.concat(sequence.getValue().stream(), Stream.of(value))));
    }

    @Override
    public DataResult<Node> mergeToList(Node list, List<Node> values) {
        if (!(list instanceof SequenceNode sequence)) {
            if (list.getTag() != Tag.NULL) {
                return DataResult.error(() -> "mergeList called with not a list: " + Yaml.serializeToBlock(list), list);
            }

            if (values.isEmpty()) {
                return DataResult.success(emptyList());
            }

            return DataResult.success(createList(values.stream()));
        }

        if (values.isEmpty()) {
            if (list.getTag() == Tag.NULL) {
                return DataResult.success(emptyList());
            }

            return DataResult.success(list);
        }

        return DataResult.success(createList(Stream.concat(sequence.getValue().stream(), values.stream())));
    }

    @Override
    public DataResult<Node> mergeToMap(Node map, Node key, Node value) {
        if (!(map instanceof MappingNode mapping)) {
            if (map.getTag() != Tag.NULL) {
                return DataResult.error(() -> "mergeMap called with not a map: " + Yaml.serializeToBlock(map), map);
            }

            if (!(key instanceof ScalarNode) && key.getTag() != Tag.STR) {
                return DataResult.error(() -> "key is not a string: " + Yaml.serializeToBlock(key), map);
            }

            return DataResult.success(createMap(Stream.of(Pair.of(key, value))));
        }

        if (!(key instanceof ScalarNode) && key.getTag() != Tag.STR) {
            return DataResult.error(() -> "key is not a string: " + Yaml.serializeToBlock(key), map);
        }

        if (map.getTag() == Tag.NULL) {
            return DataResult.success(createMap(Stream.of(Pair.of(key, value))));
        }

        return DataResult.success(createMap(Stream.concat(
                mapping.getValue().stream().map(t -> Pair.of(t.getKeyNode(), t.getValueNode())),
                Stream.of(Pair.of(key, value)))));
    }

    @Override
    public DataResult<Node> mergeToMap(Node map, MapLike<Node> values) {
        Iterator<Pair<Node, Node>> valuesIterator = values.entries().iterator();

        if (!(map instanceof MappingNode mapping)) {
            if (map.getTag() != Tag.NULL) {
                return DataResult.error(() -> "mergeMap called with not a map: " + Yaml.serializeToBlock(map), map);
            }

            if (!valuesIterator.hasNext()) {
                return DataResult.success(emptyMap());
            }

            return DataResult.success(createMap(values.entries()));
        }

        if (!valuesIterator.hasNext()) {
            if (map.getTag() == Tag.NULL) {
                return DataResult.success(emptyMap());
            }

            return DataResult.success(map);
        }

        var output = new ArrayList<Pair<Node, Node>>();
        var missed = new ArrayList<String>();

        while (valuesIterator.hasNext()) {
            Pair<Node, Node> entry = valuesIterator.next();
            var key = entry.getFirst();
            if (!(key instanceof ScalarNode) || key.getTag() != Tag.STR) {
                missed.add(Yaml.serializeToBlock(key));
                continue;
            }

            output.add(entry);
        }
        var outputMap = createMap(Stream.concat(
                mapping.getValue().stream().map(t -> Pair.of(t.getKeyNode(), t.getValueNode())), output.stream()));

        if (!missed.isEmpty()) {
            return DataResult.error(() -> "some keys are not strings: " + missed, outputMap);
        }

        return DataResult.success(outputMap);
    }

    @Override
    public DataResult<Stream<Pair<Node, Node>>> getMapValues(Node input) {
        if (!(input instanceof MappingNode mapping)) {
            return DataResult.error(() -> "Not a YAML map: " + Yaml.serializeToBlock(input));
        }

        return DataResult.success(mapping.getValue().stream()
                .map(entry -> Pair.of(
                        entry.getKeyNode(), entry.getValueNode().getTag() == Tag.NULL ? null : entry.getValueNode())));
    }

    @Override
    public DataResult<Consumer<BiConsumer<Node, @Nullable Node>>> getMapEntries(Node input) {
        if (!(input instanceof MappingNode mapping)) {
            return DataResult.error(() -> "Not a YAML mapping: " + Yaml.serializeToBlock(input));
        }

        return DataResult.success(c -> {
            for (var entry : mapping.getValue()) {
                c.accept(entry.getKeyNode(), entry.getValueNode().getTag() == Tag.NULL ? null : entry.getValueNode());
            }
        });
    }

    @Override
    public DataResult<MapLike<@Nullable Node>> getMap(Node input) {
        if (!(input instanceof MappingNode mapping)) {
            return DataResult.error(() -> "Not a YAML mapping: " + Yaml.serializeToBlock(input));
        }

        var map = mapping.getValue().stream().collect(Collectors.toMap(NodeTuple::getKeyNode, NodeTuple::getValueNode));

        return DataResult.success(new MapLike<>() {

            @Override
            public @Nullable Node get(Node key) {
                var node = map.get(key);
                if (node == null || node.getTag() != Tag.NULL) {
                    return null;
                }

                return node;
            }

            @Override
            public @Nullable Node get(String key) {
                for (var entry : map.entrySet()) {
                    var entryKey = entry.getKey();
                    if (entryKey instanceof ScalarNode scalar
                            && scalar.getValue().equals(key)) {
                        return entry.getValue().getTag() == Tag.NULL ? null : entry.getValue();
                    }
                }

                return null;
            }

            @Override
            public Stream<Pair<Node, Node>> entries() {
                return map.entrySet().stream().map(e -> Pair.of(e.getKey(), e.getValue()));
            }

            @Override
            public String toString() {
                return "MapLike[" + Yaml.serializeToBlock(mapping) + "]";
            }
        });
    }

    @Override
    public Node createMap(Stream<Pair<Node, Node>> map) {
        var value = map.map(e -> new NodeTuple(e.getFirst(), e.getSecond())).toList();
        return new MappingNode(Tag.MAP, value, FlowStyle.AUTO);
    }

    @Override
    public DataResult<Stream<Node>> getStream(Node input) {
        if (input instanceof SequenceNode sequence) {
            return DataResult.success(sequence.getValue().stream().map(e -> e.getTag() == Tag.NULL ? null : e));
        }

        return DataResult.error(() -> "Not a YAML sequence: " + Yaml.serializeToBlock(input));
    }

    @Override
    public DataResult<Consumer<Consumer<@Nullable Node>>> getList(Node input) {
        if (input instanceof SequenceNode sequence) {
            return DataResult.success(c -> {
                for (var node : sequence.getValue()) {
                    c.accept(node.getTag() == Tag.NULL ? null : node);
                }
            });
        }

        return DataResult.error(() -> "Not a YAML sequence: " + Yaml.serializeToBlock(input));
    }

    @Override
    public Node createList(Stream<Node> input) {
        return new SequenceNode(Tag.SEQ, input.toList(), FlowStyle.AUTO);
    }

    @Override
    public DataResult<ByteBuffer> getByteBuffer(Node input) {
        if (input instanceof ScalarNode scalar && input.getTag() == Tag.BINARY) {
            return DataResult.success(ByteBuffer.wrap(Base64.getDecoder().decode(scalar.getValue())));
        }

        return DataResult.error(() -> "Not a YAML binary: " + Yaml.serializeToBlock(input));
    }

    @Override
    public Node createByteList(ByteBuffer input) {
        return new ScalarNode(Tag.BINARY, Base64.getEncoder().encodeToString(input.array()), ScalarStyle.PLAIN);
    }

    @Override
    public Node remove(Node input, String key) {
        if (input instanceof MappingNode mapping) {
            return createMap(mapping.getValue().stream()
                    .filter(t -> t.getKeyNode() instanceof ScalarNode scalar
                            && scalar.getValue().equals(key))
                    .map(e -> Pair.of(e.getKeyNode(), e.getValueNode())));
        }

        return input;
    }

    @Override
    public String toString() {
        return "YAML";
    }

    @Override
    public ListBuilder<Node> listBuilder() {
        return new SequenceBuilder();
    }

    @Override
    public RecordBuilder<Node> mapBuilder() {
        return new MappingBuilder();
    }

    private static final class SequenceBuilder implements ListBuilder<Node> {

        private DataResult<SequenceNode> builder =
                DataResult.success(new SequenceNode(Tag.SEQ, new ArrayList<>(), FlowStyle.AUTO), Lifecycle.stable());

        @Override
        public DynamicOps<Node> ops() {
            return INSTANCE;
        }

        @Override
        public DataResult<Node> build(Node prefix) {
            DataResult<Node> result = builder.flatMap(b -> {
                if (!(prefix instanceof SequenceNode sequence)) {
                    if (prefix.getTag() != Tag.NULL) {
                        return DataResult.error(() -> "Cannot append a list to not a list:" + Yaml.serializeToBlock(prefix), prefix);
                    }

                    return DataResult.success(b, Lifecycle.stable());
                }

                if (prefix.getTag() != Tag.NULL) {
                    return DataResult.success(
                            new SequenceNode(
                                    Tag.SEQ,
                                    Stream.concat(sequence.getValue().stream(), b.getValue().stream())
                                            .toList(),
                                    FlowStyle.AUTO),
                            Lifecycle.stable());
                }

                return DataResult.success(b, Lifecycle.stable());
            });

            builder = DataResult.success(new SequenceNode(Tag.SEQ, new ArrayList<>(), FlowStyle.AUTO));
            return result;
        }

        @Override
        public ListBuilder<Node> add(Node value) {
            builder = builder.map(b -> {
                b.getValue().add(value);
                return b;
            });
            return this;
        }

        @Override
        public ListBuilder<Node> add(DataResult<Node> value) {
            builder = builder.apply2stable(
                    (b, node) -> {
                        b.getValue().add(node);
                        return b;
                    },
                    value);
            return this;
        }

        @Override
        public ListBuilder<Node> withErrorsFrom(DataResult<?> result) {
            builder = builder.flatMap(r -> result.map(_ -> r));
            return this;
        }

        @Override
        public ListBuilder<Node> mapError(UnaryOperator<String> onError) {
            builder = builder.mapError(onError);
            return this;
        }
    }

    private static final class MappingBuilder extends RecordBuilder.AbstractStringBuilder<Node, MappingNode> {

        private MappingBuilder() {
            super(YamlOps.INSTANCE);
        }

        @Override
        protected MappingNode append(String key, Node value, MappingNode builder) {
            builder.getValue().add(new NodeTuple(new ScalarNode(Tag.STR, key, ScalarStyle.PLAIN), value));
            return builder;
        }

        @Override
        protected MappingNode initBuilder() {
            return new MappingNode(Tag.MAP, new ArrayList<>(), FlowStyle.AUTO);
        }

        @Override
        protected DataResult<Node> build(MappingNode builder, Node prefix) {
            if (prefix.getTag() == Tag.NULL) {
                return DataResult.success(builder);
            }

            if (prefix instanceof MappingNode mapping) {
                return DataResult.success(new MappingNode(
                        Tag.MAP,
                        Stream.concat(mapping.getValue().stream(), builder.getValue().stream())
                                .toList(),
                        FlowStyle.AUTO));
            }

            return DataResult.error(() -> "mergeToMap called with not a map: " + Yaml.serializeToBlock(prefix), prefix);
        }
    }
}
