/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package openwar;

import com.jme3.math.ColorRGBA;

/**
 *
 * @author kehl
 */
public class WorldRegion {
    
    ColorRGBA regionColor; // The color for this region from regions.tga
    String Name;
    int playerOwner;
    boolean hasHarbor;
    WorldCity city;
    
    public WorldRegion()
    {
        
    }
    
}
