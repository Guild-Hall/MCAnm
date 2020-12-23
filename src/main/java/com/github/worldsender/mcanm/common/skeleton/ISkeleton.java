package com.github.worldsender.mcanm.common.skeleton;

import com.github.worldsender.mcanm.common.animation.IPose;

import net.minecraft.client.renderer.Tessellator;

public interface ISkeleton {
    ISkeleton EMPTY = new ISkeleton() {

        @Override
        public void setup(IPose pose) {
        }

        @Override
        public IBone getBoneByIndex(int index) {
            return IBone.STATIC_BONE;
        }

        @Override
        public void debugDraw(Tessellator tess) {
        }
    };

    IBone getBoneByIndex(int index);

    /**
     * Sets up the Skeleton for the animation given
     */
    void setup(IPose pose);

    /**
     * Added for debug, don't actually use this, especially when on the server
     *
     * @param tess
     */
    void debugDraw(Tessellator tess);
}
