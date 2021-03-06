package org.anchor.engine.shared.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.anchor.engine.common.utils.FileHelper;
import org.anchor.engine.shared.physics.CollisionMesh;
import org.lwjgl.util.vector.Vector3f;

public class CollisionMeshLoader {

    private static final String RES_LOC = "res";

    public static List<CollisionMesh> loadCollisionMeshes(String mesh) {
        return loadCollisionMeshes(FileHelper.newGameFile(RES_LOC, mesh + ".pcl"));
    }

    public static List<CollisionMesh> loadCollisionMeshes(File meshFile) {
        String[] lines = FileHelper.read(meshFile).split("\n");

        List<Vector3f> vertices = new ArrayList<Vector3f>();
        List<Vector3f> normals = new ArrayList<Vector3f>();
        List<Integer> indices = new ArrayList<Integer>();
        List<CollisionMesh> meshes = new ArrayList<CollisionMesh>();

        for (String line : lines) {
            line = line.trim().replaceAll(" +", " ");
            if (line.startsWith("v ")) {
                String[] currentLine = line.split(" ");
                Vector3f vertex = new Vector3f((float) Float.valueOf(currentLine[1]), (float) Float.valueOf(currentLine[2]), (float) Float.valueOf(currentLine[3]));

                vertices.add(vertex);
            } else if (line.startsWith("vn ")) {
                String[] currentLine = line.split(" ");
                Vector3f normal = new Vector3f((float) Float.valueOf(currentLine[1]), (float) Float.valueOf(currentLine[2]), (float) Float.valueOf(currentLine[3]));

                normals.add(normal);
            } else if (line.startsWith("o")) {
                if (indices.size() == 0)
                    continue;

                List<Integer> meshIndices = new ArrayList<Integer>();
                List<Vector3f> meshVertices = new ArrayList<Vector3f>();

                int i = 0;
                for (int index : indices) {
                    i++;

                    meshVertices.add(vertices.get(index - 1));
                    meshIndices.add(i);
                }

                meshes.add(new CollisionMesh(meshVertices, normals, meshIndices));
                indices.clear();
            } else if (line.startsWith("f")) {
                String[] currentLine = line.split(" ");
                String[] vertex1 = currentLine[1].split("/");
                String[] vertex2 = currentLine[2].split("/");
                String[] vertex3 = currentLine[3].split("/");

                indices.add(Integer.parseInt(vertex1[0]));
                indices.add(Integer.parseInt(vertex2[0]));
                indices.add(Integer.parseInt(vertex3[0]));
            }
        }

        if (vertices.size() > 0)
            meshes.add(new CollisionMesh(vertices, normals, indices));

        return meshes;
    }

}
