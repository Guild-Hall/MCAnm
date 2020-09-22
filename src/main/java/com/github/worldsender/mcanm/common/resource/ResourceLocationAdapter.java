package com.github.worldsender.mcanm.common.resource;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public abstract class ResourceLocationAdapter implements IResourceLocation {
    private List<Consumer<IResourceLocation>> listeners = new ArrayList<>(3);

    // If the resource is active, registering a listener immediately triggers it, if it is inactive, nothing is triggered
    private boolean resourceManagerActive = false;

    public void activate() {
        resourceManagerActive = true;
        triggerReload();
    }

    public void deactivate() {
        resourceManagerActive = false;
    }

    /**
     * This method should be called to notify all listeners of a reload of this resource
     */
    @SuppressWarnings("unchecked")
    public void triggerReload() {
        Consumer<IResourceLocation>[] currentListeners;
        synchronized (listeners) {
            currentListeners = listeners.stream().toArray(Consumer[]::new);
        }
        for (Consumer<IResourceLocation> listener : currentListeners) {
            listener.accept(this);
        }
    }

    @Override
    public boolean shouldCache() {
        return true;
    }

    @Override
    public void registerReloadListener(Consumer<IResourceLocation> reloadListener) {
        synchronized (listeners) {
            listeners.add(reloadListener);
            if (resourceManagerActive) {
                reloadListener.accept(this);
            }
        }
    }

}
