package org.anchor.engine.shared.components.redirections;

import org.anchor.engine.shared.entity.Entity;
import org.anchor.engine.shared.entity.IComponent;
import org.anchor.engine.shared.net.Redirect;

@Redirect("org.anchor.game.client.components.MeshComponent")
public class EngineMeshComponent implements IComponent {

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
