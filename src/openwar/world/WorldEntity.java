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

    public abstract void createData(WorldMap m);

    public abstract void update(float tpf);
    
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
