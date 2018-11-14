package org.anchor.game.client;

import org.anchor.engine.common.console.GameVariableType;
import org.anchor.engine.shared.console.GameVariable;
import org.anchor.engine.shared.console.GameVariableManager;

public class ClientGameVariables {

    public static GameVariable r_wireframe, r_showLightmaps, r_performSSAO, r_performLighting;
    public static GameVariable cl_showPerformanceInformation, cl_showPosition, cl_showProfiler;
    public static GameVariable sensitivity;
    public static GameVariable developer;

    public static void init() {
        r_wireframe = GameVariableManager.get("r_wireframe", "0", "Enables wireframe rendering", GameVariableType.CHEAT);
        r_showLightmaps = GameVariableManager.get("r_showLightmaps", "0", "Disables albedo textures", GameVariableType.CHEAT);
        r_performSSAO = GameVariableManager.get("r_performSSAO", "1", "Enables/disables SSAO", GameVariableType.CHEAT);
        r_performLighting = GameVariableManager.get("r_performLighting", "1", "Enables/disables lighting", GameVariableType.CHEAT);

        cl_showPerformanceInformation = GameVariableManager.get("cl_showPerformanceInformation", "0", "Shows GPU and CPU time", GameVariableType.CLIENT);
        cl_showPosition = GameVariableManager.get("cl_showPosition", "0", "Shows player position", GameVariableType.CLIENT);
        cl_showProfiler = GameVariableManager.get("cl_showProfiler", "0", "Shows the profiler and timing for methods", GameVariableType.CLIENT);

        sensitivity = GameVariableManager.get("sensitivity", "0.5", "Mouse sensitivity", GameVariableType.CLIENT);

        developer = GameVariableManager.get("developer", "1", "Enables developer features", GameVariableType.CLIENT);
    }

}
