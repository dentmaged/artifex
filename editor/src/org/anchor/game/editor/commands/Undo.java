package org.anchor.game.editor.commands;

import java.util.ArrayList;
import java.util.List;

import org.anchor.engine.common.utils.JavaField;
import org.anchor.engine.shared.entity.Entity;
import org.anchor.engine.shared.entity.IComponent;
import org.lwjgl.util.vector.Vector3f;

public class Undo {

    private static List<CommandCallback> history = new ArrayList<CommandCallback>(), redo = new ArrayList<CommandCallback>();
    private static boolean changedSinceSave;

    public static void registerEntity(Entity entity) {
        addCommandToHistory(new CreateEntityCommand(entity));
    }

    public static <T extends IComponent> void addComponent(Entity entity, Class<T> component) {
        try {
            addComponent(entity, component.newInstance());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void addComponent(Entity entity, IComponent component) {
        entity.addComponent(component);
        addCommandToHistory(new AddComponentCallback(entity, component));
    }

    public static void registerChange(Vector3f target, Vector3f difference) {
        addCommandToHistory(new VectorChangeCallback(target, difference));
    }

    public static void fieldSet(JavaField field, Object target, Object value) {
        Object previous = field.get(target);
        field.set(target, value);

        addCommandToHistory(new FieldSetCallback(field, target, value, previous));
    }

    public static void addCommandToHistory(CommandCallback callback) {
        history.add(callback);
        redo.clear();

        changedSinceSave = true;
    }

    public static void undo() {
        if (history.size() == 0)
            return;

        CommandCallback callback = history.get(history.size() - 1);
        redo.add(callback);
        history.remove(callback);

        callback.undo();
    }

    public static void redo() {
        if (redo.size() == 0)
            return;

        CommandCallback callback = redo.get(redo.size() - 1);
        redo.remove(callback);
        history.add(callback);

        callback.redo();
    }

    public static void clearHistory() {
        history.clear();
        redo.clear();
    }

    public static void saved() {
        changedSinceSave = false;
    }

    public static boolean hasChangedSinceSave() {
        return changedSinceSave;
    }

}
