package com.github.worldsender.mcanm.common.skeleton.stored.parts;

import com.github.worldsender.mcanm.common.skeleton.visitor.IConstraintVisitor;

public interface IConstraintVisitable {

    void visitBy(IConstraintVisitor visitor);

}
