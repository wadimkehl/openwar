/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package openwar.world;

import com.jme3.terrain.heightmap.ImageBasedHeightMap;
import com.jme3.texture.Image;

/**
 *
 * @author kehl
 * 
 * Tries to imitate something like a total war heightmap creation
 */
public class WorldHeightMap extends ImageBasedHeightMap {
    
    float factor0, factor1, cutoff;

    public WorldHeightMap(Image i) {
        super(i, 1.0f);
    }
    
    
    public WorldHeightMap(Image image, float f0, float f1, float offs)
    {
     this(image); 
     factor0 = f0;
     factor1 = f1;
     cutoff = offs;
    }


    @Override
    public float calculateHeight(float r, float g, float b) {

        float value = (r + g + b) * factor0 + r*factor1;
        return value <= cutoff ? -0.2f : Math.max(0f,value);
       
    }
}
