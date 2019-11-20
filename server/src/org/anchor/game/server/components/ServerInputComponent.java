package org.anchor.game.server.components;

import org.anchor.engine.common.utils.Mathf;
import org.anchor.engine.common.utils.VectorUtils;
import org.anchor.engine.shared.components.LivingComponent;
import org.anchor.engine.shared.net.packet.PlayerMovementPacket;
import org.anchor.engine.shared.physics.PhysicsEngine;
import org.lwjgl.util.vector.Vector3f;

public class ServerInputComponent extends LivingComponent {

    public PlayerMovementPacket playerMovementPacket;

    {
        canShoot = true;
    }

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

            fire = playerMovementPacket.fire;
            reload = playerMovementPacket.reload;
            selectedIndex = playerMovementPacket.selectedWeapon;

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
                if (playerMovementPacket.jump) {
                    entity.getPosition().y += 2 * JUMP_POWER;
                    isInAir = true;
                } else if (playerMovementPacket.walk) {
                    entity.getPosition().y -= 2 * JUMP_POWER;
                }
            } else if (isInLiquid) {
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
