package com.github.worldsender.mcanm.client.renderer;

import com.github.worldsender.mcanm.client.mcanmmodel.IModel;
import com.github.worldsender.mcanm.client.model.IEntityAnimator;
import com.github.worldsender.mcanm.client.model.IRenderPassInformation;
import com.github.worldsender.mcanm.client.model.util.RenderPassInformation;
import com.github.worldsender.mcanm.client.renderer.entity.RenderAnimatedModel;

import net.minecraft.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * An animatable type that is typically rendered with a {@link RenderAnimatedModel} or a //TODO: RenderMHFCModelTile<br>
 * This is a pretty simple type of entity that doesn't have much setup to do, apart from setting an animation.
 *
 * @author WorldSEnder
 */
@OnlyIn(Dist.CLIENT)
public interface IAnimatedObject {
    /**
     * An animator that can be used in
     * {@link RenderAnimatedModel#fromModel(IEntityAnimator, IModel, float)} when the
     * animated entity implements the {@link IAnimatedObject} interface.
     */
    static <T extends Entity & IAnimatedObject> IEntityAnimator<T> ANIMATOR_ADAPTER() {
        return new IEntityAnimator<T>() {
            @Override
            public IRenderPassInformation preRenderCallback(
                    T entity,
                    RenderPassInformation buffer,
                    float partialTick) {
                return entity.preRenderCallback(partialTick, buffer);
            }
        };
    }

    /**
     * A callback to the animated object just before it is rendered. The method is always called BEFORE. The object
     * should setup and return the {@link RenderPassInformation} given as callback (never null).
     *
     * @param subFrame the current subFrame (subTick)
     * @param callback a {@link RenderPassInformation} to prepare
     * @return A {@link RenderPassInformation} to use in the current pass, not null
     */
    RenderPassInformation preRenderCallback(float subFrame, RenderPassInformation callback);
}
