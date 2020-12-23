package com.github.worldsender.mcanm.common.skeleton;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

import org.apache.commons.lang3.ArrayUtils;
import org.lwjgl.opengl.GL11;

import com.github.worldsender.mcanm.common.animation.IAnimation;
import com.github.worldsender.mcanm.common.animation.IPose;
import com.github.worldsender.mcanm.common.resource.IResource;
import com.github.worldsender.mcanm.common.resource.IResourceLocation;
import com.github.worldsender.mcanm.common.skeleton.parts.Bone;
import com.github.worldsender.mcanm.common.skeleton.parts.Bone.BoneBuilder;
import com.github.worldsender.mcanm.common.skeleton.stored.RawData;
import com.github.worldsender.mcanm.common.skeleton.visitor.IBoneVisitor;
import com.github.worldsender.mcanm.common.skeleton.visitor.ISkeletonVisitable;
import com.github.worldsender.mcanm.common.skeleton.visitor.ISkeletonVisitor;
import com.github.worldsender.mcanm.common.util.ReloadableData;
import com.github.worldsender.mcanm.common.util.math.Matrix4f;
import com.github.worldsender.mcanm.common.util.math.Quat4f;
import com.github.worldsender.mcanm.common.util.math.Vector3f;
import com.github.worldsender.mcanm.common.util.math.Vector4f;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;

public abstract class AbstractSkeleton extends ReloadableData<ISkeletonVisitable> implements ISkeleton {

    private static interface BoneManipulation {
        String layerName();

        void apply(IPose pose);
    }

    /**
     * Apply pose position
     */
    private class ApplyPose implements BoneManipulation {
        private Bone bone;
        private IAnimation.BoneTransformation transformationCache;

        public ApplyPose(Bone bone) {
            this.bone = Objects.requireNonNull(bone);
            this.transformationCache = new IAnimation.BoneTransformation();
        }

        @Override
        public String layerName() {
            return "pose(" + bone.name + ")";
        }

        @Override
        public void apply(IPose pose) {
            pose.storeCurrentTransformation(bone.name, this.transformationCache);
            Matrix4f transformMat = this.transformationCache.getMatrix();
            Matrix4f boneTransformMat = bone.globalToTransformedGlobal;
            boneTransformMat.mul(transformMat, bone.globalToLocal);
            boneTransformMat.mul(bone.localToGlobal, boneTransformMat);
        }
    }

    /**
     * Apply Parenting
     */
    private class ApplyParenting implements BoneManipulation {
        private Bone child, parent;

        public ApplyParenting(Bone child, Bone parent) {
            this.child = Objects.requireNonNull(child);
            this.parent = Objects.requireNonNull(parent);
        }

        @Override
        public String layerName() {
            return "\"" + child.name + "\".parent = \"" + parent.name + "\"";
        }

        @Override
        public void apply(IPose pose) {
            child.globalToTransformedGlobal.mul(parent.globalToTransformedGlobal, child.globalToTransformedGlobal);
        }
    }

    private class RecomputeNormalTransform implements BoneManipulation {
        private Bone bone;

        public RecomputeNormalTransform(Bone bone) {
            this.bone = Objects.requireNonNull(bone);
        }

        @Override
        public String layerName() {
            return "normals(" + bone.name + ")";
        }

        @Override
        public void apply(IPose pose) {
            Matrix4f pointMat = this.bone.globalToTransformedGlobal;
            Matrix4f normalMat = this.bone.globalToTransformedGlobalNormal;
            pointMat.getRotationScale(normalMat);
            normalMat.invert();
            normalMat.transpose();
        }
    }

    private static int doBFSSingleBone(int[] parents, int index, List<List<Integer>> layers, int[] layerNumbers) {
        if (index == 0xFF)
            return -1;
        if (layerNumbers[index] != -1)
            return layerNumbers[index];
        // Determine parent
        int parent = parents[index] & 0xFF;
        // Determine layer in tree and handle parent first
        int layerNbr = doBFSSingleBone(parents, parent, layers, layerNumbers) + 1;
        // Else handle
        layerNumbers[index] = layerNbr;
        // Get layer
        if (layers.size() <= layerNbr)
            layers.add(new ArrayList<Integer>());
        List<Integer> layer = layers.get(layerNbr);
        // Add current index
        layer.add(index);
        return layerNbr;
    }

    /**
     * Orders the bonesBreadthFirst in a breadth first order. This trusts in the bonesBreadthFirst having a tree-like
     * structure. Parent bones will always be ordered before their children
     *
     * @param src the bone
     * @return indices in an order that is breadth first.
     */
    private static int[] doBFSBoneOrdering(int[] parents) {
        List<List<Integer>> layers = new ArrayList<>();
        int[] layerNumber = new int[parents.length];
        Arrays.fill(layerNumber, -1);
        for (int i = 0; i < parents.length; ++i) {
            doBFSSingleBone(parents, i, layers, layerNumber);
        }
        int[] breadthFirst = new int[parents.length];
        int i = 0;
        for (List<Integer> layer : layers) {
            for (int b : layer) {
                breadthFirst[i++] = b;
            }
        }
        return breadthFirst;
    }

    private Bone[] bonesByIndex;
    private Map<String, Bone> bonesByName;
    private List<BoneManipulation> animationOrder;

    public AbstractSkeleton(IResourceLocation resLoc, Function<IResource, ISkeletonVisitable> readFunc) {
        super(resLoc, readFunc, RawData.MISSING_DATA);
    }

    @Override
    protected void preInit(Object... args) {
        bonesByName = new HashMap<>();
        animationOrder = new ArrayList<>();
    }

    @Override
    protected void loadData(ISkeletonVisitable data) {
        data.visitBy(this.new SkeletonVisitor());
    }

    @Override
    public IBone getBoneByIndex(int index) {
        return index < 0 || index >= bonesByIndex.length ? IBone.STATIC_BONE : bonesByIndex[index];
    }

    @Override
    public void setup(IPose pose) {
        for (BoneManipulation boneMod : animationOrder) {
            boneMod.apply(pose);
        }
    }

    @Override
    public void debugDraw(Tessellator tess) {
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glLineWidth(4.0f);
        BufferBuilder buffer = Tessellator.getInstance().getBuffer();
        buffer.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION);
        GL11.glColor4f(0f, 0f, 0f, 1f);
        for (Bone bone : bonesByIndex) {
            Vector4f tail = bone.getTail();
            Vector4f head = bone.getHead();
            buffer.pos(tail.x, tail.z, -tail.y).endVertex();
            buffer.pos(head.x, head.z, -head.y).endVertex();
        }
        tess.draw();
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
    }

    private class SkeletonVisitor implements ISkeletonVisitor {
        private List<Integer> parentIndices = new ArrayList<>();
        private List<Supplier<Bone>> boneSuppliers = new ArrayList<>();
        private Bone[] bones = null;

        @Override
        public IBoneVisitor visitBone(String name) {
            assert parentIndices.size() == boneSuppliers.size();
            final int boneIndex = parentIndices.size();
            parentIndices.add(boneIndex, -1);
            boneSuppliers.add(boneIndex, null);

            return new IBoneVisitor() {
                private int parentIndex = -1;
                private BoneBuilder builder = new BoneBuilder(name);

                @Override
                public void visitParent(int parentIndex) {
                    parentIndices.set(boneIndex, parentIndex);
                    this.parentIndex = parentIndex;
                }

                @Override
                public void visitLocalRotation(Quat4f rotation) {
                    builder.setRotation(rotation);
                }

                @Override
                public void visitLocalOffset(Vector3f headPosition) {
                    builder.setOffset(headPosition);
                }

                @Override
                public void visitEnd() {
                    if (parentIndex != -1) {
                        boneSuppliers.set(boneIndex, () -> builder.setParent(bones[parentIndex]).build());
                    } else {
                        boneSuppliers.set(boneIndex, builder::build);
                    }
                }
            };
        }

        @Override
        public void visitEnd() {
            assert parentIndices.size() == boneSuppliers.size();
            int size = boneSuppliers.size();
            int[] parentList = ArrayUtils.toPrimitive(parentIndices.toArray(new Integer[0]));
            int[] breadthFirstOrdering = doBFSBoneOrdering(parentList);

            AbstractSkeleton.this.bonesByIndex = bones = new Bone[size];
            AbstractSkeleton.this.bonesByName.clear();

            for (int i = 0; i < size; i++) {
                // We have to make the bone breadth first because the supplier accesses its parent bones
                int index = breadthFirstOrdering[i];
                Bone b = Objects.requireNonNull(boneSuppliers.get(index).get());
                bonesByIndex[index] = b;
                bonesByName.put(b.name, b);
            }

            List<BoneManipulation> animationOrder = AbstractSkeleton.this.animationOrder;
            animationOrder.clear();

            // add modifiers that copy the pose
            for (int i = 0; i < size; i++) {
                animationOrder.add(new ApplyPose(bones[i]));
            }
            // add parenting modifiers
            for (int i = 0; i < size; i++) {
                int index = breadthFirstOrdering[i];
                Bone bone = bones[index];
                if (parentList[index] < 0) {
                    continue;
                }
                Bone parent = bones[parentList[index]];
                animationOrder.add(new ApplyParenting(bone, parent));
            }
            // finally recompute normals
            for (int i = 0; i < size; i++) {
                animationOrder.add(new RecomputeNormalTransform(bones[i]));
            }
        }
    }

}
