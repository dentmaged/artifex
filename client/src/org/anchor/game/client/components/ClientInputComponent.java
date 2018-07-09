package org.anchor.game.client.components;

import org.anchor.engine.common.utils.Mathf;
import org.anchor.engine.common.utils.VectorUtils;
import org.anchor.engine.shared.components.LivingComponent;
import org.anchor.engine.shared.physics.PhysicsEngine;
import org.anchor.game.client.audio.Audio;
import org.anchor.game.client.utils.KeyboardUtils;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

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
            if (KeyboardUtils.isKeyDown(Keyboard.KEY_W))
                forwards = selectedSpeed;
            if (KeyboardUtils.isKeyDown(Keyboard.KEY_S))
                forwards -= selectedSpeed;

            sideways = 0;
            if (KeyboardUtils.isKeyDown(Keyboard.KEY_A))
                sideways = selectedSpeed;
            if (KeyboardUtils.isKeyDown(Keyboard.KEY_D))
                sideways -= selectedSpeed;

            if (forwards != 0 && sideways != 0) {
                forwards *= constant;
                sideways *= constant;
            }

            if (isInAir) {
                forwards *= 0.15f;
                sideways *= 0.15f;
            }

            fire = Mouse.isButtonDown(0);
            reload = KeyboardUtils.wasKeyJustPressed(Keyboard.KEY_R);

            if (VectorUtils.horizontalLength(entity.getVelocity()) < selectedSpeed && !isInWater) {
                entity.getVelocity().x += (Math.sin(yaw / 180 * pi)) * (forwards * PhysicsEngine.TICK_DELAY);
                entity.getVelocity().z -= (Math.cos(yaw / 180 * pi)) * (forwards * PhysicsEngine.TICK_DELAY);

                entity.getVelocity().x += (Math.sin((yaw - 90) / 180 * pi)) * (sideways * PhysicsEngine.TICK_DELAY);
                entity.getVelocity().z -= (Math.cos((yaw - 90) / 180 * pi)) * (sideways * PhysicsEngine.TICK_DELAY);

                float velocityClamp = 50 * PhysicsEngine.TICK_DELAY;
                entity.getVelocity().x = Mathf.clamp(entity.getVelocity().x, -velocityClamp, velocityClamp);
                entity.getVelocity().y = Mathf.clamp(entity.getVelocity().y, -velocityClamp, velocityClamp);
                entity.getVelocity().z = Mathf.clamp(entity.getVelocity().z, -velocityClamp, velocityClamp);
            }

            if (!gravity) {
                if (KeyboardUtils.isKeyDown(Keyboard.KEY_SPACE)) {
                    entity.getPosition().y += 2 * JUMP_POWER;
                    isInAir = true;
                } else if (KeyboardUtils.isKeyDown(Keyboard.KEY_LSHIFT)) {
                    entity.getPosition().y -= 2 * JUMP_POWER;
                }
            } else if (isInWater) {
                if (KeyboardUtils.isKeyDown(Keyboard.KEY_SPACE)) {
                    entity.getPosition().y += 0.5f * JUMP_POWER;
                } else if (KeyboardUtils.isKeyDown(Keyboard.KEY_LSHIFT)) {
                    entity.getPosition().y -= 0.5f * JUMP_POWER;
                }
            } else {
                space = KeyboardUtils.wasKeyJustPressed(Keyboard.KEY_SPACE);
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
    }

    @Override
    public void setSoundData() {
        Audio.setListenerData(getEyePosition(), entity.getVelocity());
    }

}
