package com.github.worldsender.mcanm.client.model;

import com.github.worldsender.mcanm.client.model.util.RenderPassInformation;

import net.minecraft.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public interface IEntityAnimator<T extends Entity> {
    IEntityAnimator<Entity> STATIC_ANIMATOR = new IEntityAnimator<Entity>() {
        @Override
        public IRenderPassInformation preRenderCallback(
                Entity entity,
                RenderPassInformation buffer,
                float partialTick) {
            return buffer;
        }

    };

    @SuppressWarnings("unchecked")
    static <T extends Entity> IEntityAnimator<T> STATIC_ENTITY() {
        // Actually safe cast, because T is only in, not out
        return (IEntityAnimator<T>) STATIC_ANIMATOR;
    }

    /**
     * Pre-render callback for the animator.<br>
     * This method should, when called, fill the given {@link IRenderPassInformation} with the correct values for the
     * current call and return it.<br>
     * Additional OpenGL transformations can be safely done, too (matrix is pushed). E.g. resizing the model, etc...
     *
     * @param entity            the entity being rendered
     * @param buffer            an {@link IRenderPassInformation} you can write to
     * @param subFrame          the current partial frame
     * @param matrixStackIn     the transformations to apply before rendering
     * @return the {@link IRenderPassInformation} to use for this pass. Most of the time return the passed in buffer
     * after setting your values
     */
    IRenderPassInformation preRenderCallback(
            T entity,
            RenderPassInformation buffer,
            float subFrame);
}
