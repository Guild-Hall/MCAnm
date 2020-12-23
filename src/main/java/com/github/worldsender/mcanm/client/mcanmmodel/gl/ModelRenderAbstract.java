package com.github.worldsender.mcanm.client.mcanmmodel.gl;

import com.github.worldsender.mcanm.client.IRenderPass;
import com.github.worldsender.mcanm.client.mcanmmodel.parts.IPart;
import com.github.worldsender.mcanm.client.mcanmmodel.parts.PartBuilder;
import com.github.worldsender.mcanm.client.mcanmmodel.visitor.*;
import com.github.worldsender.mcanm.client.model.IModelStateInformation;
import com.github.worldsender.mcanm.common.animation.IPose;
import com.github.worldsender.mcanm.common.skeleton.ISkeleton;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

public abstract class ModelRenderAbstract<P extends IPart> implements IModelRenderData {
    private final ISkeleton skeleton;
    private final Function<PartBuilder, P> buildingFunc;
    private IPart[] parts; // May have Random order
    public ModelRenderAbstract(IModelVisitable data, ISkeleton skeleton, Function<PartBuilder, P> buildingFunc) {
        this.skeleton = skeleton;
        this.buildingFunc = buildingFunc;
        ModelVisitor visitor = new ModelVisitor();
        data.visitBy(visitor);
    }

    /**
     * Sets up all bones for the following draw call. It is assumed that the bones are present in a breadth-first order
     * so that applying a transformation to a bone can already access the transformed parent of this bone
     *
     * @param anim
     *                 the pose currently executed
     */
    private void setupBones(IPose anim) {
        skeleton.setup(anim);
    }

    public void setup(IModelStateInformation currAnimation) {
        setupBones(currAnimation.getModelPose());
    }

    @Override
    public void render(IRenderPass currentPass) {
        setup(currentPass);
        for (IPart part : this.parts) {
            if (currentPass.shouldRenderPart(part.getName()))
                part.render(currentPass);
        }
        if (Minecraft.getMinecraft().gameSettings.showDebugInfo) {
            this.skeleton.debugDraw(Tessellator.getInstance());
        }
    }

    // Totally inefficient
    @Override
    public List<BakedQuad> getAsBakedQuads(
            IModelStateInformation currentPass,
            Map<String, TextureAtlasSprite> slotToTex,
            VertexFormat format) {
        List<BakedQuad> quads = new ArrayList<>();
        setup(currentPass);
        for (IPart part : this.parts) {
            if (currentPass.shouldRenderPart(part.getName()))
                part.getAsBakedQuads(slotToTex, format, quads);
        }
        return quads;
    }

    private class ModelVisitor implements IModelVisitor {
        List<IPart> parts = new ArrayList<>();

        @Override
        public void visitModelUUID(UUID uuid) {
        }

        @Override
        public void visitArtist(String artist) {
        }

        @Override
        public IPartVisitor visitPart(String name) {
            final int partIndex = parts.size();
            parts.add(null);
            return new IPartVisitor() {
                private PartBuilder builder = new PartBuilder().setSkeleton(skeleton).setName(name);

                @Override
                public void visitEnd() {
                    parts.set(partIndex, buildingFunc.apply(builder));
                }

                @Override
                public void visitTesselationPoint(TesselationPoint point) {
                    builder.addPoint(point);
                }

                @Override
                public void visitTesselationPoints(Iterable<TesselationPoint> points) {
                    builder.addPoints(points);
                }

                @Override
                public void visitFace(short tess1, short tess2, short tess3) {
                    builder.addFace(tess1, tess2, tess3);
                }

                @Override
                public void visitFaces(short[] tessOrder) {
                    builder.addIndices(tessOrder);
                }

                @Override
                public IMaterialVisitor visitTexture() {
                    return new IMaterialVisitor() {
                        @Override
                        public void visitTexture(String textureName) {
                            builder.setTexture(textureName);
                        }

                        @Override
                        public void visitEnd() {
                        }
                    };
                }
            };
        }

        @Override
        public void visitEnd() {
            ModelRenderAbstract.this.parts = this.parts.toArray(new IPart[0]);
        }
    }
}
