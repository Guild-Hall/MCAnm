package com.github.worldsender.mcanm.common.skeleton.stored;

import com.github.worldsender.mcanm.common.exceptions.ModelFormatException;
import com.github.worldsender.mcanm.common.skeleton.stored.parts.CopyRotation;
import com.github.worldsender.mcanm.common.skeleton.stored.parts.IConstraintVisitable;
import com.github.worldsender.mcanm.common.skeleton.stored.parts.RawBone;
import com.github.worldsender.mcanm.common.skeleton.stored.parts.CopyRotation.CoordinateSystem;
import com.github.worldsender.mcanm.common.skeleton.stored.parts.CopyRotation.MixMode;
import com.github.worldsender.mcanm.common.skeleton.visitor.IBoneVisitor;
import com.github.worldsender.mcanm.common.skeleton.visitor.ISkeletonVisitor;

import java.io.DataInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class RawDataV2 implements IVersionSpecificData {
    private static interface ConstraintLoader {
        IConstraintVisitable load(DataInputStream dis, RawBone[] bones) throws IOException;
    }

    private static Map<Integer, ConstraintLoader> KNOWN_CONSTRAINTS;
    static {
        KNOWN_CONSTRAINTS = new HashMap<>();
        int ID_COPY_ROT = ByteBuffer.wrap(new byte[] { 'C', 'P', 'Y', 'R' }).getInt();
        KNOWN_CONSTRAINTS.put(ID_COPY_ROT, RawDataV2::readCopyRotation);
    }

    private static CopyRotation readCopyRotation(DataInputStream dis, RawBone[] bones) throws IOException {
        CopyRotation constraint = new CopyRotation();
        constraint.controlledBoneIdx = dis.readInt();
        constraint.targetBoneIdx = dis.readInt();
        constraint.influence = dis.readFloat();
        int options = dis.readUnsignedByte();
        constraint.useX = (options & 0x1) != 0;
        constraint.useY = (options & 0x2) != 0;
        constraint.useZ = (options & 0x4) != 0;
        constraint.invertX = (options & 0x10) != 0;
        constraint.invertY = (options & 0x20) != 0;
        constraint.invertZ = (options & 0x40) != 0;
        constraint.mixMode = MixMode.decode(dis.readUnsignedByte());
        constraint.controlledSystem = CoordinateSystem.decode(dis.readUnsignedByte());
        constraint.targetSystem = CoordinateSystem.decode(dis.readUnsignedByte());
        return constraint;
    }

    private RawBone[] bones;
    private IConstraintVisitable[] constraints;

    public static final RawDataV2 loadFrom(DataInputStream dis) throws IOException, ModelFormatException {
        // This looks eerily similar to v1, but uses integer indices instead of unsigned bytes, hence almost no combined logic
        RawDataV2 data = new RawDataV2();

        int nbrBones = dis.readInt();
        if (nbrBones < 0) {
            throw new ModelFormatException("way too many bones (not fitting in a signed int)");
        }
        // Read bones
        RawBone[] bones = new RawBone[nbrBones];
        Set<String> boneNameSet = new HashSet<>();
        for (int i = 0; i < nbrBones; i++) {
            RawBone newBone = RawBone.readBoneFrom(dis);
            if (!boneNameSet.add(newBone.name))
                throw new ModelFormatException("Two bones with same name " + newBone.name);
            bones[i] = newBone;
        }
        readBoneParents(dis, bones); // Structure has to be tree-like
        IConstraintVisitable[] constraints = readConstraints(dis, bones);

        data.bones = bones;
        data.constraints = constraints;
        return data;
    }

    private static void readBoneParents(DataInputStream di, RawBone[] bones) throws IOException {
        int nbrBones = bones.length;
        for (RawBone bone : bones) {
            int parentIndex = di.readInt();
            if (parentIndex != 0xFFFFFFFF && parentIndex >= nbrBones) {
                throw new ModelFormatException(
                        String.format("ParentIndex (%d) has to be smaller than nbrBones (%d).", parentIndex, nbrBones));
            }
            bone.parent = parentIndex;
        }
    }

    private static IConstraintVisitable[] readConstraints(DataInputStream dis, RawBone[] bones) throws IOException {
        int nbrConstraints = dis.readInt();
        if (nbrConstraints < 0) {
            throw new ModelFormatException("way too many bones (not fitting in a signed int)");
        }
        IConstraintVisitable[] constraints = new IConstraintVisitable[nbrConstraints];
        for (int i = 0; i < nbrConstraints; ++i) {
            constraints[i] = readConstraint(dis, bones);
        }
        return constraints;
    }

    private static IConstraintVisitable readConstraint(DataInputStream dis, RawBone[] bones) throws IOException {
        int id = dis.readInt();
        ConstraintLoader loader = KNOWN_CONSTRAINTS.get(Integer.valueOf(id));
        if (loader == null) {
            ByteBuffer buff = ByteBuffer.allocate(4);
            buff.putInt(id);
            buff.rewind();
            String idAsString = new String(buff.array(), StandardCharsets.ISO_8859_1);
            throw new ModelFormatException("encountered unknown constraint type '" + idAsString + "'");
        }
        return loader.load(dis, bones);
    }

    @Override
    public void visitBy(ISkeletonVisitor visitor) {
        for (RawBone bone : bones) {
            IBoneVisitor boneVisitor = visitor.visitBone(bone.name);
            if (bone.parent != 0xFF) {
                boneVisitor.visitParent(bone.parent);
            }
            boneVisitor.visitLocalOffset(bone.offset);
            boneVisitor.visitLocalRotation(bone.rotation);
            boneVisitor.visitEnd();
        }
        for (IConstraintVisitable constraint : constraints) {
            constraint.visitBy(visitor);
        }
    }

}
