package org.anchor.game.client.components;

import org.anchor.client.engine.renderer.keyboard.Keys;
import org.anchor.engine.common.utils.Mathf;
import org.anchor.engine.common.utils.VectorUtils;
import org.anchor.engine.shared.components.LivingComponent;
import org.anchor.engine.shared.physics.PhysicsEngine;
import org.anchor.game.client.audio.Audio;
import org.lwjgl.input.Mouse;
import org.lwjgl.util.vector.Vector3f;

public class ClientInputComponent extends LivingComponent {

    public boolean space;

    @Override
    protected void checkInput() {
        if (Mouse.isGrabbed()) {
            float mouseDX = Mouse.getDX() * 6 * PhysicsEngine.TICK_DELAY;
            float mouseDY = Mouse.getDY() * 6 * PhysicsEngine.TICK_DELAY;

            if (yaw + mouseDX >= 360)
                mouseDX -= 360;
            else if (yaw + mouseDX < 0)
                mouseDX = 360 - yaw + mouseDX;

            pitch -= mouseDY;
            pitch = Math.min(90, Math.max(-90, pitch));
            yaw += mouseDX;

            forwards = 0;
            if (Keys.isKeyPressed("forward"))
                forwards = selectedSpeed;
            if (Keys.isKeyPressed("back"))
                forwards -= selectedSpeed;

            sideways = 0;
            if (Keys.isKeyPressed("left"))
                sideways = selectedSpeed;
            if (Keys.isKeyPressed("right"))
                sideways -= selectedSpeed;

            if (forwards != 0 && sideways != 0) {
                forwards *= constant;
                sideways *= constant;
            }

            fire = Mouse.isButtonDown(0);
            reload = Keys.isKeyPressed("reload");

            if (!isInLiquid) {
                Vector3f accelerateDirection = new Vector3f(Mathf.sinDegrees(yaw) * forwards * PhysicsEngine.TICK_DELAY + Mathf.sinDegrees(yaw - 90) * sideways * PhysicsEngine.TICK_DELAY, 0, -Mathf.cosDegrees(yaw) * forwards * PhysicsEngine.TICK_DELAY - Mathf.cosDegrees(yaw - 90) * sideways * PhysicsEngine.TICK_DELAY);

                if (accelerateDirection.lengthSquared() != 0) {
                    accelerateDirection.normalise();

                    float projectedVelocity = Vector3f.dot(entity.getVelocity(), accelerateDirection);
                    float maximumVelocity = selectedSpeed * PhysicsEngine.TICK_DELAY / (isInAir ? MAX_SPEED_AIR : MAX_SPEED_GROUND);
                    float accelerateVelocity = PhysicsEngine.TICK_DELAY * (isInAir ? ACCELERATE_AIR : ACCELERATE_GROUND);

                    if (projectedVelocity + accelerateVelocity > maximumVelocity)
                        accelerateVelocity = maximumVelocity - projectedVelocity;

                    Vector3f.add(entity.getVelocity(), VectorUtils.mul(accelerateDirection, accelerateVelocity), entity.getVelocity());
                }
            }

            if (!gravity) {
                if (Keys.isKeyPressed("jump")) {
                    entity.getPosition().y += 2 * JUMP_POWER;
                    isInAir = true;
                } else if (Keys.isKeyPressed("sprint")) {
                    entity.getPosition().y -= 2 * JUMP_POWER;
                }
            } else if (isInLiquid) {
                if (Keys.isKeyPressed("jump")) {
                    entity.getPosition().y += 0.5f * JUMP_POWER;
                } else if (Keys.isKeyPressed("sprint")) {
                    entity.getPosition().y -= 0.5f * JUMP_POWER;
                }
            } else {
                space = Keys.isKeyPressed("jump");
                Keys.setPressed("jump", false);
                if (space && !isInAir) {
                    entity.getVelocity().y += JUMP_POWER;
                    isInAir = true;
                    voluntaryJump = true;
                }
            }
        } else {
            forwards = 0;
            sideways = 0;
        }

        {
            Vector3f right = getRightVector();
            float side = Vector3f.dot(entity.getVelocity(), right) * ROLL_MULTIPLIER;

            float sign = side < 0 ? -1 : 1;
            side = Mathf.abs(side);
            float value = ROLL_ANGLE;

            if (side < ROLL_SPEED)
                side = side * value / ROLL_SPEED;
            else
                side = value;

            roll = side * sign;
        }
    }

    @Override
    public void setSoundData() {
        Audio.setListenerData(getEyePosition(), entity.getVelocity());
    }

}
