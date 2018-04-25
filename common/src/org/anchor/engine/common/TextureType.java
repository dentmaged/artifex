package org.anchor.engine.common;

import java.io.File;
import java.util.regex.Pattern;

public enum TextureType {

    FONT, GUI, HEIGHTMAP, PARTICLE, TERRAIN, WATER;

    public String getName() {
        return this.name().toLowerCase();
    }

    public File getDirectory() {
        return new File("res", getName());
    }

    public String withFile(String other) {
        return "res" + File.separator + getName() + File.separator + other;
    }

    public String extract(String text) {
        return text.replace("res" + File.separator + getName() + File.separator, "");
    }

    public boolean contains(String texture) {
        for (File file : getDirectory().listFiles()) {
            if (!file.isFile() || !file.getName().endsWith("png"))
                continue;

            if (texture.equalsIgnoreCase(file.getName().split(Pattern.quote("."))[0]))
                return true;
        }

        return false;
    }

}
