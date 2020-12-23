package com.github.worldsender.mcanm.common.animation;

/**
 * A pose a skeleton is in. Conceptually, this is animation + frame, but has internal buffers for the transforms where
 * they are precomputed.
 */
public interface IPose {
    /**
     * Stores the bone's current {@link BoneTransformation} in <code>transform</code> (identified by name). <br>
     * If the requested bone is not known to the pose, <code>transform</code> is left untouched and this return
     * <code>false</code>. Otherwise <code>transform</code> is set to the bone's current transform state.
     *
     * @param bone
     *                      the name of the bone which matrix is requested
     * @param transform
     *                      the transform to set
     * @return if a transformation has been set
     */
    boolean storeCurrentTransformation(String bone, IAnimation.BoneTransformation transform);
}
