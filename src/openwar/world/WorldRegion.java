/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package openwar.world;

import com.jme3.math.ColorRGBA;

/**
 *
 * @author kehl
 */
public class WorldRegion {
    
    ColorRGBA regionColor; // The color for this region from regions.tga
    String name;
    WorldFaction owner;
    boolean hasHarbor;
    WorldCity city;
    WorldMap map;
    
    public WorldRegion(String n, String c, int r, int g, int b, WorldMap m)
    {
        name = n;
        regionColor = new ColorRGBA(r,g,b,0);
        map = m;
        
        if (c!= null)
        city = new WorldCity(this, c, m);
    }
    
    public void update(float tpf) {
        
        if (city != null)
            city.update(tpf);
    }
    
}
