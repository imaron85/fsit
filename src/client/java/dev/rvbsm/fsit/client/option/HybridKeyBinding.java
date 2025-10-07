package dev.rvbsm.fsit.client.option;

import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.TranslatableOption;

import java.util.HashSet;
import java.util.Set;
import java.util.function.IntFunction;
import java.util.function.IntSupplier;
import java.util.function.Supplier;
import java.util.stream.IntStream;

public class HybridKeyBinding extends KeyBinding {

    private static final Set<HybridKeyBinding> HYBRID_KEYS = new HashSet<>();
    private final Supplier<Mode> modeGetter;
    private final IntSupplier windowGetter;
    private int stickyTicks = 0;

    //? if >=1.21.9 {
    public HybridKeyBinding(String id, int code, Category category, Supplier<Mode> modeGetter, IntSupplier windowGetter) {
    //?} else
    /*public HybridKeyBinding(String id, int code, String category, Supplier<Mode> modeGetter, IntSupplier windowGetter) {*/
        super(id, InputUtil.Type.KEYSYM, code, category);
        this.modeGetter = modeGetter;
        this.windowGetter = windowGetter;
        HYBRID_KEYS.add(this);
    }

    public static void tick() {
        for (final HybridKeyBinding hybridKey : HYBRID_KEYS) {
            if (hybridKey.stickyTicks > 0) {
                hybridKey.stickyTicks -= 1;
            }
        }
    }

    @Override
    public void setPressed(boolean pressed) {
        switch (this.modeGetter.get()) {
            case TOGGLE -> {
                if (pressed) {
                    super.setPressed(!this.isPressed());
                }
            }
            case HOLD -> super.setPressed(pressed);
            case HYBRID -> {
                if (pressed) {
                    super.setPressed(!this.isPressed());

                    if (this.isPressed()) {
                        this.stickyTicks = this.windowGetter.getAsInt();
                    }
                } else {
                    if (this.stickyTicks == 0) {
                        super.setPressed(false);
                    }
                }
            }
        }
    }


    public enum Mode implements TranslatableOption {
        TOGGLE("options.key.toggle"), HOLD("options.key.hold"), HYBRID("fsit.option.key.hybrid");

        private static final IntFunction<Mode> ID_MAPPER = new Int2ObjectArrayMap<>(IntStream.range(0, values().length).toArray(), values());
        public static final Codec<Mode> CODEC = Codec.INT.xmap(ID_MAPPER::apply, Mode::ordinal);
        private final String translationKey;

        Mode(final String translationKey) {
            this.translationKey = translationKey;
        }

        @Override
        public int getId() {
            return this.ordinal();
        }

        @Override
        public String getTranslationKey() {
            return this.translationKey;
        }
    }
}
