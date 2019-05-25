package org.anchor.game.editor.utils.file;

import java.io.File;

public interface Filter {

    public boolean allow(File file);

}
