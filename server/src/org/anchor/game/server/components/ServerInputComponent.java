package org.anchor.game.server.components;

import org.anchor.engine.common.utils.Mathf;
import org.anchor.engine.common.utils.VectorUtils;
import org.anchor.engine.shared.components.LivingComponent;
import org.anchor.engine.shared.net.packet.PlayerMovementPacket;
import org.anchor.engine.shared.physics.PhysicsEngine;

public class ServerInputComponent extends LivingComponent {

    public PlayerMovementPacket playerMovementPacket;

    @Override
    protected void checkInput() {
        if (playerMovementPacket != null) {
            pitch = Math.min(90, Math.max(-90, playerMovementPacket.pitch));
            yaw = playerMovementPacket.yaw;

            forwards = 0;
            if (playerMovementPacket.forwards)
                forwards = selectedSpeed;
            if (playerMovementPacket.backwards)
                forwards -= selectedSpeed;

            sideways = 0;
            if (playerMovementPacket.left)
                sideways = selectedSpeed;
            if (playerMovementPacket.right)
                sideways -= selectedSpeed;

            if (forwards != 0 && sideways != 0) {
                forwards *= constant;
                sideways *= constant;
            }

            if (isInAir) {
                forwards *= 0.15f;
                sideways *= 0.15f;
            }

            if (VectorUtils.horizontalLength(entity.getVelocity()) < selectedSpeed && !isInWater) {
                entity.getVelocity().x += Mathf.sinD(yaw) * forwards * PhysicsEngine.TICK_DELAY;
                entity.getVelocity().z -= Mathf.cosD(yaw) * forwards * PhysicsEngine.TICK_DELAY;

                entity.getVelocity().x += Mathf.sinD(yaw - 90) * sideways * PhysicsEngine.TICK_DELAY;
                entity.getVelocity().z -= Mathf.cosD(yaw - 90) * sideways * PhysicsEngine.TICK_DELAY;

                float velocityClamp = 50 * PhysicsEngine.TICK_DELAY;
                entity.getVelocity().x = Mathf.clamp(entity.getVelocity().x, -velocityClamp, velocityClamp);
                entity.getVelocity().y = Mathf.clamp(entity.getVelocity().y, -velocityClamp, velocityClamp);
                entity.getVelocity().z = Mathf.clamp(entity.getVelocity().z, -velocityClamp, velocityClamp);
            }

            fire = playerMovementPacket.fire;
            reload = playerMovementPacket.reload;
            selectedIndex = playerMovementPacket.selectedWeapon;

            if (!gravity) {
                if (playerMovementPacket.jump) {
                    entity.getPosition().y += 2 * JUMP_POWER;
                    isInAir = true;
                } else if (playerMovementPacket.walk) {
                    entity.getPosition().y -= 2 * JUMP_POWER;
                }
            } else if (isInWater) {
                if (playerMovementPacket.jump) {
                    entity.getPosition().y += 0.5f * JUMP_POWER;
                } else if (playerMovementPacket.walk) {
                    entity.getPosition().y -= 0.5f * JUMP_POWER;
                }
            } else {
                if (playerMovementPacket.jump && !isInAir) {
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

}
