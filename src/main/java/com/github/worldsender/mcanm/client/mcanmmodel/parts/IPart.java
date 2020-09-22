package com.github.worldsender.mcanm.client.mcanmmodel.parts;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.github.worldsender.mcanm.client.IRenderPass;

import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public interface IPart {

    void render(IRenderPass currentPass);

    /**
     * The totally inefficient way minecraft wants us to render item models.
     *
     * @param slotToTex
     * @param out
     */
    void getAsBakedQuads(Map<String, TextureAtlasSprite> slotToTex, List<BakedQuad> out);

    Set<String> getTextureSlots();

    String getName();
}
