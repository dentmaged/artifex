package org.anchor.engine.shared.components.redirections;

import org.anchor.engine.shared.components.IComponent;
import org.anchor.engine.shared.entity.Entity;
import org.anchor.engine.shared.net.Redirect;

@Redirect("org.anchor.game.client.components.MeshComponent")
public class EngineMeshComponent implements IComponent {

    @Override
    public void spawn(Entity entity) {
        
    }

    @Override
    public void setValue(String key, String value) {
        
    }

    @Override
    public IComponent copy() {
        return null;
    }

}
