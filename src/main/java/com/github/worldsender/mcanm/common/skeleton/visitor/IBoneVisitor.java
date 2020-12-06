package com.github.worldsender.mcanm.common.skeleton.visitor;

import com.github.worldsender.mcanm.common.util.math.Quat4f;
import com.github.worldsender.mcanm.common.util.math.Vector3f;

public interface IBoneVisitor {
    void visitParent(int parentIndex);

    void visitLocalOffset(Vector3f headPosition);

    void visitLocalRotation(Quat4f rotation);

    void visitEnd();
}
