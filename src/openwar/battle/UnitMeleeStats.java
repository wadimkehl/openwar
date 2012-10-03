/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package openwar.battle;

import java.util.Map;

/**
 *
 * @author kehl
 */
public class UnitMeleeStats {
    

    
    boolean canShiltron;
    
    int damage, assault, shield, armor;
    
    float distance=1.2f,sqDist;
    float timeToStance=2.0f;
    float timeToHit=2.0f;
    float timeBetweenHits;
    
    public UnitMeleeStats(Map<String,String> stats)
    {
        
        for(String s : stats.keySet())
        {
            
            String val = stats.get(s);
            
            if("damage".equals(s)) damage = Integer.parseInt(val);
            if("assault".equals(s)) assault = Integer.parseInt(val);
            if("shield".equals(s)) shield = Integer.parseInt(val);
            if("armor".equals(s)) armor = Integer.parseInt(val);
            
            if("distance".equals(s)) distance = Float.parseFloat(val);
            if("timetostance".equals(s)) timeToStance = Float.parseFloat(val);
            if("timetohit".equals(s)) timeToHit = Float.parseFloat(val);
            if("timebetweenhits".equals(s)) timeBetweenHits = Float.parseFloat(val);

            sqDist = distance*distance;
        }
    }
    
}
