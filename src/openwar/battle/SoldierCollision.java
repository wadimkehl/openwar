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

/**
 *
 * @author kehl
 */
public class SoldierCollision extends GhostControl implements PhysicsCollisionListener {

    Soldier s;

    public SoldierCollision(CollisionShape shape, Soldier s) {
        super(shape);
        this.s = s;
        s.unit.battle.game.bulletState.getPhysicsSpace().addCollisionListener(this);

    }

    @Override
    public void collision(PhysicsCollisionEvent event) {

        Soldier a = s.unit.battle.hashedSoldiers.get(event.getNodeA());
        Soldier b = s.unit.battle.hashedSoldiers.get(event.getNodeB());
        if(a==s)
            a.collObjects.add(b);
        else
            b.collObjects.add(a);
       

    }
}
