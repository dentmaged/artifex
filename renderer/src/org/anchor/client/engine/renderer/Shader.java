package org.anchor.client.engine.renderer;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.util.HashMap;
import java.util.Map;

import org.anchor.client.engine.renderer.deferred.DeferredShader;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

public abstract class Shader {

    protected String program;
    private int programId;
    private int vertexShaderId, fragmentShaderId;
    protected Map<String, Integer> uniforms = new HashMap<String, Integer>();

    private static FloatBuffer matrixBuffer = BufferUtils.createFloatBuffer(16);
    private static String DEFINES = "\n#define tex uniform sampler2D\n#define float2 vec2\n#define float3 vec3\n#define float4 vec4\n#define MAX_LIGHTS " + DeferredShader.MAX_LIGHTS + "\n#define FS_OUT(x) out vec4 out_##x;\n#define GAMMA 2.2\n";

    public Shader(String program) {
        this.program = program;
        vertexShaderId = loadShader("shaders/" + program + "/vertex.glsl", GL20.GL_VERTEX_SHADER);
        fragmentShaderId = loadShader("shaders/" + program + "/fragment.glsl", GL20.GL_FRAGMENT_SHADER);
        programId = GL20.glCreateProgram();

        GL20.glAttachShader(programId, vertexShaderId);
        GL20.glAttachShader(programId, fragmentShaderId);
        bindAttributes();

        GL20.glLinkProgram(programId);
        GL20.glValidateProgram(programId);

        start();
        loadMatrix("projectionMatrix", Renderer.getProjectionMatrix());
        loadMatrix("inverseProjectionMatrix", Matrix4f.invert(Renderer.getProjectionMatrix(), null));
        stop();
    }

    protected int getUniformLocation(String name) {
        if (!uniforms.containsKey(name.toLowerCase()))
            uniforms.put(name.toLowerCase(), GL20.glGetUniformLocation(programId, name));

        return uniforms.get(name.toLowerCase());
    }

    public void start() {
        GL20.glUseProgram(programId);
    }

    public void stop() {
        GL20.glUseProgram(0);
    }

    public void shutdown() {
        stop();

        GL20.glDetachShader(programId, vertexShaderId);
        GL20.glDetachShader(programId, fragmentShaderId);

        GL20.glDeleteShader(vertexShaderId);
        GL20.glDeleteShader(fragmentShaderId);

        GL20.glDeleteProgram(programId);
    }

    protected abstract void bindAttributes();

    protected void bindFragOutput(int attachment, String variableName) {
        GL30.glBindFragDataLocation(programId, attachment, variableName);
    }

    protected void bindAttribute(int attribute, String variableName) {
        GL20.glBindAttribLocation(programId, attribute, variableName);
    }

    protected void loadInt(String location, int value) {
        GL20.glUniform1i(getUniformLocation(location), value);
    }

    protected void loadFloat(String location, float value) {
        GL20.glUniform1f(getUniformLocation(location), value);
    }

    protected void loadVector(String location, Vector2f vector) {
        GL20.glUniform2f(getUniformLocation(location), vector.x, vector.y);
    }

    protected void loadVector(String location, Vector3f vector) {
        GL20.glUniform3f(getUniformLocation(location), vector.x, vector.y, vector.z);
    }

    protected void loadVector(String location, Vector4f vector) {
        GL20.glUniform4f(getUniformLocation(location), vector.x, vector.y, vector.z, vector.w);
    }

    protected void loadBoolean(String location, boolean value) {
        GL20.glUniform1f(getUniformLocation(location), value ? 1 : 0);
    }

    protected void loadMatrix(String location, Matrix4f matrix) {
        matrix.store(matrixBuffer);
        matrixBuffer.flip();

        GL20.glUniformMatrix4(getUniformLocation(location), false, matrixBuffer);
    }

    private static int loadShader(String file, int type) {
        StringBuilder shaderSource = new StringBuilder();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line;

            while ((line = reader.readLine()) != null)
                shaderSource.append(line).append("\n");

            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }

        shaderSource.insert(shaderSource.indexOf("\n", shaderSource.indexOf("#version")) + 1, DEFINES);

        int shaderId = GL20.glCreateShader(type);
        GL20.glShaderSource(shaderId, shaderSource);
        GL20.glCompileShader(shaderId);

        if (GL20.glGetShaderi(shaderId, GL20.GL_COMPILE_STATUS) == GL11.GL_FALSE) {
            System.out.println(GL20.glGetShaderInfoLog(shaderId, 500));
            System.err.println("Could not compile shader " + file + "!");

            System.exit(-1);
        }

        return shaderId;
    }

}
