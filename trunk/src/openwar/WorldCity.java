/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package openwar;

import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue.ShadowMode;
import com.jme3.scene.Spatial;
import java.util.ArrayList;

/**
 *
 * @author kehl
 */
public class WorldCity {

    int posX, posZ;
    int playerNumber;
    String name;
    WorldRegion region;
    Spatial model;
    WorldMap map;
    int civLevel = 0;
    int population = 0;
    ArrayList<ArmyUnit> units;

    public WorldCity(WorldRegion r, String n, WorldMap m) {
        
        region = r;
        name = n;
        map = m;
        units = new ArrayList<ArmyUnit>();
    }

    public WorldCity(int x, int z, int player, Spatial m, String name, WorldMap map) {
        this.posX = x;
        this.posZ = z;
        playerNumber = player;
        this.name = name;
        this.map = map;

        units = new ArrayList<ArmyUnit>();

        model = m;
        model.setShadowMode(ShadowMode.CastAndReceive);
        model.setLocalScale(0.5f);


        Vector3f vec = map.getGLTileCenter(x, z);
        model.setLocalTranslation(vec);


    }

    public void update(float tpf) {
    }

    public void garrisonArmy(WorldArmy a) {
        for (ArmyUnit u : a.units) {
            units.add(u);
        }

        map.armyToDelete = a;

    }
}
