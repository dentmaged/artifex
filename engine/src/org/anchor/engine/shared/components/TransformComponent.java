package org.anchor.engine.shared.components;

import org.anchor.engine.common.utils.CoreMaths;
import org.anchor.engine.common.utils.VectorUtils;
import org.anchor.engine.shared.entity.Entity;
import org.anchor.engine.shared.entity.IComponent;
import org.anchor.engine.shared.utils.Property;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

public class TransformComponent implements IComponent {

    @Property("Position")
    public Vector3f position = new Vector3f();

    @Property("Rotation")
    public Vector3f rotation = new Vector3f();

    @Property("Scale")
    public Vector3f scale = new Vector3f(1, 1, 1);

    @Override
    public void spawn(Entity entity) {

    }

    @Override
    public void setValue(String key, String value) {
        if (key.equals("position"))
            position.set(VectorUtils.stringToVector(value));

        if (key.equals("rotation"))
            rotation.set(VectorUtils.stringToVector(value));

        if (key.equals("scale"))
            scale.set(VectorUtils.stringToVector(value));
    }

    public Matrix4f getTransformationMatrix() {
        return CoreMaths.createTransformationMatrix(position, rotation, scale);
    }

    @Override
    public IComponent copy() {
        TransformComponent copy = new TransformComponent();
        copy.position = new Vector3f(position);
        copy.rotation = new Vector3f(rotation);
        copy.scale = new Vector3f(scale);

        return copy;
    }

}
