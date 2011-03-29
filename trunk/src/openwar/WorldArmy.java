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
import java.util.Stack;
import openwar.WorldMap.PathTile;

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
    Stack<PathTile> route;
    boolean onRoute = false;

    public WorldArmy() {
    }

    WorldArmy(int x, int z, int player, Spatial m, WorldMap map) {
        posX = x;
        posZ = z;
        playerNumber = player;
        this.map = map;
        model = m;
        model.setShadowMode(ShadowMode.CastAndReceive);




        control = new CharacterControl(new CapsuleCollisionShape(0.25f, 1.5f, 1), 10000f);
        model.addControl(control);

        Vector3f vec = map.getGLTileCenter(x, z);
        vec.addLocal(0, 1f, 0);
        control.setPhysicsLocation(vec);


        model.scale(0.2f);
        units = new ArrayList<ArmyUnit>();

    }

    public int calculateMovePoints() {

        int points = 10000;
        for (ArmyUnit u : units) {
            points = Math.min(u.currMovePoints, points);
        }
        return currMovePoints = points;

    }

    public int resetMovePoints() {
        for (ArmyUnit u : units) {
            u.resetMovePoints();
        }
        return calculateMovePoints();
    }

    public void update(float tpf) {

        if (onRoute) {

            // check if goal reached
            if (route.isEmpty()) {
                route = null;
                onRoute = false;
                System.out.println("Goal reached");
                control.setWalkDirection(Vector3f.ZERO);


            } else {

                PathTile t = route.peek();
                Vector3f checkpoint = map.getGLTileCenter(t.x, t.z);
                Vector3f location = control.getPhysicsLocation().subtractLocal(0, 0.75f, 0);

                // next checkpoint reached
                if (checkpoint.distanceSquared(location) < 0.15f) {
                    route.pop();
                    System.out.println("Checkpoint reached");
                    control.setWalkDirection(Vector3f.ZERO);


                    if (route.isEmpty()) {
                        control.setPhysicsLocation(checkpoint.addLocal(0, 0.75f, 0));                   
                        return;
                    }

                    t = route.peek();
                    checkpoint = map.getGLTileCenter(t.x, t.z);
                    Vector3f dir = checkpoint.subtractLocal(location);
                    dir.normalizeLocal().multLocal(0.5f * tpf).setY(0);
                    control.setWalkDirection(dir);
                }
            }



        }

    }

    public void setRoute(Stack<PathTile> r) {
        route = r;
        onRoute = true;
    }
}
