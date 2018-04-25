package org.anchor.engine.common.launcher;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.anchor.engine.common.app.App;
import org.anchor.engine.common.console.GameVariableManager;

public class Launcher {

    public static String game;
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

        game = args.get("game");
        for (Entry<String, String> entry : args.entrySet()) {
            System.out.println(entry.getKey() + " = " + entry.getValue());
        }

        System.out.println("Loading " + new File(new File(game, "bin"), "game.jar").getAbsolutePath() + "...");
        loadJar(new File(new File(game, "bin"), "game.jar"), "org.anchor.game.GameStart", "GameVarInit");

        for (int i = 0; i < arguments.length; i++) {
            String next = getNextArg(arguments, i);

            if (arguments[i].startsWith("+")) {
                if (next.startsWith("-"))
                    next = "1";

                GameVariableManager.getByName(arguments[i].substring(1)).setValue(next);
            }
        }

        runMethod("org.anchor.game.GameStart", "GameInit");
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
