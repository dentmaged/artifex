package org.anchor.game.client.components;

import org.anchor.client.engine.renderer.types.Material;
import org.anchor.client.engine.renderer.types.Model;
import org.anchor.engine.common.utils.AABB;
import org.anchor.engine.common.utils.VectorUtils;
import org.anchor.engine.shared.components.PhysicsComponent;
import org.anchor.engine.shared.entity.Entity;
import org.anchor.engine.shared.entity.IComponent;
import org.anchor.engine.shared.utils.CollisionMeshLoader;
import org.anchor.engine.shared.utils.Property;
import org.anchor.game.client.loaders.AssetLoader;
import org.anchor.game.client.shaders.ForwardStaticShader;
import org.anchor.game.client.shaders.NormalShader;
import org.anchor.game.client.shaders.StaticShader;
import org.anchor.game.client.types.ClientShader;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

public class MeshComponent implements IComponent {

    @Property("Model")
    public Model model;

    @Property("Material")
    public Material material;

    @Property("Texture Index")
    public int textureIndex;

    @Property("Disable Frustum Culling")
    public boolean disableFrustumCulling;

    @Property("UV Scale X")
    public boolean uvX;

    @Property("UV Scale Y")
    public boolean uvY;

    @Property("UV Scale Z")
    public boolean uvZ;

    @Property("UV Scale")
    public Vector2f uvScale = new Vector2f(1, 1);

    public ClientShader shader;
    public boolean castsShadows = true, visible = true;

    public Vector4f colour = new Vector4f();
    protected Entity entity;

    @Override
    public void spawn(Entity entity) {
        this.entity = entity;
        if (model == null && entity.containsKey("model"))
            model = AssetLoader.loadModel(entity.getValue("model"));
        material = AssetLoader.loadMaterial(entity.getValue("material"));

        if (entity.containsKey("backface"))
            material.setCullingEnabled(Boolean.parseBoolean(entity.getValue("backface")));

        if (entity.containsKey("blending"))
            material.setBlendingEnabled(Boolean.parseBoolean(entity.getValue("blending")));

        if (shader == null)
            refreshShader();
    }

    @Override
    public void update() {
        if (shader == null)
            refreshShader();
    }

    public Vector2f getTextureOffset() {
        float column = textureIndex % material.getNumberOfRows();
        float row = textureIndex / material.getNumberOfRows();

        return new Vector2f(column / (float) material.getNumberOfRows(), row / (float) material.getNumberOfRows());
    }

    public AABB getAABB() {
        if (entity == null || model == null || model.getMesh() == null || model.getMesh().getAABB() == null)
            return null;

        return AABB.generateAABB(model.getMesh().getAABB(), entity.getTransformationMatrix());
    }

    public float getFurthestVertex() {
        if (entity == null || model == null || model.getMesh() == null || model.getMesh().getAABB() == null)
            return 0;

        Vector3f furthest = model.getMesh().getFurthestVertex();
        return Matrix4f.transform(entity.getTransformationMatrix(), new Vector4f(furthest.x, furthest.y, furthest.z, 0), null).length();
    }

    @Override
    public void setValue(String key, String value) {
        if (key.equals("castsShadows"))
            castsShadows = Boolean.parseBoolean(value);

        if (key.equals("disableFrustumCullling"))
            disableFrustumCulling = Boolean.parseBoolean(value);

        if (material != null) {
            if (key.equals("backface"))
                material.setCullingEnabled(Boolean.parseBoolean(value));

            if (key.equals("blending"))
                material.setBlendingEnabled(Boolean.parseBoolean(value));
        }
    }

    @Override
    public IComponent copy() {
        MeshComponent copy = new MeshComponent();
        copy.model = model;
        copy.material = material;
        copy.shader = shader;
        copy.textureIndex = textureIndex;
        copy.castsShadows = castsShadows;
        copy.disableFrustumCulling = disableFrustumCulling;
        copy.colour = new Vector4f(colour);
        copy.uvX = uvX;
        copy.uvY = uvY;
        copy.uvZ = uvZ;
        copy.uvScale = new Vector2f(uvScale);

        return copy;
    }

    @Property("Refresh Shader")
    public void refreshShader() {
        if (material == null)
            return;

        if (material.hasNormalMap())
            shader = NormalShader.getInstance();
        else
            shader = StaticShader.getInstance();

        if (material.isBlendingEnabled())
            shader = ForwardStaticShader.getInstance();
    }

    @Property("Set collision mesh")
    public void setCollisionMesh() {
        if (entity == null)
            return;

        PhysicsComponent component = entity.getComponent(PhysicsComponent.class);
        if (component == null)
            entity.addComponent(component = new PhysicsComponent());

        entity.setValue("collisionMesh", entity.getValue("model"));
        component.meshes = CollisionMeshLoader.loadCollisionMeshes(entity.getValue("collisionMesh"));
    }

    public Vector2f getUVScale() {
        Vector3f axis = new Vector3f(uvX ? 1 : 0, uvY ? 1 : 0, uvZ ? 1 : 0);
        float count = Vector3f.dot(axis, axis);

        float uniformScale = 1;
        if (count > 0)
            uniformScale = Vector3f.dot(axis, entity.getScale()) / count;

        return VectorUtils.mul(new Vector2f(uniformScale, uniformScale), uvScale);
    }

}
