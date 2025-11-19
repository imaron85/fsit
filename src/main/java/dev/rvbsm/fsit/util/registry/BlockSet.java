package dev.rvbsm.fsit.util.registry;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;

import java.util.Collection;

public final class BlockSet extends RegistrySet<Block> {

    public BlockSet(Collection<String> ids) {
        super(BuiltInRegistries.BLOCK, ids);
    }

    @Override
    public boolean test(final Block block) {
        if (!this.entries.isEmpty() && this.entries.contains(block)) {
            return true;
        }

        if (this.tags.isEmpty()) {
            return false;
        }

        final Holder<Block> blockEntry = this.getRegistry().wrapAsHolder(block);
        return blockEntry.tags().anyMatch(this.tags::contains);
    }
}
