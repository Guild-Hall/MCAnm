package com.github.worldsender.mcanm.client.mcanmmodel.parts;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.IntStream;

import com.github.worldsender.mcanm.client.IRenderPass;
import com.github.worldsender.mcanm.client.mcanmmodel.visitor.TesselationPoint;
import com.github.worldsender.mcanm.common.util.math.Tuple3f;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import org.lwjgl.opengl.GL30;

import net.minecraft.client.renderer.RenderState;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.pipeline.BakedQuadBuilder;

@OnlyIn(Dist.CLIENT)
public class PartDirect implements IPart {
    private static RenderType getPartRenderType(ResourceLocation texture) {
        RenderType.State renderState = RenderType.State.getBuilder()
            .texture(new RenderState.TextureState(texture, false, false))
            // .transparency(new RenderState.TransparencyState(false))
            .diffuseLighting(new RenderState.DiffuseLightingState(true))
            .lightmap(new RenderState.LightmapState(true))
            .overlay(new RenderState.OverlayState(true))
            .build(true);
        return RenderType.makeType("entity_solid_tris", DefaultVertexFormats.ENTITY, GL30.GL_TRIANGLES, 256, true, false, renderState);
    }

    private final String textureSlot;
    private final String name;
    private final String textureSlotWithOct;
    private final Point[] pointsList;
    private final int[] indices;

    public PartDirect(PartBuilder builder) {
        Point[] points = new Point[builder.pointList.size()];
        int idx = 0;
        for (TesselationPoint point : builder.pointList) {
            points[idx++] = Point.from(point, builder.skeleton);
        }
        short[] indices = new short[builder.indexBuf.readableBytes() / 2];
        builder.indexBuf.nioBuffer().asShortBuffer().get(indices);
        for (short i : indices) {
            if (i < 0 || i >= points.length) {
                throw new IllegalArgumentException(
                        "face index " + i + " too big. Only " + points.length + " points available");
            }
        }
        this.pointsList = points;
        this.name = Objects.requireNonNull(builder.name, "A name is required");
        this.textureSlot = Objects.requireNonNull(builder.textureName, "texture name required");
        this.textureSlotWithOct = "#" + textureSlot;
        // Required for the stupid item rendering...
        this.indices = IntStream.range(0, indices.length).map(i -> indices[i]).toArray();
    }

    @Override
    public void render(IRenderPass currentPass) {
        ResourceLocation texture = currentPass.getActualResourceLocation(textureSlot);
        RenderType renderType = getPartRenderType(texture);
        IVertexBuilder buffer = currentPass.getRenderTypeBuffer().getBuffer(renderType);
        for (int i = 0; i < indices.length; i += 3) {
            Point point1 = pointsList[indices[i]];
            Point point2 = pointsList[indices[i + 1]];
            Point point3 = pointsList[indices[i + 2]];
            point1.render(buffer, currentPass);
            point2.render(buffer, currentPass);
            point3.render(buffer, currentPass);
        }
    }

    @Override
    public void getAsBakedQuads(Map<String, TextureAtlasSprite> slotToTex, List<BakedQuad> out) {
        TextureAtlasSprite tex = retrieveSprite(slotToTex);
        for (int i = 0; i < indices.length; i += 3) {
            Point point1 = pointsList[indices[i]];
            Point point2 = pointsList[indices[i + 1]];
            Point point3 = pointsList[indices[i + 2]];
            BakedQuadBuilder builder = new BakedQuadBuilder(tex);

            Tuple3f normal = new Tuple3f();
            point1.vert.getNormal(normal);
            builder.setQuadOrientation(Direction.getFacingFromVector(normal.x, normal.y, normal.z));

            point1.putIntoBakedQuadBuilder(builder);
            point2.putIntoBakedQuadBuilder(builder);
            point3.putIntoBakedQuadBuilder(builder);
            point1.putIntoBakedQuadBuilder(builder);

            out.add(builder.build());
        }
    }

    private TextureAtlasSprite retrieveSprite(Map<String, TextureAtlasSprite> slotToTex) {
        return slotToTex.get(textureSlotWithOct);
    }

    @Override
    public Set<String> getTextureSlots() {
        return Collections.singleton(textureSlotWithOct);
    }

    @Override
    public String getName() {
        return this.name;
    }
}
