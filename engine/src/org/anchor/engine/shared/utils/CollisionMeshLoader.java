package org.anchor.engine.shared.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.anchor.engine.common.utils.FileHelper;
import org.anchor.engine.shared.physics.CollisionMesh;
import org.lwjgl.util.vector.Vector3f;

public class CollisionMeshLoader {

    private static final String RES_LOC = "res";

    public static CollisionMesh loadCollisionMesh(String mesh) {
        return loadCollisionMesh(FileHelper.newGameFile(RES_LOC, mesh + ".pcl"));
    }

    public static CollisionMesh loadCollisionMesh(File meshFile) {
        FileReader isr = null;
        try {
            isr = new FileReader(meshFile);
        } catch (FileNotFoundException e) {
            System.err.println("File not found in res; don't use any extention");
        }

        BufferedReader reader = new BufferedReader(isr);
        String line;

        List<Vector3f> vertices = new ArrayList<Vector3f>();
        List<Vector3f> normals = new ArrayList<Vector3f>();
        List<Integer> indices = new ArrayList<Integer>();

        try {
            while (true) {
                line = reader.readLine().trim().replaceAll(" +", " ");
                if (line.startsWith("v ")) {
                    String[] currentLine = line.split(" ");
                    Vector3f vertex = new Vector3f((float) Float.valueOf(currentLine[1]), (float) Float.valueOf(currentLine[2]), (float) Float.valueOf(currentLine[3]));

                    vertices.add(vertex);
                } else if (line.startsWith("vn ")) {
                    String[] currentLine = line.split(" ");
                    Vector3f normal = new Vector3f((float) Float.valueOf(currentLine[1]), (float) Float.valueOf(currentLine[2]), (float) Float.valueOf(currentLine[3]));

                    normals.add(normal);
                } else if (line.startsWith("f ")) {
                    break;
                }
            }

            while (line != null) {
                if (line.startsWith("f ")) {
                    String[] currentLine = line.split(" ");
                    String[] vertex1 = currentLine[1].split("/");
                    String[] vertex2 = currentLine[2].split("/");
                    String[] vertex3 = currentLine[3].split("/");

                    indices.add(Integer.parseInt(vertex1[0]));
                    indices.add(Integer.parseInt(vertex2[0]));
                    indices.add(Integer.parseInt(vertex3[0]));
                }

                line = reader.readLine();
            }

            reader.close();
        } catch (IOException e) {
            System.err.println("Error reading the file");
        }

        float[] verticesArray = new float[vertices.size() * 3];
        float[] normalsArray = new float[normals.size() * 3];
        int[] indicesArray = new int[indices.size()];
        convertDataToArrays(vertices, normals, indices, verticesArray, normalsArray, indicesArray);

        return new CollisionMesh(verticesArray, normalsArray, indicesArray);
    }

    private static void convertDataToArrays(List<Vector3f> vertices, List<Vector3f> normals, List<Integer> indices, float[] verticesArray, float[] normalsArray, int[] indicesArray) {
        for (int i = 0; i < vertices.size(); i++) {
            Vector3f vertex = vertices.get(i);

            verticesArray[i * 3] = vertex.x;
            verticesArray[i * 3 + 1] = vertex.y;
            verticesArray[i * 3 + 2] = vertex.z;
        }

        for (int i = 0; i < normals.size(); i++) {
            Vector3f normal = normals.get(i);

            normalsArray[i * 3] = normal.x;
            normalsArray[i * 3 + 1] = normal.y;
            normalsArray[i * 3 + 2] = normal.z;
        }

        for (int i = 0; i < indicesArray.length; i++)
            indicesArray[i] = indices.get(i);
    }

}
