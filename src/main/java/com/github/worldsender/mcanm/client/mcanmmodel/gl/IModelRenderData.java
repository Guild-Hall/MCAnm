package com.github.worldsender.mcanm.client.mcanmmodel.gl;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.github.worldsender.mcanm.client.IRenderPass;
import com.github.worldsender.mcanm.client.model.IModelStateInformation;

import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * Interface for ModelData.
 *
 * @author WorldSEnder
 */
@OnlyIn(Dist.CLIENT)
public interface IModelRenderData {
    /**
     * Renders the model from the given RenderPass.
     *
     * @param pass
     */
    void render(IRenderPass pass);

    /**
     * Get all texture slots of the model
     * @return
     */
    Set<String> getTextureSlots();

    /**
     * The totally inefficient method of minecraft to get block data
     *
     * @param currentPass
     * @param slotToTex
     * @return
     */
    List<BakedQuad> getAsBakedQuads(
            IModelStateInformation currentPass,
            Map<String, TextureAtlasSprite> slotToTex);
}
