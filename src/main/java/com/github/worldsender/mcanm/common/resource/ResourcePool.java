package com.github.worldsender.mcanm.common.resource;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Predicate;

import com.github.worldsender.mcanm.MCAnm;
import com.github.worldsender.mcanm.client.ClientResourceLocation;

import net.minecraft.resources.IReloadableResourceManager;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.resource.IResourceType;
import net.minecraftforge.resource.ISelectiveResourceReloadListener;

/**
 * Used to collect all {@link ClientResourceLocation}s and trigger an update when the {@link IResourceManager}
 * changes.
 *
 * @author WorldSEnder
 */
public abstract class ResourcePool<T extends ResourceLocationAdapter> {

    private IResourceManager activeResourceManager = null;
    private List<WeakReference<T>> allIssuedLocations = new LinkedList<>();

    private void forEach(Consumer<T> consumer) {
        for (Iterator<WeakReference<T>> it = allIssuedLocations.iterator(); it.hasNext(); ) {
            T resLoc = it.next().get();
            if (resLoc == null) { // Already garbage collected
                it.remove();
                continue;
            }
            consumer.accept(resLoc);
        }
    }

    public void activate(IResourceManager resourceManager) {
        this.activeResourceManager = Objects.requireNonNull(resourceManager);
        if (resourceManager instanceof IReloadableResourceManager) {
            IReloadableResourceManager registry = (IReloadableResourceManager) resourceManager;
            registry.addReloadListener(new ISelectiveResourceReloadListener(){
                @Override
                public void onResourceManagerReload(IResourceManager resourceManager) {
                    onResourceManagerReload(resourceManager, _typ -> true);
                }

                @Override
                public void onResourceManagerReload(IResourceManager resourceManager,
                        Predicate<IResourceType> resourcePredicate) {
                    ResourcePool.this.reload(resourcePredicate);
                }
            });
            // manually trigger a reload to load all resource that have been instantiated up to now
            
        } else {
            MCAnm.logger()
                    .warn("Couldn't register reload managers. Models will not be reloaded on switching resource pack");
        }
        forEach(resource -> resource.activate());
    }

    private void reload(Predicate<IResourceType> resourcePredicate) {
        if (!MCAnm.configuration().isReloadEnabled()) {
            return;
        }
        this.onResourceManagerReloaded(resourcePredicate);
    }

    protected void onResourceManagerReloaded(Predicate<IResourceType> _resourcePredicate) {
        forEach(resource -> resource.triggerReload());
    }

    public IResourceManager getCurrentResourceManager() throws IOException {
        if (activeResourceManager == null) {
            throw new IOException("no resource manager active");
        }
        return activeResourceManager;
    }

    protected abstract T createResourceLocation(ResourceLocation resLoc);

    public T makeResourceLocation(ResourceLocation resLoc) {
        T location = this.createResourceLocation(resLoc);
        if (activeResourceManager != null) { location.activate(); }
        allIssuedLocations.add(new WeakReference<>(location));
        return location;
    }
}
