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
    
    float factor0, factor1, offset;

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
     offset = offs;
    }


    @Override
    public float calculateHeight(float r, float g, float b) {

        
        return Math.max(0f, (r + g + b) * factor0 + r*factor1) + offset;
       
    }
}
