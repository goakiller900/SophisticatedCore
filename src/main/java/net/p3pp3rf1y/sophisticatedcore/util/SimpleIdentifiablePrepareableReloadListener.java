package net.p3pp3rf1y.sophisticatedcore.util;

import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;

public abstract class SimpleIdentifiablePrepareableReloadListener<T> extends SimplePreparableReloadListener<T> implements IdentifiableResourceReloadListener {
    private final Identifier id;

    public SimpleIdentifiablePrepareableReloadListener(Identifier id) {
        this.id = id;
    }

    @Override
    public Identifier getFabricId() {
        return id;
    }

    @Override
    protected T prepare(ResourceManager resourceManager, ProfilerFiller profiler) {
        return null;
    }
}
