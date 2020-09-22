package com.github.worldsender.mcanm.client.model;

import com.github.worldsender.mcanm.client.IRenderPass;
import com.github.worldsender.mcanm.client.mcanmmodel.IModel;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.Model;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * A general purpose model that should fulfill most of your needs. It is possible to use an {@link IEntityAnimator} to
 * determine the rendered
 *
 * @author WorldSEnder
 */
@OnlyIn(Dist.CLIENT)
public class ModelAnimated extends Model {
    private IModel model;

    /**
     * This constructor just puts the model into itself. Nothing is checked
     *
     * @param model the model to render
     */
    public ModelAnimated(IModel model) {
        super(RenderType::getEntityCutoutNoCull);
        this.model = model; // No null-checks, getters could be overridden
        // Useless piece of .... sklsdalsafhkjasd
        // So we don't get problems with arrows in our entity.
        // I want to kill the programmer who thought it would be a good idea
        // not to let the entity decide where to put the arrow
        ModelRenderer argggghhhh = new ModelRenderer(this, 0, 0);
        argggghhhh.addBox(0, 0, 0, 1, 1, 1);
    }

    @Override
    public void render(MatrixStack matrixStack, IVertexBuilder bufferIn, int packedLightIn, int packedOverlayIn,
            float red, float green, float blue, float alpha) {
        throw new IllegalStateException("unused, don't use render to render these kinds of models, use #renderWithPass");
    }

    public void renderWithPass(IRenderPass renderPass) {
        getModel().render(renderPass);
    }

    /**
     * The current model is accessed via this getter. If a subclass chooses to override this, the returned model will be
     * rendered.
     *
     * @return
     */
    protected IModel getModel() {
        return this.model;
    }
}
