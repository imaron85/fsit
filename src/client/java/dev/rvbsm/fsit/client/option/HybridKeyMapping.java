package dev.rvbsm.fsit.client.option;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import net.minecraft.client.KeyMapping;

import java.util.HashSet;
import java.util.Set;
import java.util.function.IntFunction;
import java.util.function.IntSupplier;
import java.util.function.Supplier;
import java.util.stream.IntStream;

public class HybridKeyMapping extends KeyMapping {

    private static final Set<HybridKeyMapping> HYBRID_KEYS = new HashSet<>();
    private final Supplier<Mode> modeGetter;
    private final IntSupplier windowGetter;
    private int toggleTicks = 0;

    public HybridKeyMapping(final String id, final int code, final Category category, final Supplier<Mode> modeGetter, final IntSupplier windowGetter) {
        super(id, InputConstants.Type.KEYSYM, code, category);
        this.modeGetter = modeGetter;
        this.windowGetter = windowGetter;
        HYBRID_KEYS.add(this);
    }

    public static void tick() {
        for (final HybridKeyMapping hybridKey : HYBRID_KEYS) {
            if (hybridKey.toggleTicks > 0) {
                hybridKey.toggleTicks -= 1;
            }
        }
    }

    @Override
    public void setDown(final boolean pressed) {
        switch (this.modeGetter.get()) {
            case TOGGLE -> {
                if (pressed) {
                    super.setDown(!this.isDown());
                }
            }
            case HOLD -> super.setDown(pressed);
            case HYBRID -> {
                if (pressed) {
                    super.setDown(!this.isDown());

                    if (this.isDown()) {
                        this.toggleTicks = this.windowGetter.getAsInt();
                    }
                } else {
                    if (this.toggleTicks == 0) {
                        super.setDown(false);
                    }
                }
            }
        }
    }

    public void reset() {
        super.setDown(false);
    }

    public enum Mode {
        TOGGLE("options.key.toggle"), HOLD("options.key.hold"), HYBRID("fsit.option.key.hybrid");

        private static final IntFunction<Mode> ID_MAPPER = new Int2ObjectArrayMap<>(IntStream.range(0, values().length).toArray(), values());
        public static final Codec<Mode> CODEC = Codec.INT.xmap(ID_MAPPER::apply, Mode::ordinal);
        private final String translationKey;

        Mode(final String translationKey) {
            this.translationKey = translationKey;
        }

        public String getKey() {
            return this.translationKey;
        }
    }
}
