/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package openwar.world;

import com.jme3.bullet.collision.shapes.CapsuleCollisionShape;
import com.jme3.bullet.control.CharacterControl;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue.ShadowMode;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.UpdateControl;
import java.util.ArrayList;
import java.util.Stack;
import java.util.concurrent.Callable;
import openwar.DB.Settlement;
import openwar.DB.Unit;

/**
 *
 * @author kehl
 */
public class Army {

    int posX, posZ;
    String player;
    int currMovePoints;
    Spatial model;
    WorldMap map;
    ArrayList<Unit> units;
    Stack<Tile> route;
    boolean onRoute = false;
    public Vector3f locationGL;

    public Army() {
    }

    public Army(int x, int z, String p, Spatial m, WorldMap map) {
        posX = x;
        posZ = z;
        player = p;
        this.map = map;
        model = m;
        model.setShadowMode(ShadowMode.CastAndReceive);


        locationGL = map.getGLTileCenter(x, z);
        m.setLocalTranslation(locationGL);
        units = new ArrayList<Unit>();

    }

    public void addUnit(Unit u) {
        units.add(u);
        calculateMovePoints();
    }

    public int calculateMovePoints() {

        int points = 10000;
        for (Unit u : units) {
            points = Math.min(u.currMovePoints, points);
        }
        return currMovePoints = points;

    }

    public int resetMovePoints() {
        for (Unit u : units) {
            u.resetMovePoints();
        }
        return calculateMovePoints();
    }

    public int reduceMovePoints(int minus) {
        for (Unit u : units) {
            u.currMovePoints = Math.max(0, u.currMovePoints - minus);
        }
        return calculateMovePoints();
    }

    public void update(float tpf) {

        if (!onRoute) {
            return;
        }
        if (route == null || route.isEmpty()) {
            route = null;
            onRoute = false;

        } else {

            Tile t = route.peek();

            if (currMovePoints < map.getTileCosts(t)) {
                onRoute = false;
                return;
            }

            Vector3f checkpoint = map.getGLTileCenter(t);

            // next checkpoint reached
            if (checkpoint.distance(locationGL) < 0.05f) {
                route.pop();
                posX = t.x;
                posZ = t.z;
                locationGL = map.getGLTileCenter(t);
                model.setLocalTranslation(locationGL);

                reduceMovePoints(map.getTileCosts(t));

                if (map.selectedArmy == this) {
                    map.drawReachableArea(this);
                }

                if (route.isEmpty()) {
                    System.out.println("Goal reached");


                    Settlement s = map.getSettlement(t);
                    if (s != null) {
                        garrisonArmy(s);
                    }

                    return;
                }
            }
            t = route.peek();
            checkpoint = map.getGLTileCenter(t);
            Vector3f dir = checkpoint.subtract(locationGL).normalizeLocal();
            locationGL.addLocal(dir.multLocal(tpf));
            model.setLocalTranslation(locationGL);



        }

    }

    public void setRoute(Stack<Tile> r) {

//        System.err.println("Planned path:");
//        route = new Stack<Tile>();
//        while (!r.empty()) {
//            Tile t = r.pop();
//            route.push(t);
//        }
        r.pop();
        route = r;
        onRoute = true;


    }

    public void garrisonArmy(Settlement s) {

        for (Unit u : units) {
            units.add(u);
        }

        final Army a = this;

        map.scene.getControl(UpdateControl.class).enqueue(new Callable() {

            @Override
            public Object call() throws Exception {
                map.removeArmy(a);
                return null;
            }
        });
    }
}
