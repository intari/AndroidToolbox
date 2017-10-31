/*
 * Copyright (c) 2017 Dmitriy Kazimirov, e-mail:dmitriy.kazimirov@viorsan.com
 *
 */

package net.intari.AndroidToolbox;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLUtils;

import net.intari.CustomLogger.CustomLog;


/**
 * Created by Dmitriy Kazimirov, e-mail:dmitriy.kazimirov@viorsan.com on 12.08.17.
 */

public class TextureHelper {
    public static final String TAG = TextureHelper.class.getName();

    public static int loadTexture(Context context, int resourceId) {

        //generate texture object
        final int[] textureObjectIds = new int[1];

        GLES20.glGenTextures(1, textureObjectIds, 0);
        if (textureObjectIds[0] == 0) {
            CustomLog.w(TAG,"Could not generate a new OpenGL texture object");
            return 0;
        }

        //load bitmap data (and bind to texture)
        //large images can have...issues... so see my code library for alternative means
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inScaled = false;

        final Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), resourceId, options);

        if (bitmap == null) {
            CustomLog.w(TAG," Resource ID "+resourceId+" could not be decoded");
            GLES20.glDeleteTextures(1,textureObjectIds,0);
            return 0;
        }
        //loadeded image
        //bind so next operations will apply to this texture object
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,textureObjectIds[0]);

        //configure texture filtering
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR_MIPMAP_LINEAR);//riliniear filtering
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);//bilinear filtering

        //load image data to texture
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D,0,bitmap,0);

        //drop android bitmap
        bitmap.recycle();

        //generate mipmaps
        GLES20.glGenerateMipmap(GLES20.GL_TEXTURE_2D);

        //unbind from current texture (so we don't make futher changes)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,0);

        //return texture object
        return textureObjectIds[0];

    }

    /**
     * Loads a cubemap texture from the provided resources and returns the
     * texture ID. Returns 0 if the load failed.
     *
     * @param context
     * @param cubeResources
     *            An array of resources corresponding to the cube map. Should be
     *            provided in this order: left, right, bottom, top, front, back.
     * @return
     */
    public static int loadCubeMap(Context context, int[] cubeResources) {
        final int[] textureObjectIds = new int[1];
        GLES20.glGenTextures(1, textureObjectIds, 0);

        if (textureObjectIds[0] == 0) {
            CustomLog.w(TAG, "Could not generate a new OpenGL texture object.");
            return 0;
        }

        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inScaled = false;
        final Bitmap[] cubeBitmaps = new Bitmap[6];

        for (int i = 0; i < 6; i++) {
            cubeBitmaps[i] =
                    BitmapFactory.decodeResource(context.getResources(),
                            cubeResources[i], options);

            if (cubeBitmaps[i] == null) {
                CustomLog.w(TAG, "Resource ID " + cubeResources[i]
                        + " could not be decoded.");

                GLES20.glDeleteTextures(1, textureObjectIds, 0);

                return 0;
            }
        }

        // Linear filtering for minification and magnification
        GLES20.glBindTexture(GLES20.GL_TEXTURE_CUBE_MAP, textureObjectIds[0]);

        GLES20.glTexParameteri(GLES20.GL_TEXTURE_CUBE_MAP,
                GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_CUBE_MAP,
                GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);

        GLUtils.texImage2D(GLES20.GL_TEXTURE_CUBE_MAP_NEGATIVE_X, 0, cubeBitmaps[0], 0);
        GLUtils.texImage2D(GLES20.GL_TEXTURE_CUBE_MAP_POSITIVE_X, 0, cubeBitmaps[1], 0);

        GLUtils.texImage2D(GLES20.GL_TEXTURE_CUBE_MAP_NEGATIVE_Y, 0, cubeBitmaps[2], 0);
        GLUtils.texImage2D(GLES20.GL_TEXTURE_CUBE_MAP_POSITIVE_Y, 0, cubeBitmaps[3], 0);

        GLUtils.texImage2D(GLES20.GL_TEXTURE_CUBE_MAP_NEGATIVE_Z, 0, cubeBitmaps[4], 0);
        GLUtils.texImage2D(GLES20.GL_TEXTURE_CUBE_MAP_POSITIVE_Z, 0, cubeBitmaps[5], 0);

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);

        for (Bitmap bitmap : cubeBitmaps) {
            bitmap.recycle();
        }

        return textureObjectIds[0];
    }
}
