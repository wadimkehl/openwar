/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package openwar;

import com.jme3.bounding.BoundingBox;
import com.jme3.bullet.collision.shapes.CapsuleCollisionShape;
import com.jme3.bullet.control.CharacterControl;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue.ShadowMode;
import com.jme3.scene.Spatial;
import java.util.ArrayList;

/**
 *
 * @author kehl
 */
public class WorldArmy {

    int posX, posZ;
    int playerNumber;
    int currMovePoints;
    Spatial model;
    CharacterControl control;
    WorldMap map;
    ArrayList<ArmyUnit> units;

    public WorldArmy() {
    }

    WorldArmy(int x, int z, int player, Spatial m, WorldMap map) {
        posX = x;
        posZ = z;
        playerNumber = player;
        this.map = map;
        model = m;
        model.setShadowMode(ShadowMode.CastAndReceive);

        model.scale(0.3f);


        control = new CharacterControl(new CapsuleCollisionShape(1.5f, 0f, 1), 1f);
        model.addControl(control);

        control.setPhysicsLocation(map.getGLTileCenter(x, z)); 
        
        units = new ArrayList<ArmyUnit>();

    }

    public int calculateMovePoints() {

        int points = 10000;
        for(ArmyUnit u: units)
            points = Math.min(u.currMovePoints, points);
        return currMovePoints = points;
            
    }
    
    public int resetMovePoints()
    {
        for(ArmyUnit u: units)
            u.resetMovePoints();
        return calculateMovePoints();
    }

   
        
    public void update(float tpf)
    {
        
    }
}
