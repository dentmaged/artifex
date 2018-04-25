package org.anchor.game.editor.components;

import org.anchor.engine.common.utils.VectorUtils;
import org.anchor.engine.shared.components.LivingComponent;
import org.anchor.engine.shared.physics.PhysicsEngine;
import org.anchor.game.client.utils.KeyboardUtils;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

public class EditorInputComponent extends LivingComponent {

    public static float MOUSE_WHEEL_MULTIPLIER = 0.4f;

    @Override
    protected void checkInput() {
        if (Mouse.isGrabbed()) {
            float mouseDX = Mouse.getDX() * 6 * PhysicsEngine.TICK_DELAY;
            float mouseDY = Mouse.getDY() * 6 * PhysicsEngine.TICK_DELAY;

            if (entity.getRotation().y + mouseDX >= 360)
                mouseDX -= 360;
            else if (entity.getRotation().y + mouseDX < 0)
                mouseDX = 360 - entity.getRotation().y + mouseDX;

            pitch -= mouseDY;
            pitch = Math.min(89, Math.max(-89, pitch));
            entity.getRotation().y += mouseDX;

            forwards = 0;
            if (KeyboardUtils.isKeyDown(Keyboard.KEY_W))
                forwards = SELECTED_SPEED;
            if (KeyboardUtils.isKeyDown(Keyboard.KEY_S))
                forwards -= SELECTED_SPEED;

            if (forwards == 0)
                forwards = Mouse.getDWheel() * MOUSE_WHEEL_MULTIPLIER;

            sideways = 0;
            if (KeyboardUtils.isKeyDown(Keyboard.KEY_A))
                sideways = SELECTED_SPEED;
            if (KeyboardUtils.isKeyDown(Keyboard.KEY_D))
                sideways -= SELECTED_SPEED;

            if (forwards != 0 && sideways != 0) {
                forwards *= constant;
                sideways *= constant;
            }

            if (VectorUtils.horizontalLength(entity.getVelocity()) < SELECTED_SPEED && !isInAir && !isInWater) {
                entity.getVelocity().x += (Math.sin(entity.getRotation().y / 180 * pi)) * (forwards * PhysicsEngine.TICK_DELAY);
                entity.getVelocity().z -= (Math.cos(entity.getRotation().y / 180 * pi)) * (forwards * PhysicsEngine.TICK_DELAY);

                entity.getVelocity().x += (Math.sin((entity.getRotation().y - 90) / 180 * pi)) * (sideways * PhysicsEngine.TICK_DELAY);
                entity.getVelocity().z -= (Math.cos((entity.getRotation().y - 90) / 180 * pi)) * (sideways * PhysicsEngine.TICK_DELAY);
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
                if (KeyboardUtils.isKeyPressed(Keyboard.KEY_SPACE) && !isInAir) {
                    entity.getVelocity().y += JUMP_POWER;
                    isInAir = true;
                    voluntaryJump = true;
                }
            }
        } else {
            forwards = Mouse.getDWheel() * MOUSE_WHEEL_MULTIPLIER;
            sideways = 0;

            if (VectorUtils.horizontalLength(entity.getVelocity()) < SELECTED_SPEED && !isInAir && !isInWater) {
                entity.getVelocity().x += (Math.sin(entity.getRotation().y / 180 * pi)) * (forwards * PhysicsEngine.TICK_DELAY);
                entity.getVelocity().z -= (Math.cos(entity.getRotation().y / 180 * pi)) * (forwards * PhysicsEngine.TICK_DELAY);
            }
        }
    }

}
