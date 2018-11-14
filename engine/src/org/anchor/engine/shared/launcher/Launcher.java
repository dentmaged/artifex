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

    private static URLClassLoader child;

    public static void main(String[] arguments) {
        Map<String, String> args = new HashMap<String, String>();

        for (int i = 0; i < arguments.length; i++) {
            String next = getNextArg(arguments, i);
            if (arguments[i].startsWith("-")) {
                if (next.startsWith("-"))
                    next = "1";

                args.put(arguments[i].substring(1), next);
            }
        }

        FileHelper.game = args.get("game");
        for (Entry<String, String> entry : args.entrySet())
            Log.info(entry.getKey() + " = " + entry.getValue());

        String file = "client.jar";
        if (args.containsKey("server") && args.get("server").equals("1"))
            file = "server.jar";

        Log.info("Loading " + new File(new File(FileHelper.game, "bin"), file).getAbsolutePath() + "...");
        loadJar(new File(new File(FileHelper.game, "bin"), file), "org.anchor.game.GameStart", "gameVarInit");

        for (int i = 0; i < arguments.length; i++) {
            String next = getNextArg(arguments, i);

            if (arguments[i].startsWith("+")) {
                if (next == null || next.startsWith("-"))
                    next = "1";

                GameVariableManager.getByName(arguments[i].substring(1)).setValue(next);
            }
        }

        runMethod("org.anchor.game.GameStart", "gameInit");
    }

    private static String getNextArg(String[] args, int i) {
        if (i + 1 == args.length)
            return null;

        return args[i + 1];
    }

    private static Object loadJar(File path, String clazz, String methodName) {
        try {
            child = new URLClassLoader(new URL[] {
                    path.toURI().toURL()
            }, App.class.getClassLoader());
        } catch (Exception e) {
            e.printStackTrace();
        }

        return runMethod(clazz, methodName);
    }

    private static Object runMethod(String clazz, String methodName) {
        try {
            Class<?> classToLoad = Class.forName(clazz, true, child);

            Method method = classToLoad.getDeclaredMethod(methodName);
            Object instance = classToLoad.newInstance();

            return method.invoke(instance);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

}
