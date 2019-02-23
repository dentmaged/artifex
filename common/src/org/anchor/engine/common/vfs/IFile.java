package org.anchor.engine.common.vfs;

import java.io.InputStream;

public interface IFile {

    public String getName();

    public String getAbsolutePath();

    public InputStream openInputStream();

    public boolean isDirectory();

    public boolean exists();

}
