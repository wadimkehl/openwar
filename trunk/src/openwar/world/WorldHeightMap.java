/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package openwar.world;

import com.jme3.terrain.heightmap.ImageBasedHeightMap;
import com.jme3.texture.Texture;
import java.awt.Image;
import jme3tools.converters.ImageToAwt;

/**
 *
 * @author kehl
 * 
 * Tries to imitate something like a total war heightmap creation
 */
public class WorldHeightMap extends ImageBasedHeightMap {
    
    float factor0, factor1, cutoff;

    public WorldHeightMap(Image colorImage) {
        super(colorImage, 1.0f);
    }

    public WorldHeightMap(Image colorImage, float dampen) {
        super(colorImage, dampen);
        this.colorImage = colorImage;
        this.dampen = dampen;
    }
    
    
    public WorldHeightMap(Texture image, float f0, float f1, float offs)
    {
     this(ImageToAwt.convert(image.getImage(), false, false, 0)); 
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
