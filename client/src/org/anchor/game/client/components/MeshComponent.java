package org.anchor.game.client.components;

import org.anchor.client.engine.renderer.types.Model;
import org.anchor.engine.common.utils.AABB;
import org.anchor.engine.shared.components.IComponent;
import org.anchor.engine.shared.components.PhysicsComponent;
import org.anchor.engine.shared.entity.Entity;
import org.anchor.engine.shared.utils.CollisionMeshLoader;
import org.anchor.engine.shared.utils.Property;
import org.anchor.game.client.loaders.ModelLoader;
import org.anchor.game.client.shaders.NormalShader;
import org.anchor.game.client.shaders.StaticShader;
import org.anchor.game.client.shaders.WaterShader;
import org.anchor.game.client.types.ClientShader;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

public class MeshComponent implements IComponent {

    @Property("Model")
    public Model model;

    @Property("Texture Index")
    public int textureIndex;

    @Property("Disable Frustum Culling")
    public boolean disableFrustumCulling;

    public ClientShader shader;
    public boolean castsShadows = true;

    public Vector4f colour = new Vector4f();
    protected Entity entity;

    private boolean ran;

    @Override
    public void spawn(Entity entity) {
        this.entity = entity;
        if (model == null && entity.containsKey("model"))
            model = ModelLoader.loadModel(entity.getValue("model"));

        if (model != null && model.isLoaded()) {
            ran = true;
            if (entity.containsKey("backface"))
                model.getTexture().setCullingEnabled(Boolean.parseBoolean(entity.getValue("backface")));

            if (entity.containsKey("blending"))
                model.getTexture().setBlendingEnabled(Boolean.parseBoolean(entity.getValue("blending")));

            if (entity.containsKey("shineDamper"))
                model.getTexture().setShineDamper(Float.parseFloat(entity.getValue("shineDamper")));

            if (entity.containsKey("reflectivity"))
                model.getTexture().setReflectivity(Float.parseFloat(entity.getValue("reflectivity")));

            if (shader == null)
                refreshShader();
        }
    }

    @Override
    public void update() {
        if (model != null && model.isLoaded() && !ran) {
            ran = true;

            if (entity.containsKey("backface"))
                model.getTexture().setCullingEnabled(Boolean.parseBoolean(entity.getValue("backface")));

            if (entity.containsKey("blending"))
                model.getTexture().setBlendingEnabled(Boolean.parseBoolean(entity.getValue("blending")));

            if (entity.containsKey("shineDamper"))
                model.getTexture().setShineDamper(Float.parseFloat(entity.getValue("shineDamper")));

            if (entity.containsKey("reflectivity"))
                model.getTexture().setReflectivity(Float.parseFloat(entity.getValue("reflectivity")));

            if (shader == null)
                refreshShader();
        }
    }

    public Vector2f getTextureOffset() {
        float column = textureIndex % model.getTexture().getNumberOfRows();
        float row = textureIndex / model.getTexture().getNumberOfRows();

        return new Vector2f(column / (float) model.getTexture().getNumberOfRows(), row / (float) model.getTexture().getNumberOfRows());
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
    }

    @Override
    public IComponent copy() {
        MeshComponent copy = new MeshComponent();
        copy.model = model;
        copy.shader = shader;
        copy.textureIndex = textureIndex;
        copy.colour = new Vector4f(colour);

        return copy;
    }

    @Property("Refresh Shader")
    public void refreshShader() {
        if (model == null || !model.isLoaded())
            return;

        if (model.getTexture().getNormalMap() != 0)
            shader = NormalShader.getInstance();
        else
            shader = StaticShader.getInstance();

        if (entity.hasComponent(WaterComponent.class))
            shader = WaterShader.getInstance();
    }

    @Property("Set collision mesh (AABB)")
    public void setCollisionMeshAABB() {
        AABB aabb = model.getMesh().getAABB();
        if (entity == null || aabb == null)
            return;

        PhysicsComponent component = entity.getComponent(PhysicsComponent.class);
        if (component == null)
            entity.addComponent(component = new PhysicsComponent());

        entity.setValue("collisionMesh", "aabb");
        component.mesh = CollisionMeshLoader.loadCollisionMesh("aabb");
    }

}
