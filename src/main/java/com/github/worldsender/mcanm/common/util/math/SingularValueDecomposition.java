/*
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license agreements. See the NOTICE
 * file distributed with this work for additional information regarding copyright ownership. The ASF licenses this file
 * to You under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0 Unless required by
 * applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
// Includes a lot of changes to modify the svd for Matrix4f
package com.github.worldsender.mcanm.common.util.math;

/**
 * Calculates the compact Singular Value Decomposition of a matrix.
 * <p>
 * The Singular Value Decomposition of matrix A is a set of three matrices: U, &Sigma; and V such that A = U &times;
 * &Sigma; &times; V<sup>T</sup>. Let A be a m &times; n matrix, then U is a m &times; p orthogonal matrix, &Sigma; is a
 * p &times; p diagonal matrix with positive or null elements, V is a p &times; n orthogonal matrix (hence V<sup>T</sup>
 * is also orthogonal) where p=min(m,n).
 * </p>
 * <p>
 * This class is similar to the class with similar name from the
 * <a href="http://math.nist.gov/javanumerics/jama/">JAMA</a> library, with the following changes:
 * </p>
 * <ul>
 * <li>the {@code norm2} method which has been renamed as {@link #getNorm() getNorm},</li>
 * <li>the {@code cond} method which has been renamed as {@link #getConditionNumber() getConditionNumber},</li>
 * <li>the {@code rank} method which has been renamed as {@link #getRank() getRank},</li>
 * <li>a {@link #getUT() getUT} method has been added,</li>
 * <li>a {@link #getVT() getVT} method has been added,</li>
 * <li>a {@link #getSolver() getSolver} method has been added,</li>
 * <li>a {@link #getCovariance(double) getCovariance} method has been added.</li>
 * </ul>
 * 
 * @see <a href="http://mathworld.wolfram.com/SingularValueDecomposition.html">MathWorld</a>
 * @see <a href="http://en.wikipedia.org/wiki/Singular_value_decomposition">Wikipedia</a>
 * @since 2.0 (changed to concrete class in 3.0)
 */
public class SingularValueDecomposition {
    private static final long EXPONENT_OFFSET = 1023l;
    /**
     * The smallest double such that 1 / SAFE_MIN does not overflow.
     */
    public static final double SAFE_MIN;
    static {
        /*
         * This was previously expressed as = 0x1.0p-1022;
         * However, OpenJDK (Sparc Solaris) cannot handle such small
         * constants: MATH-721
         */
        SAFE_MIN = Double.longBitsToDouble((EXPONENT_OFFSET - 1022l) << 52);
    }

    /** Relative threshold for small singular values. */
    private static final double EPS = 0x1.0p-52;
    /** Absolute threshold for small singular values. */
    private static final double TINY = 0x1.0p-966;
    /** Computed singular values. */
    private final double[] singularValues;
    /** Cached value of U matrix. */
    private final Matrix4f cachedU;
    /** Cached value of transposed U matrix. */
    private Matrix4f cachedUt;
    /** Cached value of S (diagonal) matrix. */
    private Matrix4f cachedS;
    /** Cached value of V matrix. */
    private final Matrix4f cachedV;
    /** Cached value of transposed V matrix. */
    private Matrix4f cachedVt;
    /**
     * Tolerance value for small singular values, calculated once we have populated "singularValues".
     **/
    private final double tol;

    /**
     * Calculates the compact Singular Value Decomposition of the given matrix.
     *
     * @param matrix
     *                   Matrix to decompose.
     */
    public SingularValueDecomposition(final Matrix4f matrix) {
        final Matrix4f mat = matrix;
        final double[][] A = { //
                { mat.m00, mat.m01, mat.m02, mat.m03 }, //
                { mat.m10, mat.m11, mat.m12, mat.m13 }, //
                { mat.m20, mat.m21, mat.m22, mat.m23 }, //
                { mat.m30, mat.m31, mat.m32, mat.m33 } //
        };
        final int m = 4;
        final int n = 4;

        singularValues = new double[n];
        final double[][] U = new double[m][n];
        final double[][] V = new double[n][n];
        final double[] e = new double[n];
        final double[] work = new double[m];
        // Reduce A to bidiagonal form, storing the diagonal elements
        // in s and the super-diagonal elements in e.
        final int nct = Math.min(m - 1, n);
        final int nrt = Math.max(0, n - 2);
        for (int k = 0; k < Math.max(nct, nrt); k++) {
            if (k < nct) {
                // Compute the transformation for the k-th column and
                // place the k-th diagonal in s[k].
                // Compute 2-norm of k-th column without under/overflow.
                singularValues[k] = 0;
                for (int i = k; i < m; i++) {
                    singularValues[k] = Math.hypot(singularValues[k], A[i][k]);
                }
                if (singularValues[k] != 0) {
                    if (A[k][k] < 0) {
                        singularValues[k] = -singularValues[k];
                    }
                    for (int i = k; i < m; i++) {
                        A[i][k] /= singularValues[k];
                    }
                    A[k][k] += 1;
                }
                singularValues[k] = -singularValues[k];
            }
            for (int j = k + 1; j < n; j++) {
                if (k < nct && singularValues[k] != 0) {
                    // Apply the transformation.
                    double t = 0;
                    for (int i = k; i < m; i++) {
                        t += A[i][k] * A[i][j];
                    }
                    t = -t / A[k][k];
                    for (int i = k; i < m; i++) {
                        A[i][j] += t * A[i][k];
                    }
                }
                // Place the k-th row of A into e for the
                // subsequent calculation of the row transformation.
                e[j] = A[k][j];
            }
            if (k < nct) {
                // Place the transformation in U for subsequent back
                // multiplication.
                for (int i = k; i < m; i++) {
                    U[i][k] = A[i][k];
                }
            }
            if (k < nrt) {
                // Compute the k-th row transformation and place the
                // k-th super-diagonal in e[k].
                // Compute 2-norm without under/overflow.
                e[k] = 0;
                for (int i = k + 1; i < n; i++) {
                    e[k] = Math.hypot(e[k], e[i]);
                }
                if (e[k] != 0) {
                    if (e[k + 1] < 0) {
                        e[k] = -e[k];
                    }
                    for (int i = k + 1; i < n; i++) {
                        e[i] /= e[k];
                    }
                    e[k + 1] += 1;
                }
                e[k] = -e[k];
                if (k + 1 < m && e[k] != 0) {
                    // Apply the transformation.
                    for (int i = k + 1; i < m; i++) {
                        work[i] = 0;
                    }
                    for (int j = k + 1; j < n; j++) {
                        for (int i = k + 1; i < m; i++) {
                            work[i] += e[j] * A[i][j];
                        }
                    }
                    for (int j = k + 1; j < n; j++) {
                        final double t = -e[j] / e[k + 1];
                        for (int i = k + 1; i < m; i++) {
                            A[i][j] += t * work[i];
                        }
                    }
                }

                // Place the transformation in V for subsequent
                // back multiplication.
                for (int i = k + 1; i < n; i++) {
                    V[i][k] = e[i];
                }
            }
        }
        // Set up the final bidiagonal matrix or order p.
        int p = n;
        if (nct < n) {
            singularValues[nct] = A[nct][nct];
        }
        if (m < p) {
            singularValues[p - 1] = 0;
        }
        if (nrt + 1 < p) {
            e[nrt] = A[nrt][p - 1];
        }
        e[p - 1] = 0;

        // Generate U.
        for (int j = nct; j < n; j++) {
            for (int i = 0; i < m; i++) {
                U[i][j] = 0;
            }
            U[j][j] = 1;
        }
        for (int k = nct - 1; k >= 0; k--) {
            if (singularValues[k] != 0) {
                for (int j = k + 1; j < n; j++) {
                    double t = 0;
                    for (int i = k; i < m; i++) {
                        t += U[i][k] * U[i][j];
                    }
                    t = -t / U[k][k];
                    for (int i = k; i < m; i++) {
                        U[i][j] += t * U[i][k];
                    }
                }
                for (int i = k; i < m; i++) {
                    U[i][k] = -U[i][k];
                }
                U[k][k] = 1 + U[k][k];
                for (int i = 0; i < k - 1; i++) {
                    U[i][k] = 0;
                }
            } else {
                for (int i = 0; i < m; i++) {
                    U[i][k] = 0;
                }
                U[k][k] = 1;
            }
        }

        // Generate V.
        for (int k = n - 1; k >= 0; k--) {
            if (k < nrt && e[k] != 0) {
                for (int j = k + 1; j < n; j++) {
                    double t = 0;
                    for (int i = k + 1; i < n; i++) {
                        t += V[i][k] * V[i][j];
                    }
                    t = -t / V[k + 1][k];
                    for (int i = k + 1; i < n; i++) {
                        V[i][j] += t * V[i][k];
                    }
                }
            }
            for (int i = 0; i < n; i++) {
                V[i][k] = 0;
            }
            V[k][k] = 1;
        }

        // Main iteration loop for the singular values.
        final int pp = p - 1;
        while (p > 0) {
            int k;
            int kase;
            // Here is where a test for too many iterations would go.
            // This section of the program inspects for
            // negligible elements in the s and e arrays.  On
            // completion the variables kase and k are set as follows.
            // kase = 1     if s(p) and e[k-1] are negligible and k<p
            // kase = 2     if s(k) is negligible and k<p
            // kase = 3     if e[k-1] is negligible, k<p, and
            //              s(k), ..., s(p) are not negligible (qr step).
            // kase = 4     if e(p-1) is negligible (convergence).
            for (k = p - 2; k >= 0; k--) {
                final double threshold = TINY + EPS * (Math.abs(singularValues[k]) + Math.abs(singularValues[k + 1]));

                // the following condition is written this way in order
                // to break out of the loop when NaN occurs, writing it
                // as "if (FastMath.abs(e[k]) <= threshold)" would loop
                // indefinitely in case of NaNs because comparison on NaNs
                // always return false, regardless of what is checked
                // see issue MATH-947
                if (!(Math.abs(e[k]) > threshold)) {
                    e[k] = 0;
                    break;
                }

            }

            if (k == p - 2) {
                kase = 4;
            } else {
                int ks;
                for (ks = p - 1; ks >= k; ks--) {
                    if (ks == k) {
                        break;
                    }
                    final double t = (ks != p ? Math.abs(e[ks]) : 0) + (ks != k + 1 ? Math.abs(e[ks - 1]) : 0);
                    if (Math.abs(singularValues[ks]) <= TINY + EPS * t) {
                        singularValues[ks] = 0;
                        break;
                    }
                }
                if (ks == k) {
                    kase = 3;
                } else if (ks == p - 1) {
                    kase = 1;
                } else {
                    kase = 2;
                    k = ks;
                }
            }
            k++;
            // Perform the task indicated by kase.
            switch (kase) {
            // Deflate negligible s(p).
            case 1: {
                double f = e[p - 2];
                e[p - 2] = 0;
                for (int j = p - 2; j >= k; j--) {
                    double t = Math.hypot(singularValues[j], f);
                    final double cs = singularValues[j] / t;
                    final double sn = f / t;
                    singularValues[j] = t;
                    if (j != k) {
                        f = -sn * e[j - 1];
                        e[j - 1] = cs * e[j - 1];
                    }

                    for (int i = 0; i < n; i++) {
                        t = cs * V[i][j] + sn * V[i][p - 1];
                        V[i][p - 1] = -sn * V[i][j] + cs * V[i][p - 1];
                        V[i][j] = t;
                    }
                }
            }
                break;
            // Split at negligible s(k).
            case 2: {
                double f = e[k - 1];
                e[k - 1] = 0;
                for (int j = k; j < p; j++) {
                    double t = Math.hypot(singularValues[j], f);
                    final double cs = singularValues[j] / t;
                    final double sn = f / t;
                    singularValues[j] = t;
                    f = -sn * e[j];
                    e[j] = cs * e[j];

                    for (int i = 0; i < m; i++) {
                        t = cs * U[i][j] + sn * U[i][k - 1];
                        U[i][k - 1] = -sn * U[i][j] + cs * U[i][k - 1];
                        U[i][j] = t;
                    }
                }
            }
                break;
            // Perform one qr step.
            case 3: {
                // Calculate the shift.
                final double maxPm1Pm2 = Math.max(Math.abs(singularValues[p - 1]), Math.abs(singularValues[p - 2]));
                final double scale = Math.max(
                        Math.max(Math.max(maxPm1Pm2, Math.abs(e[p - 2])), Math.abs(singularValues[k])),
                        Math.abs(e[k]));
                final double sp = singularValues[p - 1] / scale;
                final double spm1 = singularValues[p - 2] / scale;
                final double epm1 = e[p - 2] / scale;
                final double sk = singularValues[k] / scale;
                final double ek = e[k] / scale;
                final double b = ((spm1 + sp) * (spm1 - sp) + epm1 * epm1) / 2.0;
                final double c = (sp * epm1) * (sp * epm1);
                double shift = 0;
                if (b != 0 || c != 0) {
                    shift = Math.sqrt(b * b + c);
                    if (b < 0) {
                        shift = -shift;
                    }
                    shift = c / (b + shift);
                }
                double f = (sk + sp) * (sk - sp) + shift;
                double g = sk * ek;
                // Chase zeros.
                for (int j = k; j < p - 1; j++) {
                    double t = Math.hypot(f, g);
                    double cs = f / t;
                    double sn = g / t;
                    if (j != k) {
                        e[j - 1] = t;
                    }
                    f = cs * singularValues[j] + sn * e[j];
                    e[j] = cs * e[j] - sn * singularValues[j];
                    g = sn * singularValues[j + 1];
                    singularValues[j + 1] = cs * singularValues[j + 1];

                    for (int i = 0; i < n; i++) {
                        t = cs * V[i][j] + sn * V[i][j + 1];
                        V[i][j + 1] = -sn * V[i][j] + cs * V[i][j + 1];
                        V[i][j] = t;
                    }
                    t = Math.hypot(f, g);
                    cs = f / t;
                    sn = g / t;
                    singularValues[j] = t;
                    f = cs * e[j] + sn * singularValues[j + 1];
                    singularValues[j + 1] = -sn * e[j] + cs * singularValues[j + 1];
                    g = sn * e[j + 1];
                    e[j + 1] = cs * e[j + 1];
                    if (j < m - 1) {
                        for (int i = 0; i < m; i++) {
                            t = cs * U[i][j] + sn * U[i][j + 1];
                            U[i][j + 1] = -sn * U[i][j] + cs * U[i][j + 1];
                            U[i][j] = t;
                        }
                    }
                }
                e[p - 2] = f;
            }
                break;
            // Convergence.
            default: {
                // Make the singular values positive.
                if (singularValues[k] <= 0) {
                    singularValues[k] = singularValues[k] < 0 ? -singularValues[k] : 0;

                    for (int i = 0; i <= pp; i++) {
                        V[i][k] = -V[i][k];
                    }
                }
                // Order the singular values.
                while (k < pp) {
                    if (singularValues[k] >= singularValues[k + 1]) {
                        break;
                    }
                    double t = singularValues[k];
                    singularValues[k] = singularValues[k + 1];
                    singularValues[k + 1] = t;
                    if (k < n - 1) {
                        for (int i = 0; i < n; i++) {
                            t = V[i][k + 1];
                            V[i][k + 1] = V[i][k];
                            V[i][k] = t;
                        }
                    }
                    if (k < m - 1) {
                        for (int i = 0; i < m; i++) {
                            t = U[i][k + 1];
                            U[i][k + 1] = U[i][k];
                            U[i][k] = t;
                        }
                    }
                    k++;
                }
                p--;
            }
                break;
            }
        }

        // Set the small value tolerance used to calculate rank and pseudo-inverse
        tol = Math.max(m * singularValues[0] * EPS, Math.sqrt(SAFE_MIN));

        cachedU = new Matrix4f(
                (float) U[0][0],
                (float) U[0][1],
                (float) U[0][2],
                (float) U[0][3], //
                (float) U[1][0],
                (float) U[1][1],
                (float) U[1][2],
                (float) U[1][3], //
                (float) U[2][0],
                (float) U[2][1],
                (float) U[2][2],
                (float) U[2][3], //
                (float) U[3][0],
                (float) U[3][1],
                (float) U[3][2],
                (float) U[3][3] //
        );
        cachedV = new Matrix4f(
                (float) V[0][0],
                (float) V[0][1],
                (float) V[0][2],
                (float) V[0][3], //
                (float) V[1][0],
                (float) V[1][1],
                (float) V[1][2],
                (float) V[1][3], //
                (float) V[2][0],
                (float) V[2][1],
                (float) V[2][2],
                (float) V[2][3], //
                (float) V[3][0],
                (float) V[3][1],
                (float) V[3][2],
                (float) V[3][3] //
        );
    }

    /**
     * Returns the matrix U of the decomposition.
     * <p>
     * U is an orthogonal matrix, i.e. its transpose is also its inverse.
     * </p>
     * 
     * @return the U matrix
     * @see #getUT()
     */
    public Matrix4f getU() {
        // return the cached matrix
        return cachedU;

    }

    /**
     * Returns the transpose of the matrix U of the decomposition.
     * <p>
     * U is an orthogonal matrix, i.e. its transpose is also its inverse.
     * </p>
     * 
     * @return the U matrix (or null if decomposed matrix is singular)
     * @see #getU()
     */
    public Matrix4f getUT() {
        if (cachedUt == null) {
            cachedUt = new Matrix4f();
            cachedUt.transpose(getU());
        }
        // return the cached matrix
        return cachedUt;
    }

    /**
     * Returns the diagonal matrix &Sigma; of the decomposition.
     * <p>
     * &Sigma; is a diagonal matrix. The singular values are provided in non-increasing order, for compatibility with
     * Jama.
     * </p>
     * 
     * @return the &Sigma; matrix
     */
    public Matrix4f getS() {
        if (cachedS == null) {
            // cache the matrix for subsequent calls
            cachedS = new Matrix4f();
            cachedS.m00 = (float) singularValues[0];
            cachedS.m11 = (float) singularValues[1];
            cachedS.m22 = (float) singularValues[2];
            cachedS.m33 = (float) singularValues[3];
        }
        return cachedS;
    }

    /**
     * Returns the diagonal elements of the matrix &Sigma; of the decomposition.
     * <p>
     * The singular values are provided in non-increasing order, for compatibility with Jama.
     * </p>
     * 
     * @return the diagonal elements of the &Sigma; matrix
     */
    public double[] getSingularValues() {
        return singularValues.clone();
    }

    /**
     * Returns the matrix V of the decomposition.
     * <p>
     * V is an orthogonal matrix, i.e. its transpose is also its inverse.
     * </p>
     * 
     * @return the V matrix (or null if decomposed matrix is singular)
     * @see #getVT()
     */
    public Matrix4f getV() {
        // return the cached matrix
        return cachedV;
    }

    /**
     * Returns the transpose of the matrix V of the decomposition.
     * <p>
     * V is an orthogonal matrix, i.e. its transpose is also its inverse.
     * </p>
     * 
     * @return the V matrix (or null if decomposed matrix is singular)
     * @see #getV()
     */
    public Matrix4f getVT() {
        if (cachedVt == null) {
            cachedVt = new Matrix4f();
            cachedVt.transpose(getV());
        }
        // return the cached matrix
        return cachedVt;
    }

    /**
     * Return the effective numerical matrix rank.
     * <p>
     * The effective numerical rank is the number of non-negligible singular values. The threshold used to identify
     * non-negligible terms is max(m,n) &times; ulp(s<sub>1</sub>) where ulp(s<sub>1</sub>) is the least significant bit
     * of the largest singular value.
     * </p>
     * 
     * @return effective numerical matrix rank
     */
    public int getRank() {
        int r = 0;
        for (int i = 0; i < singularValues.length; i++) {
            if (singularValues[i] > tol) {
                r++;
            }
        }
        return r;
    }
}
