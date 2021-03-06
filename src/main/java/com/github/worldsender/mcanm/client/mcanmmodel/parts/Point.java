package com.github.worldsender.mcanm.client.mcanmmodel.parts;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.github.worldsender.mcanm.client.IRenderPass;
import com.github.worldsender.mcanm.client.mcanmmodel.visitor.BoneBinding;
import com.github.worldsender.mcanm.client.mcanmmodel.visitor.TesselationPoint;
import com.github.worldsender.mcanm.common.skeleton.IBone;
import com.github.worldsender.mcanm.common.skeleton.ISkeleton;
import com.github.worldsender.mcanm.common.util.math.Point4f;
import com.github.worldsender.mcanm.common.util.math.Tuple2f;
import com.github.worldsender.mcanm.common.util.math.Tuple3f;
import com.github.worldsender.mcanm.common.util.math.Tuple4f;
import com.github.worldsender.mcanm.common.util.math.Vector2f;
import com.github.worldsender.mcanm.common.util.math.Vector3f;
import com.github.worldsender.mcanm.common.util.math.Vector4f;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.pipeline.IVertexConsumer;

@OnlyIn(Dist.CLIENT)
public class Point {
    protected final Vertex vert;
    protected final Vertex transformed;

    protected Point(Vector3f pos, Vector3f norm, Vector2f uv) {
        this.vert = new Vertex(new Vector4f(pos.x, pos.y, pos.z, 1F), norm, uv);
        this.transformed = new Vertex(this.vert);
    }

    protected Vertex setupTransformed() {
        this.transformed.setRetainUV(this.vert);
        return this.transformed;
    }

    /**
     * Constructs a bone from the {@link TesselationPoint} given. This is implemented in the factory style to
     * efficiently handle bones without Bindings
     *
     * @param data the point to construct from
     * @return the constructed point
     */
    public static Point from(TesselationPoint data, ISkeleton skelet) {
        boolean isBound = data.boneBindings.length > 0;
        if (isBound)
            return new BoundPoint(data.coords, data.normal, data.texCoords, data.boneBindings, skelet);
        return new Point(data.coords, data.normal, data.texCoords);
    }

    /**
     * Renders this point, already transformed
     */
	public void render(IVertexBuilder buffer, IRenderPass renderPass) {
        Vertex transformedVertex = setupTransformed();
        transformedVertex.globalTransform(renderPass.getActiveMatrixStack().getLast());
        transformedVertex.render(buffer, renderPass);
	}

    public void putIntoBakedQuadBuilder(IVertexConsumer consumer, TextureAtlasSprite tex) {
        Vertex transformed = setupTransformed();
        Tuple4f positionBuffer = new Vector4f();
        transformed.getPosition(positionBuffer);
        Tuple3f normalBuffer = new Vector3f();
        transformed.getNormal(normalBuffer);
        Tuple2f uvBuffer = new Vector2f();
        transformed.getUV(uvBuffer);

        VertexFormat vertexFormat = consumer.getVertexFormat();
        for (int e = 0; e < vertexFormat.getElements().size(); ++e) {
            VertexFormatElement element = vertexFormat.getElements().get(e);
            switch (element.getUsage()) {
                case POSITION:
                    consumer.put(e, positionBuffer.x, positionBuffer.y, positionBuffer.z, positionBuffer.w);
                    break;
                case NORMAL:
                    consumer.put(e, normalBuffer.x, normalBuffer.y, normalBuffer.z, 0);
                    break;
                case COLOR:
                    consumer.put(e, 1, 1, 1, 1);
                    break;
                case UV:
                    if (element.getIndex() == 0) {
                        consumer.put(e, tex.getInterpolatedU(uvBuffer.x * 16), tex.getInterpolatedV(uvBuffer.y * 16), 0, 1);
                        break;
                    }
                    // FALLTHROUGH
                default:
                    consumer.put(e);
            }
        }
    }

    private static class BoundPoint extends Point {
        private List<Binding> binds;

        public BoundPoint(Vector3f pos, Vector3f norm, Vector2f uv, BoneBinding[] readBinds, ISkeleton skelet) {
            super(pos, norm, uv);
            // readBinds can be assumed to at least be size 1
            this.binds = new ArrayList<>();
            float strengthSummed = 0.0F;
            for (BoneBinding bind : readBinds) {
                if (bind.bindingValue <= 0.0f)
                    continue;
                Binding binding = new Binding(
                        skelet.getBoneByIndex(Byte.toUnsignedInt(bind.boneIndex)),
                        bind.bindingValue);
                this.binds.add(binding);
                strengthSummed += bind.bindingValue;
            }
            for (Binding bind : this.binds) {
                bind.normalize(strengthSummed);
            }
        }

        @Override
        protected Vertex setupTransformed() {
            Vertex base = this.vert;
            Vertex transformed = super.setupTransformed();
            transformed.retainUVOnly();
            for (Binding bind : this.binds) {
                bind.addTransformed(base, transformed);
            }
            return transformed;
        }

        private static class Binding {
            // Used as a buffer, doesn't requite to always create new
            // temporaries. Prevents parallelization, though
            private static Point4f posBuff = new Point4f();
            private static Vector3f normBuff = new Vector3f();

            private IBone bone;
            private float strength;

            public Binding(IBone bone, float strenght) {
                this.bone = Objects.requireNonNull(bone);
                this.strength = strenght;
            }

            /**
             * Computes the transformed and weighted position of the given vertex. Adds that to the target vertex.
             *
             * @param base the vertex to transform
             * @param trgt the vertex to add to. If null is given, it is just assigned to
             * @return the final vertex, a new vertex if <code>null</code> was given
             */
            public void addTransformed(Vertex base, Vertex trgt) {
                Objects.requireNonNull(base);
                Objects.requireNonNull(trgt);

                base.getPosition(posBuff);
                base.getNormal(normBuff);
                // Transform points with matrix
                this.bone.transform(posBuff);
                this.bone.transformNormal(normBuff);

                posBuff.scale(this.strength);
                normBuff.scale(this.strength);

                trgt.offset(posBuff);
                trgt.addNormal(normBuff);
            }

            public void normalize(float sum) {
                this.strength /= sum;
            }
        }
    }
}