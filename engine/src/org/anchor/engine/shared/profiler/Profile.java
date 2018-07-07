package org.anchor.engine.shared.profiler;

import java.util.ArrayList;
import java.util.List;

public class Profile {

    private String name;
    private long start;
    private float duration;

    private List<Profile> children;

    private static final float NANO_TO_MILLI = 1f / 1000000f;

    public Profile(String name) {
        this.name = name;
        this.start = System.nanoTime();
        this.duration = -1;
        this.children = new ArrayList<Profile>();
    }

    public void start(String name) {
        get().children.add(new Profile(name));
    }

    public void end(String name) {
        Profile get = get(name);
        get.duration = (System.nanoTime() - get.start) * NANO_TO_MILLI;
    }

    private Profile get() {
        for (Profile child : children)
            if (child.duration == -1)
                return child.get();

        return this;
    }

    private Profile get(String name) {
        if (this.name.equals(name))
            return this;

        for (Profile child : children) {
            Profile get = child.get(name);
            if (get != null)
                return get;
        }

        return null;
    }

    public String getName() {
        return name;
    }

    public float getDuration() {
        return duration;
    }

    public List<Profile> getChildren() {
        return children;
    }

}
