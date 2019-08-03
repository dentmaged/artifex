package org.anchor.engine.shared.launcher;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.anchor.engine.common.Log;
import org.anchor.engine.common.app.App;
import org.anchor.engine.common.utils.FileHelper;
import org.anchor.engine.shared.console.GameVariableManager;

public class Launcher {

    public static void main(String[] arguments) {
        Map<String, String> args = new HashMap<String, String>();

        for (int i = 0; i < arguments.length; i++) {
            String next = getNextArg(arguments, i);
            if (arguments[i].startsWith("-")) {
                if (next == null || next.startsWith("-") || next.startsWith("+"))
                    next = "1";

                args.put(arguments[i].substring(1), next);
            }
        }

        FileHelper.game = args.get("game");
        for (Entry<String, String> entry : args.entrySet())
            Log.info(entry.getKey() + " = " + entry.getValue());

        String fileName = "client.jar";
        if (args.containsKey("server") && args.get("server").equals("1"))
            fileName = "server.jar";

        File jar = new File(new File(FileHelper.game, "bin"), fileName);
        Log.info("Loading " + jar.getAbsolutePath() + "...");

        try {
            URLClassLoader child = new URLClassLoader(new URL[] { jar.toURI().toURL() }, App.class.getClassLoader());
            Class<?> clazz = Class.forName("org.anchor.game.GameStart", true, child);

            Method method = clazz.getDeclaredMethod("gameVarInit", Map.class);
            method.invoke(null, args);

            for (int i = 0; i < arguments.length; i++) {
                String next = getNextArg(arguments, i);

                if (arguments[i].startsWith("+")) {
                    if (next == null || next.startsWith("-") || next.startsWith("+"))
                        next = "1";

                    GameVariableManager.getByName(arguments[i].substring(1)).setValue(next);
                }
            }

            method = clazz.getDeclaredMethod("gameInit");
            method.invoke(null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String getNextArg(String[] args, int i) {
        if (i + 1 == args.length)
            return null;

        return args[i + 1];
    }

}
