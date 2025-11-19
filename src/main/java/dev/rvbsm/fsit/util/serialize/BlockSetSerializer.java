package dev.rvbsm.fsit.util.serialize;

import dev.rvbsm.fsit.util.registry.BlockSet;
import dev.rvbsm.fsit.util.registry.RegistrySet;
import org.jspecify.annotations.NullMarked;

import java.util.List;

@NullMarked
public final class BlockSetSerializer extends RegistrySetSerializer<BlockSet> {

    public static final BlockSetSerializer INSTANCE = new BlockSetSerializer();

    private BlockSetSerializer() {
    }

    @Override
    protected BlockSet construct(final List<String> ids) {
        return RegistrySet.ofBlocks(ids);
    }
}
