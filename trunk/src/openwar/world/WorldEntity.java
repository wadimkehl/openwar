/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package openwar.world;

import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import java.util.ArrayList;
import openwar.DB.Unit;

/**
 *
 * @author kehl
 */
public abstract class WorldEntity {

    public int posX, posZ;
    public String owner;
    public Spatial model;
    public Spatial banner;
    public Node node;
    public WorldMap map;
    public ArrayList<Unit> units;

    public WorldEntity() {
        units = new ArrayList<Unit>();
        node = new Node();
    }

    public void createData(WorldMap m)
    {
        map.worldTiles[posX][posZ].entity = this;
    }

    public abstract void update(float tpf);
    
    public int calculateMovePoints() {

        int points = 10000;
        for (Unit u : units) {
            points = Math.min(u.currMovePoints, points);
        }
        return points;

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

    
    public void mergeUnitsTo(WorldEntity to, ArrayList<Unit> split)
    {
         if (split != null) {
            for (Unit u : split) {
                units.remove(u);
                to.units.add(u);
            }
        }
    }
    
    public void mergeUnitsFrom(WorldEntity from, ArrayList<Unit> split)
    {
         if (split != null) {
            for (Unit u : split) {
                units.add(u);
                from.units.remove(u);
            }
        }
    }
}
