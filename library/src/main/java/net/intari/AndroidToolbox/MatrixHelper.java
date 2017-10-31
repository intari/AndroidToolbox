/*
 * Copyright (c) 2017 Dmitriy Kazimirov, e-mail:dmitriy.kazimirov@viorsan.com
 *
 */

package net.intari.AndroidToolbox;

/**
 * Created by Dmitriy Kazimirov, e-mail:dmitriy.kazimirov@viorsan.com on 11.08.17.
 */

public class MatrixHelper {
    public static final String TAG = MatrixHelper.class.getName();

    public static void perspectiveM(float[] m, float yFovInDegrees, float aspect, float n, float f) {
        //calculate focal length
        final float angleInRadians = (float) (yFovInDegrees * Math.PI / 180.0);
        final float a = (float) (1.0 / Math.tan(angleInRadians / 2.0));
        //write matrix values
        //OpenGL store matrixes in column-major order.
        m[0] = a / aspect;
        m[1] = 0f;
        m[2] = 0f;
        m[3] = 0f;

        m[4] = 0f;
        m[5] = a;
        m[6] = 0f;
        m[7] = 0f;
        m[8] = 0f;
        m[9] = 0f;
        m[10] = - ((f + n)/(f - n));
        m[11] = -1f;
        m[12] = 0f;
        m[13] = 0f;
        m[14] = -((2f * f * n) / (f - n));
        m[15] = 0f;

    }
}
