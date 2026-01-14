package dev.rvbsm.fsit.util.serialize;

import dev.rvbsm.fsit.core.registry.BlockSet;
import dev.rvbsm.fsit.core.registry.RegistrySet;

import java.util.List;

public final class BlockSetSerializer extends RegistrySetSerializer<BlockSet> {

    public static final BlockSetSerializer INSTANCE = new BlockSetSerializer();

    private BlockSetSerializer() {
    }

    @Override
    protected BlockSet construct(final List<String> ids) {
        return RegistrySet.ofBlocks(ids);
    }
}
