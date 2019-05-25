package org.anchor.game.editor.editableMesh;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.anchor.client.engine.renderer.Graphics;
import org.anchor.client.engine.renderer.Loader;
import org.anchor.client.engine.renderer.types.Material;
import org.anchor.client.engine.renderer.types.Model;
import org.anchor.client.engine.renderer.types.mesh.Mesh;
import org.anchor.engine.common.utils.AABB;
import org.anchor.engine.common.utils.ArrayUtils;
import org.anchor.engine.common.utils.MD5;
import org.anchor.engine.common.utils.VectorUtils;
import org.anchor.engine.shared.components.TransformComponent;
import org.anchor.engine.shared.editor.TransformableObject;
import org.anchor.engine.shared.entity.Entity;
import org.anchor.game.client.async.Requester;
import org.anchor.game.client.components.MeshComponent;
import org.anchor.game.client.loaders.AssetLoader;
import org.anchor.game.editor.editableMesh.components.EditableMeshComponent;
import org.anchor.game.editor.editableMesh.types.Edge;
import org.anchor.game.editor.editableMesh.types.Polygon;
import org.anchor.game.editor.editableMesh.types.Vertex;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL31;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

public class EditableMesh implements TransformableObject {

    public TransformComponent transformComponent;
    public static EditableMeshComponent editableMeshComponent = new EditableMeshComponent(); // only used for UI

    public List<Vertex> vertices = new ArrayList<Vertex>(), verboseVertices = new ArrayList<Vertex>();
    public List<Edge> edges = new ArrayList<Edge>();
    public List<Polygon> polygons = new ArrayList<Polygon>();

    public Model model;
    public Material material = AssetLoader.loadMaterial("white");
    public Vector4f colour = new Vector4f();
    public String resourceName = MD5.hash(ArrayUtils.randomBytes(1024));

    public EditableMesh(Entity entity) {
        this((TransformComponent) entity.getComponent(TransformComponent.class).copy(), entity.getComponent(MeshComponent.class).model.getMesh());
    }

    public EditableMesh(EditableMesh mesh) {
        this((TransformComponent) mesh.transformComponent.copy(), mesh.model.getMesh());
    }

    public EditableMesh(TransformComponent transformComponent, Mesh mesh) {
        this.transformComponent = transformComponent;

        importMesh(mesh);
    }

    private void importMesh(Mesh mesh) {
        int vao = Loader.getInstance().createVAO();
        int[] vbos = new int[mesh.getDimensions() + 1];

        {
            GL15.glBindBuffer(GL31.GL_COPY_READ_BUFFER, mesh.getVBO(-1));
            int size = GL15.glGetBufferParameteri(GL31.GL_COPY_READ_BUFFER, GL15.GL_BUFFER_SIZE);

            int vbo = Loader.getInstance().createEmptyIndexBuffer(size / 4);
            vbos[0] = vbo;

            GL15.glBindBuffer(GL31.GL_COPY_WRITE_BUFFER, vbo);
            GL31.glCopyBufferSubData(GL31.GL_COPY_READ_BUFFER, GL31.GL_COPY_WRITE_BUFFER, 0, 0, size);
        }

        for (int i = 0; i < vbos.length - 1; i++) {
            int coordinateSize = 3;
            if (i == 1)
                coordinateSize = 2;

            GL15.glBindBuffer(GL31.GL_COPY_READ_BUFFER, mesh.getVBO(i));
            int size = GL15.glGetBufferParameteri(GL31.GL_COPY_READ_BUFFER, GL15.GL_BUFFER_SIZE);

            int vbo = Loader.getInstance().createEmptyVBO(size / 4);
            vbos[i + 1] = vbo;

            GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo);
            GL20.glVertexAttribPointer(i, coordinateSize, GL11.GL_FLOAT, false, coordinateSize * 4, 0);
            GL31.glCopyBufferSubData(GL31.GL_COPY_READ_BUFFER, GL15.GL_ARRAY_BUFFER, 0, 0, size);
        }
        GL15.glBindBuffer(GL31.GL_COPY_READ_BUFFER, 0);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
        Loader.getInstance().unbindVAO();

        Graphics.checkForErrors("%s whilst copying mesh!");

        model = new Model(Requester.alreadyLoadedMesh(new Mesh(vao, vbos, mesh.getVertexCount(), vbos.length - 1)));

        populateVertices(model.getMesh());
        createFaces(model.getMesh());
    }

    private void populateVertices(Mesh mesh) {
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, mesh.getVBO(0));
        int size = GL15.glGetBufferParameteri(GL15.GL_ARRAY_BUFFER, GL15.GL_BUFFER_SIZE);
        float[] positions = new float[size / 4];
        int vertexCount = positions.length;

        ByteBuffer byteBuffer = BufferUtils.createByteBuffer(size);
        GL15.glGetBufferSubData(GL15.GL_ARRAY_BUFFER, 0, byteBuffer);

        byteBuffer.order(ByteOrder.nativeOrder());
        FloatBuffer floatBuffer = byteBuffer.asFloatBuffer();
        floatBuffer.get(positions);

        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);

        for (int i = 0; i < vertexCount / 3; i++) {
            Vector3f position = new Vector3f(positions[i * 3], positions[i * 3 + 1], positions[i * 3 + 2]);
            if (getVerticesAtPosition(position).size() == 0)
                vertices.add(new Vertex(this, position));
            verboseVertices.add(new Vertex(this, position));
        }

        model.getMesh().setAABB(AABB.generateAABB(positions));
        Graphics.checkForErrors("%s whilst loading vertices!");
    }

    private void createFaces(Mesh mesh) {
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, mesh.getVBO(-1));
        int size = GL15.glGetBufferParameteri(GL15.GL_ELEMENT_ARRAY_BUFFER, GL15.GL_BUFFER_SIZE);

        int[] indices = new int[size / 4];
        ByteBuffer byteBuffer = BufferUtils.createByteBuffer(size);
        GL15.glGetBufferSubData(GL15.GL_ELEMENT_ARRAY_BUFFER, 0, byteBuffer);

        byteBuffer.order(ByteOrder.nativeOrder());
        IntBuffer intBuffer = byteBuffer.asIntBuffer();
        intBuffer.get(indices);

        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);
        Graphics.checkForErrors("%s whilst loading faces!");

        int polygonCount = indices.length / 3;
        for (int i = 0; i < polygonCount; i++) {
            List<Vertex> vertices = new ArrayList<Vertex>();
            vertices.add(getVerticesAtPosition(verboseVertices.get(indices[i * 3]).position).get(0));
            vertices.add(getVerticesAtPosition(verboseVertices.get(indices[i * 3 + 1]).position).get(0));
            vertices.add(getVerticesAtPosition(verboseVertices.get(indices[i * 3 + 2]).position).get(0));

            polygons.add(new Polygon(this, vertices));
        }

        verboseVertices.clear();
        mergeTriangles();
    }

    public String exportMesh() {
        StringBuilder builder = new StringBuilder("# Anchor Engine EM .OBJ exporter\n\n");
        for (Vertex vertex : vertices)
            builder.append("v ").append(vertex.position.x).append(" ").append(vertex.position.y).append(" ").append(vertex.position.z).append("\n");

        List<Vector3f> normals = new ArrayList<Vector3f>();
        outer: for (Polygon polygon : polygons) {
            Vector3f normal = polygon.getNormal();
            for (Vector3f norm : normals)
                if (normal == norm || normal.equals(norm))
                    continue outer;

            normals.add(normal);
        }

        for (Vector3f normal : normals)
            builder.append("vn ").append(normal.x).append(" ").append(normal.y).append(" ").append(normal.z).append("\n");

        builder.append("vt 0 0\n");

        for (Polygon polygon : polygons) {
            int normalIndex = ArrayUtils.getIndex(normals, polygon.getNormal()) + 1;
            builder.append("f");
            for (Vertex vertex : polygon.vertices) {
                int positionIndex = ArrayUtils.getIndex(vertices, vertex) + 1;
                builder.append(" ").append(positionIndex).append("/1/").append(normalIndex);
            }
            builder.append("\n");
        }

        return builder.toString();
    }

    // merge two triangles into a quad if they're
    // touching and if they have the same normal
    private void mergeTriangles() {
        outer: for (int i = 0; i < polygons.size(); i++) {
            Polygon polygon = polygons.get(i);
            Vector3f normal = polygon.getNormal();

            for (int j = 0; j < polygons.size(); j++) {
                Polygon other = polygons.get(j);
                if (!other.getNormal().equals(normal))
                    continue;

                if (other.vertices.size() != 3)
                    continue;

                Set<Vertex> s1 = new HashSet<Vertex>(polygon.vertices);
                Set<Vertex> s2 = new HashSet<Vertex>(other.vertices);
                s1.retainAll(s2);

                if (s1.size() == 2) { // they must be touching - two shared vertices
                    Set<Vertex> s3 = new HashSet<Vertex>(other.vertices);

                    s3.removeAll(s1); // remove two other vertices
                    polygon.vertices.addAll(s3); // add final vertex (that isn't shared)

                    polygons.remove(other);
                    continue outer;
                }
            }
        }
    }

    public void updateMesh(boolean edit) {
        Mesh mesh = model.getMesh();

        List<Float> positions = new ArrayList<Float>();
        List<Float> textureCoordinates = new ArrayList<Float>();
        List<Float> normals = new ArrayList<Float>();
        List<Integer> indices = new ArrayList<Integer>();

        for (int i = 0; i < polygons.size(); i++) {
            Polygon polygon = polygons.get(i);
            if (polygon.getNormal() == null && edit) { // zero area polygon aka points are in the same place
                polygons.remove(i);

                List<List<Vertex>> intersectingVertices = new ArrayList<List<Vertex>>();
                for (Vertex vertex : polygon.vertices) {
                    List<Vertex> intersections = new ArrayList<Vertex>();
                    intersections.add(vertex);

                    for (Vertex other : polygon.vertices) {
                        if (vertex.position.equals(other.position))
                            intersections.add(other);
                    }

                    boolean match = false;
                    for (List<Vertex> search : intersectingVertices) {
                        if (search.containsAll(intersections)) {
                            match = true;
                            break;
                        }
                    }

                    if (intersections.size() > 1 && !match)
                        intersectingVertices.add(intersections);
                }

                for (List<Vertex> intersections : intersectingVertices)
                    mergeVertices(intersections);
            }
        }

        for (Polygon polygon : polygons) {
            Vector3f normal = polygon.getNormal();
            if (normal == null)
                continue;

            int firstIndex = positions.size() / 3;
            for (Vertex vertex : polygon.vertices) {
                positions.add(vertex.position.x);
                positions.add(vertex.position.y);
                positions.add(vertex.position.z);

                textureCoordinates.add(0f);
                textureCoordinates.add(0f);

                normals.add(normal.x);
                normals.add(normal.y);
                normals.add(normal.z);
            }

            indices.add(firstIndex);
            indices.add(firstIndex + 1);
            indices.add(firstIndex + 2);
            if (polygon.vertices.size() == 4) {
                indices.add(firstIndex);
                indices.add(firstIndex + 2);
                indices.add(firstIndex + 3);
            }
        }

        {
            int[] data = toIntArray(indices);
            IntBuffer buffer = BufferUtils.createIntBuffer(data.length);
            Loader.getInstance().updateVBO(mesh.getVBO(-1), data, buffer);
        }

        float[] data = toFloatArray(positions);
        model.getMesh().setAABB(AABB.generateAABB(data));
        FloatBuffer buffer = BufferUtils.createFloatBuffer(data.length);
        Loader.getInstance().updateVBO(mesh.getVBO(0), data, buffer);

        data = toFloatArray(textureCoordinates);
        buffer = BufferUtils.createFloatBuffer(data.length);
        Loader.getInstance().updateVBO(mesh.getVBO(1), data, buffer);

        data = toFloatArray(normals);
        buffer = BufferUtils.createFloatBuffer(data.length);
        Loader.getInstance().updateVBO(mesh.getVBO(2), data, buffer);
    }

    public static void mergeVertices(List<Vertex> vertices) {
        Vertex keep = vertices.get(0);
        Vector3f average = new Vector3f(keep.position);
        for (int i = 1; i < vertices.size(); i++) {
            Vertex remove = vertices.get(i);
            Vector3f.add(average, remove.position, average);

            for (Polygon adjustment : remove.getMesh().getPolygonsWithVertex(remove))
                adjustment.vertices.set(adjustment.vertices.indexOf(remove), keep);

            vertices.remove(remove);
        }

        keep.position.set(VectorUtils.div(average, vertices.size()));
    }

    public static void breakVertices(List<Vertex> vertices) {
        for (Vertex vertex : vertices) {
            for (Polygon polygon : vertex.getMesh().getPolygonsWithVertex(vertex)) {
                Vertex copy = new Vertex(vertex.getMesh(), vertex.position);

                vertex.getMesh().vertices.add(copy);
                polygon.vertices.set(polygon.vertices.indexOf(vertex), copy);
            }

            vertex.getMesh().vertices.remove(vertex);
        }
    }

    public List<Polygon> getPolygonsWithVertex(Vertex vertex) {
        List<Polygon> results = new ArrayList<Polygon>();
        for (Polygon polygon : polygons)
            if (polygon.vertices.contains(vertex))
                results.add(polygon);

        return results;
    }

    public List<Vertex> getVerticesAtPosition(Vector3f position) {
        List<Vertex> results = new ArrayList<Vertex>();
        for (Vertex vertex : vertices)
            if (vertex.position.equals(position))
                results.add(vertex);

        return results;
    }

    public Matrix4f getTransformationMatrix() {
        return transformComponent.getTransformationMatrix();
    }

    public AABB getAABB() {
        if (model == null || model.getMesh() == null || model.getMesh().getAABB() == null)
            return null;

        return AABB.generateAABB(model.getMesh().getAABB(), getTransformationMatrix());
    }

    @Override
    public Vector3f getPosition() {
        return transformComponent.position;
    }

    @Override
    public Vector3f getRotation() {
        return transformComponent.rotation;
    }

    @Override
    public Vector3f getScale() {
        return transformComponent.scale;
    }

    private float[] toFloatArray(List<Float> in) {
        float[] array = new float[in.size()];
        for (int i = 0; i < in.size(); i++)
            array[i] = in.get(i);

        return array;
    }

    private int[] toIntArray(List<Integer> in) {
        int[] array = new int[in.size()];
        for (int i = 0; i < in.size(); i++)
            array[i] = in.get(i);

        return array;
    }

}
