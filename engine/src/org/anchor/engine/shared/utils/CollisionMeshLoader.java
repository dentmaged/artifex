package org.anchor.engine.shared.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.anchor.engine.common.utils.FileHelper;
import org.anchor.engine.shared.physics.CollisionMesh;
import org.lwjgl.util.vector.Vector3f;

public class CollisionMeshLoader {

    private static final String RES_LOC = "res";

    public static List<CollisionMesh> loadCollisionMesh(String mesh) {
        return loadCollisionMesh(FileHelper.newGameFile(RES_LOC, mesh + ".pcl"));
    }

    public static List<CollisionMesh> loadCollisionMesh(File meshFile) {
        String[] lines = FileHelper.read(meshFile).split("\n");

        List<Vector3f> vertices = new ArrayList<Vector3f>();
        List<Vector3f> normals = new ArrayList<Vector3f>();
        List<Integer> indices = new ArrayList<Integer>();
        List<CollisionMesh> meshes = new ArrayList<CollisionMesh>();

        int vo = 0;
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

                List<Vector3f> meshVertices = new ArrayList<Vector3f>();
                List<Vector3f> original = new ArrayList<Vector3f>(vertices);

                for (int index : indices) {
                    Vector3f vertex = original.get(index - 1);
                    if (!meshVertices.contains(vertex)) {
                        meshVertices.add(vertex);
                        vertices.remove(vertex);
                    }
                }

                vo += vertices.size();

                meshes.add(new CollisionMesh(meshVertices, normals, indices));
                indices.clear();
            } else if (line.startsWith("f")) {
                String[] currentLine = line.split(" ");
                String[] vertex1 = currentLine[1].split("/");
                String[] vertex2 = currentLine[2].split("/");
                String[] vertex3 = currentLine[3].split("/");

                indices.add(Integer.parseInt(vertex1[0]) - vo);
                indices.add(Integer.parseInt(vertex2[0]) - vo);
                indices.add(Integer.parseInt(vertex3[0]) - vo);
            }
        }

        if (vertices.size() > 0)
            meshes.add(new CollisionMesh(vertices, normals, indices));

        return meshes;
    }

}
