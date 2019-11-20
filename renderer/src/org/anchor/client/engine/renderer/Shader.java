package org.anchor.client.engine.renderer;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.anchor.engine.common.Log;
import org.anchor.engine.common.utils.FileHelper;
import org.anchor.engine.common.vfs.VirtualFileSystem;
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
    private static String DEFINES = "\n#define tex uniform sampler2D\n#define texCube uniform samplerCube\n#define float2 vec2\n#define float3 vec3\n#define float4 vec4\n#define SHADOW_SPLITS " + Settings.shadowSplits + "\n#define MAX_LIGHTS " + Settings.maxLights + "\n#define MAX_JOINTS " + Settings.maxJoints + "\n#define MAX_PROBES " + Settings.maxProbes + "\n#define MAX_WEIGHTS " + Settings.maxWeights + "\n#define FS_OUT(x) out vec4 out_##x;\n#define GAMMA " + Settings.gamma + "\n#define TO_V4(x) vec4(vec3(x##.xyz), 1)\nconst float pi = 3.141592653589793238;\n";

    private static List<Shader> shaders = new ArrayList<Shader>();

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

        shaders.add(this);
    }

    public void reload() {
        uniforms.clear();

        loadShader("shaders/" + program + "/vertex.glsl", vertexShaderId, true);
        loadShader("shaders/" + program + "/fragment.glsl", fragmentShaderId, true);
        GL20.glLinkProgram(programId);

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
        loadFloat("tickTime", Renderer.tickTime);
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
        if (vector == null)
            GL20.glUniform2f(getUniformLocation(location), 0, 0);
        else
            GL20.glUniform2f(getUniformLocation(location), vector.x, vector.y);
    }

    protected void loadVector(String location, Vector3f vector) {
        if (vector == null)
            GL20.glUniform3f(getUniformLocation(location), 0, 0, 0);
        else
            GL20.glUniform3f(getUniformLocation(location), vector.x, vector.y, vector.z);
    }

    protected void loadVector(String location, Vector4f vector) {
        if (vector == null)
            GL20.glUniform4f(getUniformLocation(location), 0, 0, 0, 0);
        else
            GL20.glUniform4f(getUniformLocation(location), vector.x, vector.y, vector.z, vector.w);
    }

    protected void loadVector(String location, Vector3f vector, float w) {
        if (vector == null)
            GL20.glUniform4f(getUniformLocation(location), 0, 0, 0, w);
        else
            GL20.glUniform4f(getUniformLocation(location), vector.x, vector.y, vector.z, w);
    }

    protected void loadAs3DVector(String location, Vector4f vector) {
        if (vector == null)
            GL20.glUniform3f(getUniformLocation(location), 0, 0, 0);
        else
            GL20.glUniform3f(getUniformLocation(location), vector.x, vector.y, vector.z);
    }

    protected void loadVector(String location, float x, float y) {
        GL20.glUniform2f(getUniformLocation(location), x, y);
    }

    protected void loadVector(String location, float x, float y, float z) {
        GL20.glUniform3f(getUniformLocation(location), x, y, z);
    }

    protected void loadVector(String location, float x, float y, float z, float w) {
        GL20.glUniform4f(getUniformLocation(location), x, y, z, w);
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
        return loadShader(file, type, false);
    }

    private static int loadShader(String file, int type, boolean reload) {
        StringBuilder shaderSource = new StringBuilder();
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(VirtualFileSystem.find(file).openInputStream()));
            String line;

            while ((line = reader.readLine()) != null) {
                if (line.startsWith("#include"))
                    shaderSource.append(FileHelper.read(new File("shaders", line.substring(line.indexOf('"') + 1, line.lastIndexOf('"'))))).append("\n");
                else
                    shaderSource.append(line).append("\n");
            }

            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }
        shaderSource.insert(shaderSource.indexOf("\n", shaderSource.indexOf("#version")) + 1, DEFINES);

        int shaderId = type;
        if (!reload)
            shaderId = GL20.glCreateShader(type);

        GL20.glShaderSource(shaderId, shaderSource);
        GL20.glCompileShader(shaderId);

        if (GL20.glGetShaderi(shaderId, GL20.GL_COMPILE_STATUS) == GL11.GL_FALSE) {
            String error = GL20.glGetShaderInfoLog(shaderId, 500);
            String[] lines = shaderSource.toString().split("\n");
            System.out.println(error);

            int location;
            try {
                location = Integer.parseInt(error.substring(error.indexOf('(') + 1, error.indexOf(')')));
            } catch (StringIndexOutOfBoundsException e) {
                String[] errorDots = error.split(":");
                location = Integer.parseInt(errorDots[2]);
            }

            String before = "", middle = "", after = lines[location];
            if (location > 1)
                before = lines[location - 2];
            if (location > 0)
                middle = lines[location - 1];
            Log.error(error + "\nPossible offending lines:\n\t" + before + "\n\t" + middle + "\n\t" + after + "\nCould not compile shader " + file + "!");

            System.exit(-1);
        }

        return shaderId;
    }

    public static List<Shader> getShaders() {
        return shaders;
    }

}
