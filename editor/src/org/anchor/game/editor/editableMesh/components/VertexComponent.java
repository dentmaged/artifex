package org.anchor.game.editor.editableMesh.components;

import java.util.ArrayList;
import java.util.List;

import org.anchor.engine.shared.editor.TransformableObject;
import org.anchor.engine.shared.entity.Entity;
import org.anchor.engine.shared.entity.IComponent;
import org.anchor.engine.shared.utils.Property;
import org.anchor.game.editor.editableMesh.EditableMesh;
import org.anchor.game.editor.editableMesh.types.Vertex;
import org.anchor.game.editor.ui.LevelEditor;

public class VertexComponent implements IComponent {

    @Property("Merge")
    public void merge() {
        List<Vertex> vertices = new ArrayList<Vertex>();
        for (TransformableObject object : LevelEditor.getInstance().getSelectedObjects())
            if (object instanceof Vertex)
                vertices.add((Vertex) object);

        EditableMesh.mergeVertices(vertices);
    }

    @Property("Break")
    public void breakVertices() {
        List<Vertex> vertices = new ArrayList<Vertex>();
        for (TransformableObject object : LevelEditor.getInstance().getSelectedObjects())
            if (object instanceof Vertex)
                vertices.add((Vertex) object);

        EditableMesh.breakVertices(vertices);
    }

    @Override
    public void precache(Entity entity) {

    }

    @Override
    public void spawn() {

    }

    @Override
    public void setValue(String key, String value) {

    }

    @Override
    public IComponent copy() {
        return null;
    }

}
