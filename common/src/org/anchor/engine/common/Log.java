package org.anchor.engine.common;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;

import org.anchor.engine.common.utils.LogCallback;

public class Log {

    public static List<LogCallback> callbacks = new ArrayList<LogCallback>();

    public static void print(Object obj) {
        System.out.println(obj);
        notify(String.valueOf(obj));
    }

    public static void debug(Object obj) {
        StackTraceElement caller = Thread.currentThread().getStackTrace()[2];
        String text = "[DEBUG] " + stripPackage(caller.getClassName()) + "::" + caller.getMethodName() + "() - " + String.valueOf(obj);

        System.out.println(text);
        notify(text);
    }

    public static void info(Object obj) {
        StackTraceElement caller = Thread.currentThread().getStackTrace()[2];
        String text = "[INFO] " + stripPackage(caller.getClassName()) + "::" + caller.getMethodName() + "() - " + String.valueOf(obj);

        System.out.println(text);
        notify(text);
    }

    public static void warning(Object obj) {
        StackTraceElement caller = Thread.currentThread().getStackTrace()[2];
        String text = "[WARNING] " + stripPackage(caller.getClassName()) + "::" + caller.getMethodName() + "() - " + String.valueOf(obj);

        System.out.println(text);
        notify(text);
    }

    public static void error(Object obj) {
        StackTraceElement caller = Thread.currentThread().getStackTrace()[2];
        String text = stripPackage(caller.getClassName()) + "::" + caller.getMethodName() + "() - " + String.valueOf(obj);

        System.out.println("[ERROR] " + text);
        notify("[ERROR] " + text);

        showPopup("Engine error", text, JOptionPane.ERROR_MESSAGE);
        System.exit(7);
    }

    public static void showPopup(String title, String message) {
        showPopup(title, message, JOptionPane.PLAIN_MESSAGE);
    }

    public static void showPopup(String title, String message, int type) {
        JOptionPane.showOptionDialog(null, message, title, JOptionPane.PLAIN_MESSAGE, type, null, new Object[] { "OK", }, "OK");
    }

    private static void notify(String text) {
        for (LogCallback callback : callbacks)
            callback.log(text);
    }

    private static String stripPackage(String className) {
        return className.substring(className.lastIndexOf('.') + 1);
    }

}
