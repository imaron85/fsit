package dev.rvbsm.fsit.client;

import net.fabricmc.api.ClientModInitializer;

import java.util.Objects;

public class FSitModClient implements ClientModInitializer {

    private static FSitModClient instance;

    public static FSitModClient getInstance() {
        return Objects.requireNonNull(instance, "too early");
    }

    public void syncConfig() {
    }

    @Override
    public void onInitializeClient() {
        instance = this;
    }
}
