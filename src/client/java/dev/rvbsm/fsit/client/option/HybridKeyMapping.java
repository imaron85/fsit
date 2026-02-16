package dev.rvbsm.fsit.client.option;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.serialization.Codec;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringRepresentable;

import java.util.HashSet;
import java.util.Set;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

public class HybridKeyMapping extends KeyMapping {

    private static final Set<HybridKeyMapping> HYBRID_KEYS = new HashSet<>();
    private final Supplier<Mode> modeGetter;
    private final IntSupplier windowGetter;
    private int toggleTicks = 0;

    @SuppressWarnings("this-escape")
    public HybridKeyMapping(final String name, final int value, final Category category, final Supplier<Mode> modeGetter, final IntSupplier windowGetter) {
        super(name, InputConstants.Type.KEYSYM, value, category);
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

    public enum Mode implements StringRepresentable {
        TOGGLE("toggle", "options.key.toggle"),
        HOLD("hold", "options.key.hold"),
        HYBRID("hybrid", "fsit.options.key.hybrid");

        public static final Codec<Mode> CODEC = StringRepresentable.fromEnum(Mode::values);
        private final String serializedName;
        private final Component displayName;

        Mode(final String serializedName, final String key) {
            this.serializedName = serializedName;
            this.displayName = Component.translatable(key);
        }

        @Override
        public String getSerializedName() {
            return this.serializedName;
        }

        public Component getDisplayName() {
            return this.displayName;
        }
    }
}
