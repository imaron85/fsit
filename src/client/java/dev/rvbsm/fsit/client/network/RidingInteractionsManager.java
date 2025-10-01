package dev.rvbsm.fsit.client.network;

import net.minecraft.client.MinecraftClient;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

// todo: persistent
public final class RidingInteractionsManager {

    private final MinecraftClient client;
    private final Set<UUID> restrictedPlayers = new HashSet<>();

    public RidingInteractionsManager(MinecraftClient client) {
        this.client = client;
    }

    public void restrictPlayer(UUID uuid) {
        this.restrictedPlayers.add(uuid);
    }

    public void allowPlayer(UUID uuid) {
        this.restrictedPlayers.remove(uuid);
    }

    public boolean isRestricted(UUID uuid) {
        return this.restrictedPlayers.contains(uuid) || this.client.getSocialInteractionsManager().isPlayerMuted(uuid);
    }
}
