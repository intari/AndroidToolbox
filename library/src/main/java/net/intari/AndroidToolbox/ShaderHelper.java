/*
 * Copyright (c) 2017 Dmitriy Kazimirov, e-mail:dmitriy.kazimirov@viorsan.com
 *
 */

package net.intari.AndroidToolbox;

import net.intari.CustomLogger.CustomLog;

import static android.opengl.GLES20.GL_COMPILE_STATUS;
import static android.opengl.GLES20.GL_FRAGMENT_SHADER;
import static android.opengl.GLES20.GL_LINK_STATUS;
import static android.opengl.GLES20.GL_VALIDATE_STATUS;
import static android.opengl.GLES20.GL_VERTEX_SHADER;
import static android.opengl.GLES20.glAttachShader;
import static android.opengl.GLES20.glCompileShader;
import static android.opengl.GLES20.glCreateProgram;
import static android.opengl.GLES20.glCreateShader;
import static android.opengl.GLES20.glDeleteProgram;
import static android.opengl.GLES20.glDeleteShader;
import static android.opengl.GLES20.glGetProgramInfoLog;
import static android.opengl.GLES20.glGetProgramiv;
import static android.opengl.GLES20.glGetShaderInfoLog;
import static android.opengl.GLES20.glGetShaderiv;
import static android.opengl.GLES20.glLinkProgram;
import static android.opengl.GLES20.glShaderSource;
import static android.opengl.GLES20.glValidateProgram;

/**
 * Created by Dmitriy Kazimirov, e-mail:dmitriy.kazimirov@viorsan.com on 02.08.17.
 */

public class ShaderHelper {
    public static final String TAG = ShaderHelper.class.getName();

    public static int compileVertexShader(String shaderCode) {
        return compileShader(GL_VERTEX_SHADER,shaderCode);
    }
    public static int compileFragment_Shader(String shaderCode) {
        return compileShader(GL_FRAGMENT_SHADER,shaderCode);
    }
    private static int compileShader(int type, String shaderCode) {
        final int shaderObjectId = glCreateShader(type);
        if (shaderObjectId == 0) {
            CustomLog.w(TAG,"Could not create new shader");
            return 0;
        }

        //compile shader
        glShaderSource(shaderObjectId,shaderCode);
        glCompileShader(shaderObjectId);

        //get compilation status
        final int[] compileStatus = new int[1];
        glGetShaderiv(shaderObjectId,GL_COMPILE_STATUS,compileStatus,0);

        //get compilation log
        CustomLog.v(TAG,"Results of compiling:\n"+shaderCode+"\n:"+glGetShaderInfoLog(shaderObjectId));

        //check results
        if (compileStatus[0] == 0){
            //failed. delete shader object
            glDeleteShader(shaderObjectId);
            CustomLog.w(TAG,"Failed to compile shader");
            return 0;
        }

        //all ok, return our shader object id
        return shaderObjectId;

    }

    public static int linkProgram(int vertexShaderId, int fragmentShaderId) {
        final int programObjectId = glCreateProgram();
        if (programObjectId == 0) {
            CustomLog.w(TAG,"Could not create new program");
            return 0;
        }
        glAttachShader(programObjectId,vertexShaderId);
        glAttachShader(programObjectId,fragmentShaderId);
        glLinkProgram(programObjectId);

        //check status
        final int[] linkStatus = new int[1];
        glGetProgramiv(programObjectId,GL_LINK_STATUS,linkStatus,0);

        if (linkStatus[0] == 0) {
            //failed. delete program object
            glDeleteProgram(programObjectId);
            //prepare to throw exception
            String programLog=glGetProgramInfoLog(programObjectId);
            CustomLog.w(TAG,"Linking of program failed:\n"+programLog);
            return 0;
        }
        //sometimes crash on emulator on non-errors
        return programObjectId;
    }

    //validate if program valid for current OpenGL state, get suggestions,etc
    public static boolean validateProgram(int programObjectId) {
        glValidateProgram(programObjectId);

        final int[] validateStatus = new int[1];
        glGetProgramiv(programObjectId, GL_VALIDATE_STATUS, validateStatus,0);
        CustomLog.v(TAG,"Results of validating program:"+validateStatus+"\nLog:"+glGetProgramInfoLog(programObjectId));
        return validateStatus[0] != 0;

    }

    public static int buildProgram(String vertexShaderSource, String fragmentShaderSource) {
        int program;

        //compile shaders
        int vertexShader = compileVertexShader(vertexShaderSource);
        int fragmentShader = compileFragment_Shader(fragmentShaderSource);

        //link them into a shader program
        program = linkProgram(vertexShader, fragmentShader);

        validateProgram(program);

        return program;
    }
}
