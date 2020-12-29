package com.github.worldsender.mcanm.common.util.math;

public class EulerAngles extends Vector3f {
    public static enum EulerOrder {
        XYZ(0, 1, 2, false);

        // The index of the axis in order 012
        public final int i, j, k;
        // If we have to do parity correction due to the axis indices being an odd permutation
        public boolean parity;

        private EulerOrder(int i, int j, int k, boolean parity) {
            this.i = i;
            this.j = j;
            this.k = k;
            this.parity = parity;
        }
    }

    private static final double SQRT2 = Math.sqrt(2);

    /**
     * Returns the argument for convenience
     */
    public Quat4f toQuaternion(Quat4f quat, EulerOrder order) {
        int i = order.i, j = order.j, k = order.k;
        double ti, tj, th, ci, cj, ch, si, sj, sh, cc, cs, sc, ss;
        float e[] = { x, y, z };

        ti = e[i] * 0.5f;
        tj = e[j] * (order.parity ? -0.5f : 0.5f);
        th = e[k] * 0.5f;

        ci = Math.cos(ti);
        cj = Math.cos(tj);
        ch = Math.cos(th);
        si = Math.sin(ti);
        sj = Math.sin(tj);
        sh = Math.sin(th);

        cc = ci * ch;
        cs = ci * sh;
        sc = si * ch;
        ss = si * sh;

        double a[] = { 0, 0, 0 };
        a[i] = cj * sc - sj * cs;
        a[j] = cj * ss + sj * cc;
        a[k] = cj * cs - sj * sc;

        if (order.parity) {
            a[j] = -a[j];
        }

        float q[] = { 0, 0, 0, 0 };
        q[0] = (float) (cj * cc + sj * ss);
        q[1] = (float) (a[0]);
        q[2] = (float) (a[1]);
        q[3] = (float) (a[2]);

        quat.w = q[0];
        quat.x = q[1];
        quat.y = q[2];
        quat.z = q[3];
        return quat;
    }

    public void fromQuaternion(Quat4f q, EulerOrder order) {
        // Step 1, convert quat to normalized 3x3 matrix
        double q0, q1, q2, q3, qda, qdb, qdc, qaa, qab, qac, qbb, qbc, qcc;
        float m[][] = new float[3][3];

        q0 = SQRT2 * (double) q.w;
        q1 = SQRT2 * (double) q.x;
        q2 = SQRT2 * (double) q.y;
        q3 = SQRT2 * (double) q.z;

        qda = q0 * q1;
        qdb = q0 * q2;
        qdc = q0 * q3;
        qaa = q1 * q1;
        qab = q1 * q2;
        qac = q1 * q3;
        qbb = q2 * q2;
        qbc = q2 * q3;
        qcc = q3 * q3;

        m[0][0] = (float) (1.0 - qbb - qcc);
        m[0][1] = (float) (qdc + qab);
        m[0][2] = (float) (-qdb + qac);

        m[1][0] = (float) (-qdc + qab);
        m[1][1] = (float) (1.0 - qaa - qcc);
        m[1][2] = (float) (qda + qbc);

        m[2][0] = (float) (qdb + qac);
        m[2][1] = (float) (-qda + qbc);
        m[2][2] = (float) (1.0 - qaa - qbb);
        // Step 2, convert matrix to euler angles.
        int i = order.i, j = order.j, k = order.k;

        double cy = Math.hypot(m[i][i], m[i][j]);
        // There are two solutions, see at the end which one is closest
        double[] eul1 = { 0, 0, 0 }, eul2 = { 0, 0, 0 };
        double parity = order.parity ? -1 : 1;

        if (cy > 16.0f * Float.MIN_NORMAL) {
            eul1[i] = parity * Math.atan2(m[j][k], m[k][k]);
            eul1[j] = parity * Math.atan2(-m[i][k], cy);
            eul1[k] = parity * Math.atan2(m[i][j], m[i][i]);

            eul2[i] = parity * Math.atan2(-m[j][k], -m[k][k]);
            eul2[j] = parity * Math.atan2(-m[i][k], -cy);
            eul2[k] = parity * Math.atan2(-m[i][j], -m[i][i]);
        } else {
            eul1[i] = eul2[i] = parity * Math.atan2(-m[k][j], m[j][j]);
            eul1[j] = eul2[j] = parity * Math.atan2(-m[i][k], cy);
            eul1[k] = eul2[k] = 0;
        }

        //Step 3, find out which of these results is closest to the current value
        // !! not what blender does !!
        // see https://developer.blender.org/diffusion/B/browse/master/source/blender/blenlib/intern/math_rotation.c
        // #compatible_eul
        // #mat3_normalized_to_compatible_eulO
        double diff1 = getMinimalAbsAngleDiff(eul1[0], this.x) //
                + getMinimalAbsAngleDiff(eul1[1], this.y) //
                + getMinimalAbsAngleDiff(eul1[2], this.z);
        double diff2 = getMinimalAbsAngleDiff(eul2[0], this.x) //
                + getMinimalAbsAngleDiff(eul2[1], this.y) //
                + getMinimalAbsAngleDiff(eul2[2], this.z);
        if (diff1 < diff2) {
            this.x = (float) eul1[0];
            this.y = (float) eul1[1];
            this.z = (float) eul1[2];
        } else {
            this.x = (float) eul2[0];
            this.y = (float) eul2[1];
            this.z = (float) eul2[2];
        }
    }

    private static double getMinimalAbsAngleDiff(double a1, double a2) {
        double diff = Math.abs((a1 - a2) % (2 * Math.PI));
        // diff is in [0, 2pi)
        if (diff > Math.PI) {
            return 2 * Math.PI - diff;
        }
        return diff;
    }

}
