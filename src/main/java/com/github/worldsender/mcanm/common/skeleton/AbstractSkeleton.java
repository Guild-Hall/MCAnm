package com.github.worldsender.mcanm.common.skeleton;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.lwjgl.opengl.GL11;

import com.github.worldsender.mcanm.MCAnm;
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

    private static interface BoneConstraint extends BoneManipulation {
        int getControlledBoneIndex();

        List<Integer> getConstraintDependencies();
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

        public ApplyParenting(Bone child) {
            this.child = Objects.requireNonNull(child);
            this.parent = Objects.requireNonNull(child.parent);
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

    private class CopyRotationManip implements BoneConstraint {
        private Bone target, controlled;
        private CopyRotation rawConstraint;

        public CopyRotationManip(CopyRotation rawConstraint, Bone[] bones) {
            this.target = bones[rawConstraint.targetBoneIdx];
            this.controlled = bones[rawConstraint.controlledBoneIdx];
            this.rawConstraint = rawConstraint;
        }

        @Override
        public String layerName() {
            return "copyRot(" + target.name + " -> " + controlled.name + ")";
        }

        @Override
        public int getControlledBoneIndex() {
            return this.rawConstraint.controlledBoneIdx;
        }

        @Override
        public List<Integer> getConstraintDependencies() {
            return Arrays.asList(new Integer[] { this.rawConstraint.targetBoneIdx });
        }

        @Override
        public void apply(IPose pose) {
        }
    }

    private static int doBFSSingleBone(List<List<Integer>> parents, int index, int[] layerCount, int[] layerNumbers) {
        if (layerNumbers[index] == -2) {
            MCAnm.logger().debug("recursive dependency in parenting/constraints, results can be unpredictable");
            return 0;
        }
        if (layerNumbers[index] != -1)
            return layerNumbers[index];
        // trap the index for the recursive calls
        layerNumbers[index] = -2;
        // Determine parent
        List<Integer> parentsOfIndex = parents.get(index);
        // Determine layer in tree and handle parents first
        int layerNbr = 0;
        for (int parent : parentsOfIndex) {
            int parentLayer = doBFSSingleBone(parents, parent, layerCount, layerNumbers);
            layerNbr = Math.max(layerNbr, parentLayer + 1);
        }
        // Else handle
        layerNumbers[index] = layerNbr;
        layerCount[layerNbr]++;
        return layerNbr;
    }

    /**
     * Orders the bonesBreadthFirst in a breadth first order. This trusts in the bonesBreadthFirst having a tree-like
     * structure. Parent bones will always be ordered before their children
     *
     * @param src the bone
     * @return indices in an order that is breadth first.
     */
    private static int[] doBFSBoneOrdering(List<List<Integer>> parents) {
        int[] layerNumbers = new int[parents.size()];
        int[] layerCount = new int[parents.size()];
        Arrays.fill(layerNumbers, -1);
        for (int i = 0; i < parents.size(); ++i) {
            doBFSSingleBone(parents, i, layerCount, layerNumbers);
        }
        int[] breadthFirst = new int[parents.size()];

        // reuse layerCount for layer starts, compute prefix sums
        int[] layerStart = layerCount;
        for (int i = 0, start = 0; i < layerStart.length; i++) {
            int tmpStart = start;
            start += layerStart[i];
            layerStart[i] = tmpStart;
        }
        // assign bones to indices
        for (int b = 0; b < parents.size(); b++) {
            int layer = layerNumbers[b];
            int layerStartIndex = layerStart[layer];
            layerStart[layer]++;
            breadthFirst[layerStartIndex] = b;
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
        private List<Supplier<BoneConstraint>> constraints = new ArrayList<>();

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
            List<List<Integer>> parentList = parentIndices.stream().map(p -> {
                if (p == -1)
                    return Collections.<Integer>emptyList();
                return Collections.singletonList(p);
            }).collect(Collectors.toList());
            int[] parentBFSOrdering = doBFSBoneOrdering(parentList);

            AbstractSkeleton.this.bonesByIndex = bones = new Bone[size];
            AbstractSkeleton.this.bonesByName.clear();

            for (int i = 0; i < size; i++) {
                // We have to make the bone breadth first because the supplier accesses its parent bones
                int index = parentBFSOrdering[i];
                Bone b = Objects.requireNonNull(boneSuppliers.get(index).get());
                bonesByIndex[index] = b;
                bonesByName.put(b.name, b);
            }

            List<BoneManipulation> animationOrder = AbstractSkeleton.this.animationOrder;
            animationOrder.clear();

            // each bone contains a local transformation, computed as
            // b -> b.globalToLocal @ b.globalToTransformedGlobal @ b.localToGlobal

            // add modifiers that copy the pose
            for (int i = 0; i < size; i++) {
                animationOrder.add(new ApplyPose(bones[i]));
            }
            // after this "layer", the local transformation is what is keyframed
            // and the blender doc calls "Local Space"

            // Now, generate the read constraints
            List<List<BoneConstraint>> boneConstraints = new ArrayList<>();
            List<List<Integer>> modifierDeps = new ArrayList<>();
            for (int i = 0; i < size; ++i) {
                boneConstraints.add(new ArrayList<>());
                modifierDeps.add(new ArrayList<>(parentList.get(i)));
            }

            for (Supplier<BoneConstraint> bCon : this.constraints) {
                BoneConstraint con = bCon.get();
                int controlledBoneIndex = con.getControlledBoneIndex();
                boneConstraints.get(controlledBoneIndex).add(con);
                modifierDeps.get(controlledBoneIndex).addAll(con.getConstraintDependencies());
            }
            int[] modifierBFSOrder = doBFSBoneOrdering(modifierDeps);

            // add parenting modifiers & constraints. Have to be interleaved to generate correct
            // transformations for children of constrained bones.
            for (int i = 0; i < size; i++) {
                int index = modifierBFSOrder[i];
                Bone bone = bones[index];

                if (bone.parent != null) {
                    // parentList consists of empty or singleton list.
                    // man do I wish I could write doBFSBoneOrdering for any traversible container...
                    animationOrder.add(new ApplyParenting(bone));
                }
                animationOrder.addAll(boneConstraints.get(index));
            }
            // after this "layer", the local transformation is what the blender doc calls
            // in "Local with Parent" space

            // finally recompute normals
            for (int i = 0; i < size; i++) {
                animationOrder.add(new RecomputeNormalTransform(bones[i]));
            }
        }
    }

}
