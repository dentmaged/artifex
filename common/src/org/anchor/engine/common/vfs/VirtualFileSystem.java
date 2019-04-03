package org.anchor.engine.common.vfs;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.anchor.engine.common.utils.FileHelper;

public class VirtualFileSystem {

    protected File parent;
    protected int importance;
    protected List<ZipEntry> entries;

    protected static List<VirtualFileSystem> fileSystems = new ArrayList<VirtualFileSystem>();

    public VirtualFileSystem(File parent) {
        this(parent, 0);
    }

    public VirtualFileSystem(File parent, int importance) {
        this.parent = parent;
        this.importance = importance;
        this.entries = new ArrayList<ZipEntry>();

        try {
            ZipInputStream stream = new ZipInputStream(new FileInputStream(parent));

            ZipEntry entry;
            while ((entry = stream.getNextEntry()) != null) {
                if (entry.isDirectory())
                    continue;

                entries.add(entry);
            }

            stream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        fileSystems.add(this);
    }

    public boolean exists(String path) {
        for (ZipEntry entry : entries)
            if (entry.getName().equals(path))
                return true;

        return false;
    }

    public VirtualFile getFile(String path) {
        for (ZipEntry entry : entries)
            if (entry.getName().equals(path))
                return new VirtualFile(parent, entry);

        return null;
    }

    public int getImportance() {
        return importance;
    }

    public void unload() {
        entries = null;
        fileSystems.remove(this);
    }

    public static void init() {
        for (File file : new File(new File(FileHelper.game, "child-of-game-dir").getAbsolutePath()).getParentFile().listFiles())
            if (file.getName().toLowerCase().endsWith(".ads"))
                new VirtualFileSystem(file);
    }

    public static IFile find(File file) {
        String path = FileHelper.localFileName(file, new File(new File("child-of-dir").getAbsolutePath()).getParentFile());

        if (path == null)
            return new DiskFile(file);
        return find(path);
    }

    public static IFile find(String path) {
        VirtualFileSystem mostImportant = null;
        int highest = -1;

        for (VirtualFileSystem fileSystem : fileSystems) {
            if (fileSystem.getImportance() > highest && fileSystem.exists(path)) {
                mostImportant = fileSystem;
                highest = fileSystem.getImportance();
            }
        }

        if (mostImportant != null)
            return mostImportant.getFile(path);
        return new DiskFile(new File(path));
    }

}
