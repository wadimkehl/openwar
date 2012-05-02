/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package openwar.battle;

import com.jme3.bullet.collision.PhysicsCollisionEvent;
import com.jme3.bullet.collision.PhysicsCollisionListener;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.control.GhostControl;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.UpdateControl;
import java.util.concurrent.Callable;

/**
 *
 * @author kehl
 */
public class SoldierCollision implements PhysicsCollisionListener {

    BattleAppState battle;

    public SoldierCollision(BattleAppState b) {
        battle = b;
    }

    @Override
    public void collision(PhysicsCollisionEvent event) {


        if (event.getNodeA().getName().equals("soldier")) {

            if (event.getNodeB().getName().equals("soldier")) {
                collisionBetweenSoldiers(event.getNodeA(), event.getNodeB());
            }


        }


    }

    public void collisionBetweenSoldiers(Spatial A, Spatial B) {
        Soldier a = battle.hashedSoldiers.get(A);
        Soldier b = battle.hashedSoldiers.get(B);      
        a.collision=true;
        a.collVec = a.currPos.subtract(b.currPos);

        

    }
}
