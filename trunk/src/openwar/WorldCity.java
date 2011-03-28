/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package openwar;

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
    String region;
    boolean hasHarbor;
    Spatial harbor;
    Spatial model;
    ArrayList<ArmyUnit> units;
    
    
    public WorldCity(int x, int z, int player, String name)
    {
        this.posX = x;
        this.posZ = z;
        playerNumber = player;
        this.name = name;
        
        units = new ArrayList<ArmyUnit>();
        
    }
    
    public void update(float tpf)
    {
        
    }

}
