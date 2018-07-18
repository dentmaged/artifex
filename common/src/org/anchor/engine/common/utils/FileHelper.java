package org.anchor.engine.common.utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.Scanner;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.FileImageInputStream;
import javax.imageio.stream.ImageInputStream;

import org.anchor.engine.common.TextureType;

public class FileHelper {

    public static String game;

    public static File newGameFile(String path) {
        return new File(game, path);
    }

    public static File newGameFile(String parent, String child) {
        return new File(new File(game, parent), child);
    }

    public static File newGameFile(String parent, TextureType type, String child) {
        return newGameFile(parent, type.getName(), child);
    }

    public static File newGameFile(String parent, String child, String grandchild) {
        return new File(new File(game, parent), child + File.separator + grandchild);
    }

    public static String read(File file) {
        try {
            StringBuilder builder = new StringBuilder();
            Scanner scanner = new Scanner(file);

            while (scanner.hasNextLine())
                builder.append(scanner.nextLine()).append("\n");
            scanner.close();

            return builder.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "";
    }

    public static void write(File file, String text) {
        try {
            FileWriter writer = new FileWriter(file);
            writer.write(text);
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static File getAppData(String program) {
        String workingDirectory = "";
        String os = (System.getProperty("os.name")).toLowerCase();

        if (os.contains("win"))
            workingDirectory = System.getenv("AppData") + File.separator + program;
        else if (os.contains("mac"))
            workingDirectory = System.getProperty("user.home") + File.separator + "Library" + File.separator + "Application Support" + File.separator + program;
        else if (os.contains("nux"))
            workingDirectory = System.getProperty("user.dir") + File.separator + "." + program;

        return new File(workingDirectory);
    }

    public static void createIfNotExists(File file) {
        if (!file.getParentFile().exists())
            file.getParentFile().mkdirs();

        try {
            if (!file.exists())
                file.createNewFile();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static float getImageDimension(File imgFile) {
        int pos = imgFile.getName().lastIndexOf(".");
        if (pos == -1)
            throw new IllegalArgumentException("No extension for file: " + imgFile.getAbsolutePath());

        String suffix = imgFile.getName().substring(pos + 1);
        Iterator<ImageReader> iter = ImageIO.getImageReadersBySuffix(suffix);
        while (iter.hasNext()) {
            ImageReader reader = iter.next();
            try {
                ImageInputStream stream = new FileImageInputStream(imgFile);
                reader.setInput(stream);

                return reader.getWidth(reader.getMinIndex());
            } catch (IOException e) {

            } finally {
                reader.dispose();
            }
        }

        throw new IllegalArgumentException("Not a known image file: " + imgFile.getAbsolutePath());
    }

}
