/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package openwar;

import com.jme3.math.FastMath;
import com.jme3.terrain.heightmap.ImageBasedHeightMap;
import java.awt.Image;
import java.awt.image.BufferedImage;

/**
 *
 * @author kehl
 * 
 * Tries to imitate something like a total war heightmap creation
 */
public class TWWorldHeightMap extends ImageBasedHeightMap {

    public TWWorldHeightMap(Image colorImage) {
        super(colorImage, 1.0f);
    }

    public TWWorldHeightMap(Image colorImage, float dampen) {
        super(colorImage, dampen);
        this.colorImage = colorImage;
        this.dampen = dampen;
    }

    @Override
    public boolean load(boolean flipX, boolean flipY) {

        BufferedImage colorBufferedImage = ImageConverter.toBufferedImage(
                colorImage, BufferedImage.TYPE_3BYTE_BGR);

        int imageWidth = colorBufferedImage.getWidth();
        int imageHeight = colorBufferedImage.getHeight();

        if (imageWidth != imageHeight) {
            throw new RuntimeException("imageWidth: " + imageWidth
                    + " != imageHeight: " + imageHeight);
        }

        size = imageWidth;

        byte data[] = (byte[]) colorBufferedImage.getRaster().getDataElements(
                0, 0, imageWidth, imageHeight, null);


        heightData = new float[(imageWidth * imageHeight)];

        int index = 0;
        for (int h = 0; h < imageHeight; h++) {
            for (int w = 0; w < imageWidth; w++) {
                int baseIndex = (h * imageWidth + w) * 3;
                float r = data[baseIndex] & 0xff;
                float g = data[baseIndex + 1] & 0xff;
                float b = data[baseIndex + 2] & 0xff;
                heightData[index++] = calculateHeight(r, g, b);


            }
        }

        //TODO: fix the mesh coordinates so that water tiles wont come onto land
        


        return true;
    }

    public float calculateHeight(float r, float g, float b) {

        
        return Math.max(0f, (r + g + b) * 0.000001f + r*0.022f) - 0.25f;
       
    }
}
