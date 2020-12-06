package com.github.worldsender.mcanm.client.mcanmmodel.stored.parts;

import java.io.DataInputStream;
import java.io.IOException;

import com.github.worldsender.mcanm.common.Utils;
import com.github.worldsender.mcanm.common.util.math.Quat4f;
import com.github.worldsender.mcanm.common.util.math.Vector3f;

public class RawBone {
    public String name;
    public Quat4f rotation;
    public Vector3f offset;
    /**
     * Parent of this bone as array index. A value of 0xFF means no parent
     */
    public int parent;

    public static RawBone readBoneFrom(DataInputStream dis) throws IOException {
        RawBone bone = new RawBone();
        String name = Utils.readString(dis);
        Quat4f quat = Utils.readQuat(dis);
        Vector3f offset = Utils.readVector3f(dis);
        bone.name = name;
        bone.rotation = quat;
        bone.offset = offset;
        return bone;
    }

}
