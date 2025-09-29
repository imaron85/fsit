package dev.rvbsm.fsit.config;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import dev.rvbsm.fsit.util.registry.BlockSet;
import dev.rvbsm.fsit.util.registry.RegistrySet;

import java.util.Collection;
import java.util.List;

public final class ConfigBuilder {

    private boolean useServer = false;

    private ConfigData.Sitting.Behaviour sittingBehaviour = ConfigData.Sitting.Behaviour.GRAVITY;
    private boolean sittingShouldCenter = false;

    private boolean onUseSitting = true;
    private boolean onUseRiding = true;
    private int onUseRange = 2;
    private boolean onUseCheckSuffocation = true;
    private BlockSet onUseBlocks = RegistrySet.ofBlocks("#slabs", "#stairs", "#logs", "#fences");

    private boolean onSneakSitting = true;
    private boolean onSneakCrawling = true;
    private float onSneakMinPitch = 60.0f;
    private int onSneakWindow = 7;

    private boolean commandsRoot = true;
    private boolean commandsSit = true;
    private boolean commandsCrawl = true;

    ConfigBuilder() {
    }

    ConfigBuilder(ConfigData config) {
        this.useServer = config.useServer();

        this.sittingBehaviour = config.sitting().behaviour();
        this.sittingShouldCenter = config.sitting().shouldCenter();

        this.onUseSitting = config.onUse().sitting();
        this.onUseRiding = config.onUse().riding();
        this.onUseRange = config.onUse().range();
        this.onUseCheckSuffocation = config.onUse().checkSuffocation();
        this.onUseBlocks = config.onUse().blocks();

        this.onSneakSitting = config.onSneak().sitting();
        this.onSneakCrawling = config.onSneak().crawling();
        this.onSneakMinPitch = config.onSneak().minPitch();
        this.onSneakWindow = config.onSneak().window();

        this.commandsRoot = config.commands().root();
        this.commandsSit = config.commands().sit();
        this.commandsCrawl = config.commands().crawl();
    }

    public ConfigData build() {
        return new ConfigData(ConfigData.LATEST_VERSION, useServer,
            new ConfigData.Sitting(sittingBehaviour, sittingShouldCenter),
            new ConfigData.OnUse(onUseSitting, onUseRiding, onUseRange, onUseCheckSuffocation, onUseBlocks),
            new ConfigData.OnSneak(onSneakSitting, onSneakCrawling, onSneakMinPitch, onSneakWindow),
            new ConfigData.Commands(commandsRoot, commandsSit, commandsCrawl)
        );
    }

    public boolean useServer() {
        return this.useServer;
    }

    @CanIgnoreReturnValue
    public ConfigBuilder useServer(boolean useServer) {
        this.useServer = useServer;
        return this;
    }

    public ConfigData.Sitting.Behaviour sittingBehaviour() {
        return this.sittingBehaviour;
    }

    @CanIgnoreReturnValue
    public ConfigBuilder sittingBehaviour(ConfigData.Sitting.Behaviour sittingBehaviour) {
        this.sittingBehaviour = sittingBehaviour;
        return this;
    }

    public boolean sittingShouldCenter() {
        return this.sittingShouldCenter;
    }

    @CanIgnoreReturnValue
    public ConfigBuilder sittingShouldCenter(boolean sittingShouldCenter) {
        this.sittingShouldCenter = sittingShouldCenter;
        return this;
    }

    public boolean onUseSitting() {
        return this.onUseSitting;
    }

    @CanIgnoreReturnValue
    public ConfigBuilder onUseSitting(boolean onUseSitting) {
        this.onUseSitting = onUseSitting;
        return this;
    }

    public boolean onUseRiding() {
        return this.onUseRiding;
    }

    @CanIgnoreReturnValue
    public ConfigBuilder onUseRiding(boolean onUseRiding) {
        this.onUseRiding = onUseRiding;
        return this;
    }

    public int onUseRange() {
        return this.onUseRange;
    }

    @CanIgnoreReturnValue
    public ConfigBuilder onUseRange(int onUseRange) {
        this.onUseRange = onUseRange;
        return this;
    }

    public boolean onUseCheckSuffocation() {
        return this.onUseCheckSuffocation;
    }

    @CanIgnoreReturnValue
    public ConfigBuilder onUseCheckSuffocation(boolean onUseCheckSuffocation) {
        this.onUseCheckSuffocation = onUseCheckSuffocation;
        return this;
    }

    public BlockSet onUseBlocks() {
        return this.onUseBlocks;
    }

    public List<String> onUseBlocksList() {
        return this.onUseBlocks.asList();
    }

    @CanIgnoreReturnValue
    public ConfigBuilder onUseBlocks(Collection<String> onUseBlocks) {
        this.onUseBlocks = BlockSet.ofBlocks(onUseBlocks);
        return this;
    }

    public boolean onSneakSitting() {
        return this.onSneakSitting;
    }

    @CanIgnoreReturnValue
    public ConfigBuilder onSneakSitting(boolean onSneakSitting) {
        this.onSneakSitting = onSneakSitting;
        return this;
    }

    public boolean onSneakCrawling() {
        return this.onSneakCrawling;
    }

    @CanIgnoreReturnValue
    public ConfigBuilder onSneakCrawling(boolean onSneakCrawling) {
        this.onSneakCrawling = onSneakCrawling;
        return this;
    }

    public float onSneakMinPitch() {
        return this.onSneakMinPitch;
    }

    @CanIgnoreReturnValue
    public ConfigBuilder onSneakMinPitch(float onSneakMinPitch) {
        this.onSneakMinPitch = onSneakMinPitch;
        return this;
    }

    public int onSneakWindow() {
        return this.onSneakWindow;
    }

    @CanIgnoreReturnValue
    public ConfigBuilder onSneakWindow(int onSneakWindow) {
        this.onSneakWindow = onSneakWindow;
        return this;
    }

    public boolean commandsRoot() {
        return this.commandsRoot;
    }

    @CanIgnoreReturnValue
    public ConfigBuilder commandsRoot(boolean commandsRoot) {
        this.commandsRoot = commandsRoot;
        return this;
    }

    public boolean commandsSit() {
        return this.commandsSit;
    }

    @CanIgnoreReturnValue
    public ConfigBuilder commandsSit(boolean commandsSit) {
        this.commandsSit = commandsSit;
        return this;
    }

    public boolean commandsCrawl() {
        return this.commandsCrawl;
    }

    @CanIgnoreReturnValue
    public ConfigBuilder commandsCrawl(boolean commandsCrawl) {
        this.commandsCrawl = commandsCrawl;
        return this;
    }
}
