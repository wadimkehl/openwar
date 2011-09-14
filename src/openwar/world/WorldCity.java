/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package openwar.world;

import com.jme3.scene.Spatial;
import com.jme3.scene.control.UpdateControl;
import java.util.ArrayList;
import java.util.concurrent.Callable;

/**
 *
 * @author kehl
 */
public class WorldCity {

    int posX, posZ;
    String name;
    WorldRegion region;
    Spatial model;
    WorldMap map;
    int level = 0;
    int population = 0;
    ArrayList<ArmyUnit> units;
    ArrayList<WorldBuilding> buildings;

    public WorldCity(WorldRegion r, String n, WorldMap m) {
        
        region = r;
        name = n;
        map = m;
        units = new ArrayList<ArmyUnit>();
        buildings = new ArrayList<WorldBuilding>();
    }

    @Override
    public String toString()
    {
        return name;
    }

    public void update(float tpf) {
    }

    public void garrisonArmy(final WorldArmy a) {
        
        for (ArmyUnit u : a.units) {
            units.add(u);
        }
        

        map.scene.getControl(UpdateControl.class).enqueue(new Callable() {
         public Object call() throws Exception {
             map.removeArmy(a);             
             return null;
         }
     });

    }
}
