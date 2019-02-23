package org.anchor.engine.common.vfs;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class VirtualFile implements IFile {

    protected File parent;
    protected ZipEntry entry;

    public VirtualFile(File parent, ZipEntry entry) {
        this.parent = parent;
        this.entry = entry;
    }

    @Override
    public String getName() {
        return entry.getName().substring(Math.max(0, entry.getName().lastIndexOf(File.separatorChar)));
    }

    @Override
    public String getAbsolutePath() {
        return parent.getAbsolutePath() + "!/" + entry.getName();
    }

    @Override
    public InputStream openInputStream() {
        try {
            ZipInputStream input = new ZipInputStream(new FileInputStream(parent));

            ZipEntry entry;
            while ((entry = input.getNextEntry()) != null) {
                if (entry.getName().equals(this.entry.getName()))
                    break;
            }

            return input;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public boolean isDirectory() {
        return entry.isDirectory();
    }

    @Override
    public boolean exists() {
        return true;
    }

}
