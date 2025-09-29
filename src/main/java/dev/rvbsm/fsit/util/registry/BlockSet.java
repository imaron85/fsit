package dev.rvbsm.fsit.util.registry;

import net.minecraft.block.Block;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;

import java.util.Collection;

public final class BlockSet extends RegistrySet<Block> {

    public BlockSet(Collection<String> ids) {
        super(Registries.BLOCK, ids);
    }

    @Override
    public boolean test(final Block block) {
        if (!this.entries.isEmpty() && this.entries.contains(block)) {
            return true;
        }

        if (this.tags.isEmpty()) {
            return false;
        }

        final RegistryEntry<Block> blockEntry = this.getRegistry().getEntry(block);
        return blockEntry.streamTags().anyMatch(this.tags::contains);
    }
}
