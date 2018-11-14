package org.anchor.game.client.components;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.anchor.engine.shared.entity.Entity;
import org.anchor.engine.shared.entity.IComponent;
import org.anchor.engine.shared.physics.PhysicsEngine;
import org.anchor.engine.shared.utils.Property;
import org.anchor.game.client.loaders.AssetLoader;
import org.anchor.game.client.particles.Particle;
import org.anchor.game.client.particles.ParticleTexture;
import org.lwjgl.util.vector.Vector3f;

public class ParticleSystemComponent implements IComponent {

    @Property("Texture")
    public ParticleTexture texture;

    @Property("Particle Life Average")
    public float particleLifeAverage = 3;

    @Property("Particle Life Range")
    public float particleLifeRange = 1;

    @Property("Size Average")
    public float sizeAverage = 1;

    @Property("Size Range")
    public float sizeRange = 0.5f;

    @Property("Particles/Second")
    public float particlesPerSecond = 50;

    @Property("Gravity Strength")
    public float gravity;

    protected float particleCreateCount;
    protected Entity entity;

    protected List<Particle> particles = new ArrayList<Particle>();
    protected Random random = new Random();

    @Override
    public void spawn(Entity entity) {
        this.entity = entity;
        if (texture == null && entity.containsKey("particleTexture"))
            texture = AssetLoader.loadParticle(entity.getValue("particleTexture"));
    }

    @Override
    public void setValue(String key, String value) {

    }

    @Override
    public void updateFixed() {
        for (int i = 0; i < particles.size(); i++) {
            Particle particle = particles.get(i);

            particle.update();
            if (particle.getLife() <= 0)
                particles.remove(i);
        }

        particleCreateCount += PhysicsEngine.TICK_DELAY * particlesPerSecond;
        while (particleCreateCount >= 1) {
            particles.add(new Particle(texture, new Vector3f(entity.getPosition()), new Vector3f(0, 1, 0), random.nextFloat() * 360, get(sizeAverage, sizeRange), get(particleLifeAverage, particleLifeRange), gravity));
            particleCreateCount--;
        }
    }

    public ParticleTexture getTexture() {
        return texture;
    }

    public List<Particle> getParticles() {
        return particles;
    }

    private float get(float average, float range) {
        return average + (random.nextFloat() - 0.5f) * 2 * range;
    }

    @Override
    public IComponent copy() {
        ParticleSystemComponent copy = new ParticleSystemComponent();
        copy.texture = texture;
        copy.particleLifeAverage = particleLifeAverage;
        copy.particleLifeRange = particleLifeRange;
        copy.sizeAverage = sizeAverage;
        copy.sizeRange = sizeRange;
        copy.particlesPerSecond = particlesPerSecond;
        copy.gravity = gravity;

        return copy;
    }

}
