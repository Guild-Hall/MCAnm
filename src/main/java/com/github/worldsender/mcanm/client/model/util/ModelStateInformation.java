package com.github.worldsender.mcanm.client.model.util;

import com.github.worldsender.mcanm.client.model.IModelStateInformation;
import com.github.worldsender.mcanm.common.animation.IAnimation;
import com.github.worldsender.mcanm.common.animation.IPose;
import com.github.worldsender.mcanm.common.animation.IAnimation.BoneTransformation;

import java.util.Objects;
import java.util.Optional;
import java.util.WeakHashMap;
import java.util.function.Predicate;

public class ModelStateInformation implements IModelStateInformation {
    /**
     * A suitable {@link Predicate} to return in {@link #getPartPredicate(float)} to render all parts without exception.
     * Note that this will not test if the currently executed animation wants to display the part.
     */
    public static final Predicate<String> RENDER_ALL = g -> true;
    /**
     * A suitable {@link Predicate} to return in {@link #getPartPredicate(float)} to render no parts without exception.
     * Note that this will not test if the currently executed animation wants to display the part.
     */
    public static final Predicate<String> RENDER_NONE = g -> false;

    /**
     * This is a default animation that never returns a pose for any bone it is asked for.
     */
    public static final IAnimation BIND_POSE = new IAnimation() {
        @Override
        public boolean storeCurrentTransformation(String bone, float frame, BoneTransformation transform) {
            return false;
        }
    };

    public static final IPose POSE_BIND_POSE = new IPose() {
        public boolean storeCurrentTransformation(String bone, BoneTransformation transform) {
            return false;
        };
    };

    private static class AnimationAsPose implements IPose {
        private IAnimation animation;
        private float frame;

        public AnimationAsPose() {
            reset();
        }

        private void reset() {
            animation = BIND_POSE;
            frame = 0.0F;
        }

        public void setAnimation(IAnimation anim) {
            this.animation = Objects.requireNonNull(anim);
        }

        public void setFrame(float frame) {
            this.frame = frame;
        }

        @Override
        public boolean storeCurrentTransformation(String bone, BoneTransformation transform) {
            return this.animation.storeCurrentTransformation(bone, this.frame, transform);
        }
    }

    private static class PoseHolder {
        private static WeakHashMap<Object, AnimationAsPose> POSE_PER_ENTITY;
        static {
            POSE_PER_ENTITY = new WeakHashMap<>();
        }

        private static AnimationAsPose getPoseFor(Object e) {
            return POSE_PER_ENTITY.computeIfAbsent(e, ee -> new AnimationAsPose());
        }

        private AnimationAsPose animProxy = getPoseFor(null);
        private IPose usedPose = POSE_BIND_POSE;

        public void useAnimationHolder(Object e) {
            this.animProxy = getPoseFor(e);
        }

        public void useAnimation(IAnimation animation) {
            this.animProxy.setAnimation(animation);
            this.usedPose = animProxy;
        }

        public void useAnimationFrame(float frame) {
            this.animProxy.setFrame(frame);
            this.usedPose = animProxy;
        }

        void usePose(IPose pose) {
            this.usedPose = Objects.requireNonNull(pose);
        }

        public IPose getUsedPose() {
            return usedPose;
        }
    }

    private Predicate<String> partPredicate;
    private PoseHolder pose = new PoseHolder();

    public ModelStateInformation() {
        this.reset();
    }

    public void reset() {
        reset(null);
    }

    public void reset(Object cacheHolder) {
        this.pose.useAnimationHolder(cacheHolder);
        this.setFrame(0F).setAnimation(Optional.empty()).setPartPredicate(Optional.empty());
    }

    @Override
    public boolean shouldRenderPart(String part) {
        return this.partPredicate.test(part);
    }

    /**
     * @param frame the frame to set
     */
    public ModelStateInformation setFrame(float frame) {
        this.pose.useAnimationFrame(frame);
        return this;
    }

    @Override
    public IPose getModelPose() {
        return this.pose.getUsedPose();
    }

    /**
     * @param animation the animation to set, Optional.empty() for bind pose
     */
    public ModelStateInformation setAnimation(Optional<IAnimation> animation) {
        return setAnimation(animation.orElse(BIND_POSE));
    }

    public ModelStateInformation setAnimation(IAnimation animation) {
        this.pose.useAnimation(animation);
        return this;
    }

    public ModelStateInformation setPose(Optional<IPose> pose) {
        return this.setPose(pose.orElse(POSE_BIND_POSE));
    }

    public ModelStateInformation setPose(IPose pose) {
        this.pose.usePose(pose);
        return this;
    }

    /**
     * @param partPredicate the partPredicate to set, Optional.empty() for RENDER_ALL
     */
    public ModelStateInformation setPartPredicate(Optional<Predicate<String>> partPredicate) {
        return setPartPredicate(partPredicate.orElse(RENDER_ALL));
    }

    public ModelStateInformation setPartPredicate(Predicate<String> partPredicate) {
        this.partPredicate = Objects.requireNonNull(partPredicate);
        return this;
    }

}
