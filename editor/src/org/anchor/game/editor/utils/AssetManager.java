package org.anchor.game.editor.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import javax.swing.JComboBox;

import org.anchor.engine.common.TextureType;
import org.anchor.engine.common.utils.FileHelper;
import org.anchor.game.editor.terrain.shape.Shape;

public class AssetManager {

    public static List<String> getModels() {
        return recursiveGet(FileHelper.newGameFile("res"), new Filter() {

            @Override
            public boolean allow(File file, String tmp) {
                return file.getName().endsWith("obj") && new File(tmp + "_diffuse.png").exists();
            }

        });
    }

    public static List<String> getTextures() {
        return recursiveGet(FileHelper.newGameFile("res"), new Filter() {

            @Override
            public boolean allow(File file, String tmp) {
                return file.getName().endsWith("png");
            }

        });
    }

    public static List<String> getPrefabs() {
        return recursiveGet(FileHelper.newGameFile("res"), new Filter() {

            @Override
            public boolean allow(File file, String tmp) {
                return file.getName().endsWith("pfb");
            }

        });
    }

    private static List<String> recursiveGet(File parent, Filter filter) {
        return recursiveGet(parent, parent, filter);
    }

    private static List<String> recursiveGet(File trueParent, File parent, Filter filter) {
        List<String> files = new ArrayList<String>();

        for (File file : parent.listFiles()) {
            if (file.isDirectory())
                files.addAll(recursiveGet(trueParent, file, filter));

            String tmp = file.getAbsolutePath();
            tmp = tmp.substring(0, tmp.length() - 4);

            if (filter.allow(file, tmp))
                files.add(tmp.replace(trueParent.getAbsolutePath() + File.separator, "").replace(File.separatorChar, '/'));
        }

        return files;
    }

    public static List<String> getTerrainTextures() {
        List<String> files = new ArrayList<String>();

        for (File file : TextureType.TERRAIN.getDirectory().listFiles()) {
            if (!file.isFile() || !file.getName().endsWith("png"))
                continue;

            files.add(file.getName().split(Pattern.quote("."))[0]);
        }

        return files;
    }

    public static List<String> getHeightmaps() {
        List<String> files = new ArrayList<String>();

        for (File file : TextureType.HEIGHTMAP.getDirectory().listFiles()) {
            if (!file.isFile() || !file.getName().endsWith("png"))
                continue;

            files.add(file.getName().split(Pattern.quote("."))[0]);
        }

        return files;
    }

    public static int getIndex(JComboBox<String> dropdown, String value) {
        for (int i = 0; i < dropdown.getItemCount(); i++) {
            if (dropdown.getItemAt(i).equalsIgnoreCase(value))
                return i;
        }

        return 0;
    }

    public static Shape[] getShapes() {
        List<Shape> shapes = new ArrayList<Shape>();
        for (File file : FileHelper.newGameFile("brushes").listFiles())
            if (file.getName().toLowerCase().endsWith("png"))
                shapes.add(new Shape(file.getName().substring(0, file.getName().length() - 4)));

        Shape[] array = new Shape[shapes.size()];
        for (int i = 0; i < array.length; i++)
            array[i] = shapes.get(i);

        return array;
    }

}
