package org.anchor.engine.common.utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Scanner;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.FileImageInputStream;
import javax.imageio.stream.ImageInputStream;

import org.anchor.engine.common.Log;
import org.anchor.engine.common.TextureType;
import org.anchor.engine.common.vfs.IFile;
import org.anchor.engine.common.vfs.VirtualFileSystem;

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

    public static String read(InputStream stream) {
        try {
            StringBuilder builder = new StringBuilder();
            Scanner scanner = new Scanner(stream);

            while (scanner.hasNextLine())
                builder.append(scanner.nextLine()).append("\n");
            scanner.close();

            return builder.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "";
    }

    public static String read(File file) {
        return read(VirtualFileSystem.find(file));
    }

    public static String read(IFile file) {
        if (!file.exists()) {
            Log.warning("File not found: " + file.getName() + " (" + file.getAbsolutePath() + ")");
            return "";
        }

        return read(file.openInputStream());
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

    public static String localFileName(File file) {
        return localFileName(file, newGameFile("res"));
    }

    public static String localFileName(File file, File base) {
        if (file == null)
            return null;

        String absolute = base.getAbsolutePath();
        if (!file.getAbsolutePath().contains(absolute))
            return null;

        return file.getAbsolutePath().substring(absolute.length() + 1).replace(File.separatorChar, '/');
    }

    public static String removeFileExtension(String input) {
        if (input == null)
            return null;

        return input.substring(0, input.lastIndexOf('.'));
    }

}
