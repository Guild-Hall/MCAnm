package com.github.worldsender.mcanm.common.util.math;

public class Point3d extends Tuple3d {

    public Point3d(double x, double y, double z) {
        super(x, y, z);
    }

    public Point3d(double[] p) {
        super(p);
    }

    public Point3d(Point3d p1) {
        super(p1);
    }

    public Point3d(Point3f p1) {
        super(p1);
    }

    public Point3d(Tuple3f t1) {
        super(t1);
    }

    public Point3d(Tuple3d t1) {
        super(t1);
    }

    public Point3d() {
        super();
    }

    public final double distanceSquared(Point3d p1) {
        double dx, dy, dz;

        dx = this.x - p1.x;
        dy = this.y - p1.y;
        dz = this.z - p1.z;
        return (dx * dx + dy * dy + dz * dz);
    }

    public final double distance(Point3d p1) {
        double dx, dy, dz;

        dx = this.x - p1.x;
        dy = this.y - p1.y;
        dz = this.z - p1.z;
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    public final double distanceL1(Point3d p1) {
        return Math.abs(this.x - p1.x) + Math.abs(this.y - p1.y) + Math.abs(this.z - p1.z);
    }

    public final double distanceLinf(Point3d p1) {
        double tmp;
        tmp = Math.max(Math.abs(this.x - p1.x), Math.abs(this.y - p1.y));

        return Math.max(tmp, Math.abs(this.z - p1.z));
    }

    public final void project(Point4d p1) {
        double oneOw;

        oneOw = 1 / p1.w;
        x = p1.x * oneOw;
        y = p1.y * oneOw;
        z = p1.z * oneOw;

    }

}
