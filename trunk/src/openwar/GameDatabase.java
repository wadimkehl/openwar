package openwar;


import com.jme3.texture.Texture;
import java.util.Map;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author kehl
 */
public class GameDatabase {
    
    
    public class Unit
    {
        String refName;
        
        int maxCount;
        int maxMovePoints;

        Map<String,Integer> stats;
        Texture unitCard;
        
        
    }
    
    public class Faction
    {
        
    }
    
    public class Building
    {
        
    }
    
    
    public Map<String, Unit> units;
    public Map<String, Faction> factions;
    public Map<String, Building> buildings;
    
}
