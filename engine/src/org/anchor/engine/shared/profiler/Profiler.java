package org.anchor.engine.shared.profiler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.anchor.engine.common.Log;

public class Profiler {

    private static Map<Thread, List<Profile>> threads = new HashMap<Thread, List<Profile>>();

    public static void start(String name) {
        List<Profile> profiles = threads.get(Thread.currentThread());
        if (profiles == null) {
            profiles = new ArrayList<Profile>();
            threads.put(Thread.currentThread(), profiles);
        }

        Profile found = null;
        for (Profile profile : profiles) {
            if (profile.getDuration() == -1) {
                found = profile;
                break;
            }
        }

        if (found != null)
            found.start(name);
        else
            profiles.add(new Profile(name));
    }

    public static void end(String name) {
        for (Profile profile : threads.get(Thread.currentThread())) {
            if (profile.getDuration() == -1) {
                profile.end(name);
                break;
            }
        }
    }

    public static void frameEnd() {
        for (Entry<Thread, List<Profile>> entry : threads.entrySet())
            entry.getValue().clear();
    }

    public static void dump() {
        for (Entry<Thread, List<Profile>> entry : threads.entrySet()) {
            Log.debug("Thread " + entry.getKey().getName() + " (id " + entry.getKey().getId() + ")");

            for (Profile profile : entry.getValue()) {
                Log.debug("\t" + profile.getName() + ": " + profile.getDuration() + "ms");
                print(profile, "\t\t");
            }
        }
    }

    private static void print(Profile profile, String base) {
        for (Profile child : profile.getChildren()) {
            Log.debug(base + child.getName() + ": " + child.getDuration() + "ms");
            print(child, base + "\t");
        }
    }

    public static Map<Thread, List<Profile>> getThreads() {
        return threads;
    }

}
