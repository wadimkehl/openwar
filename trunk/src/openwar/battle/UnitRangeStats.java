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
public class UnitRangeStats {
    

    boolean fireAtWill;
    
    int damage, maxAmmo, currAmmo, shotsPerVolley;
    
    float distance, sqDist;
    
    float timeToSetUp;
    float timeToAim;
    float timeBetweenShots;
    float timeToUnAim;
    float timeToReload;
    float timeToSetDown;
    
    Projectile proj;
    
    public UnitRangeStats(Map<String,String> stats)   
    {
         for(String s : stats.keySet())
        {
            
            String val = stats.get(s);
            
            if("damage".equals(s)) damage = Integer.parseInt(val);
            if("maxammo".equals(s)) maxAmmo = Integer.parseInt(val);
            if("shotspervolley".equals(s)) shotsPerVolley = Integer.parseInt(val);
            if("distance".equals(s)) distance = Float.parseFloat(val);

           

            
        }
                     sqDist = distance*distance;

         currAmmo=maxAmmo;
    }
    
    
}
