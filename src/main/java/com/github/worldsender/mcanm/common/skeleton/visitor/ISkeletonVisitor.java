package com.github.worldsender.mcanm.common.skeleton.visitor;

public interface ISkeletonVisitor extends IConstraintVisitor {
    IBoneVisitor visitBone(String name);

    void visitEnd();
}
