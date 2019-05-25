package org.anchor.engine.common.utils;

import java.lang.management.ManagementFactory;

public class JavaUtils {

    public static boolean isDebuggerAttached() {
        for (String s : ManagementFactory.getRuntimeMXBean().getInputArguments())
            if (s.contains("jdwp"))
                return true;

        return false;
    }

}
