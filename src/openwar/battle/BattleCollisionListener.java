/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package openwar.battle;

import com.jme3.bullet.collision.PhysicsCollisionEvent;
import com.jme3.bullet.collision.PhysicsCollisionGroupListener;
import com.jme3.bullet.collision.PhysicsCollisionListener;
import com.jme3.bullet.collision.PhysicsCollisionObject;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.control.GhostControl;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;

/**
 *
 * @author kehl
 */
public class BattleCollisionListener  implements PhysicsCollisionListener {

    BattleAppState battle;

    public BattleCollisionListener(BattleAppState b)
    {
        battle = b;
    }

    @Override
    public void collision(PhysicsCollisionEvent event) {


        if (event.getNodeA().getName().equals("soldier")) {
            if (event.getNodeB().getName().equals("soldier")) {


                 Soldier a = battle.hashedSoldiers.get(event.getNodeA());
                Soldier b = battle.hashedSoldiers.get(event.getNodeB());

                // Check if enemies
                if (!a.unit.owner.equals(b.unit.owner)) {

                    if (a.enemy == null) a.enemy = b;                   
                    if (b.enemy == null) b.enemy = a;
                    
                }
                }

                
            }


        }



    }

    

