package org.anchor.engine.common.script;

import java.util.HashMap;
import java.util.Map;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import org.anchor.engine.common.utils.FileHelper;

public class ScriptLoader {

    private static Map<String, Script> scripts = new HashMap<String, Script>();

    public static Script loadScript(String file) {
        ScriptEngine engine = new ScriptEngineManager().getEngineByName("nashorn");
        String contents = FileHelper.read(FileHelper.newGameFile("res", "scripts", "engine.js")) + "\n\n" + FileHelper.read(FileHelper.newGameFile("res", file + ".js"));

        Script script = new Script(file, engine);
        scripts.put(file, script);

        try {
            engine.put("scriptEngine", engine);
            engine.eval(contents);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return script;
    }

    public static Script getScript(ScriptEngine engine) {
        for (Script script : scripts.values())
            if (script.getEngine() == engine)
                return script;

        return null;
    }

}
