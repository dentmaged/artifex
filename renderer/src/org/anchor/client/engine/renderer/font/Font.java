package org.anchor.client.engine.renderer.font;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.anchor.client.engine.renderer.Loader;
import org.anchor.engine.common.utils.FileHelper;
import org.lwjgl.opengl.Display;

public class Font {

    protected static final int SPACE_ASCII = 32;

    private static final int PAD_TOP = 0;
    private static final int PAD_LEFT = 1;
    private static final int PAD_BOTTOM = 2;
    private static final int PAD_RIGHT = 3;

    private static final int DESIRED_PADDING = 8;

    private File file;
    private int atlas;

    private float verticalPerPixelSize;
    private float horizontalPerPixelSize;
    private float spaceWidth;
    private int[] padding;
    private int paddingWidth;
    private int paddingHeight;
    private boolean isCharacter;

    private Map<Integer, Character> metaData = new HashMap<Integer, Character>();

    private BufferedReader reader;
    private Map<String, String> values = new HashMap<String, String>();

    private static List<Font> instances = new ArrayList<Font>();

    public Font(String file) {
        this(FileHelper.newGameFile("res", "font", file + ".fnt"));
    }

    public Font(File file) {
        this.file = file;
        loadData();

        instances.add(this);
    }

    private void loadData() {
        float aspectRatio = (float) Display.getWidth() / (float) Display.getHeight();

        try {
            reader = new BufferedReader(new FileReader(file));

            readNextLine();
            padding = getValues("padding");

            paddingWidth = padding[PAD_LEFT] + padding[PAD_RIGHT];
            paddingHeight = padding[PAD_TOP] + padding[PAD_BOTTOM];

            readNextLine();
            float lineHeightPixels = getValue("lineHeight") - paddingHeight;

            verticalPerPixelSize = getLineHeight() / lineHeightPixels;
            horizontalPerPixelSize = verticalPerPixelSize / aspectRatio;

            loadCharacterData();
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Couldn't read font meta file!");
        }
    }

    protected float getSpaceWidth() {
        return spaceWidth;
    }

    protected Character getCharacter(int ascii) {
        return metaData.get(ascii);
    }

    private boolean readNextLine() {
        String line = null;
        try {
            line = reader.readLine();
        } catch (IOException e) {
        }

        values.clear();
        if (line == null)
            return false;

        isCharacter = line.startsWith("char");
        for (String part : line.split(" ")) {
            String[] valuePairs = part.split("=");
            if (valuePairs.length == 2)
                values.put(valuePairs[0], valuePairs[1]);
        }

        return true;
    }

    private int getValue(String key) {
        return Integer.parseInt(values.get(key));
    }

    private int[] getValues(String key) {
        String[] numbers = values.get(key).split(",");
        int[] values = new int[numbers.length];

        for (int i = 0; i < values.length; i++)
            values[i] = Integer.parseInt(numbers[i]);

        return values;
    }

    private void loadCharacterData() {
        int imageWidth = getValue("scaleW");

        readNextLine();
        String file = values.get("file");
        atlas = Loader.getInstance().loadTexture("font/" + file.substring(1, file.length() - 5));

        readNextLine();
        while (readNextLine()) {
            if (!isCharacter)
                continue;

            Character c = loadCharacter(imageWidth);
            if (c != null)
                metaData.put(c.getId(), c);
        }
    }

    private Character loadCharacter(int imageSize) {
        int id = getValue("id");
        if (id == SPACE_ASCII) {
            spaceWidth = (getValue("xadvance") - paddingWidth) * horizontalPerPixelSize;

            return null;
        }

        float width = getValue("width") - (paddingWidth - (2 * DESIRED_PADDING));
        float height = getValue("height") - (paddingHeight - (2 * DESIRED_PADDING));

        return new Character(id, ((float) getValue("x") + (padding[PAD_LEFT] - DESIRED_PADDING)) / imageSize, ((float) getValue("y") + (padding[PAD_TOP] - DESIRED_PADDING)) / imageSize, width / imageSize, height / imageSize, (getValue("xoffset") + padding[PAD_LEFT] - DESIRED_PADDING) * horizontalPerPixelSize, (getValue("yoffset") + (padding[PAD_TOP] - DESIRED_PADDING)) * verticalPerPixelSize, width * horizontalPerPixelSize, height * verticalPerPixelSize, (getValue("xadvance") - paddingWidth) * horizontalPerPixelSize);
    }

    public static void reloadData() {
        for (Font data : instances) {
            data.metaData.clear();
            data.loadData();
        }
    }

    public int getAtlas() {
        return atlas;
    }

    public static float getLineHeight() {
        return 0.03f / ((float) Display.getHeight() / 720f);
    }

}
