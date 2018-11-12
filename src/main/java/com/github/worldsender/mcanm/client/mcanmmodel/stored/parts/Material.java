package com.github.worldsender.mcanm.client.mcanmmodel.stored.parts;

import com.github.worldsender.mcanm.common.Utils;

import java.io.DataInputStream;
import java.io.IOException;

public class Material {
    public String resLocationRaw;

    public static Material readFrom(DataInputStream di) throws IOException {
        Material tex = new Material();
        tex.resLocationRaw = Utils.readString(di);
        return tex;
    }
}
