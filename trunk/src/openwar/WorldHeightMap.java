/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package openwar;

import com.jme3.terrain.heightmap.ImageBasedHeightMap;
import java.awt.Image;
import java.awt.image.BufferedImage;

/**
 *
 * @author kehl
 */
public class WorldHeightMap extends ImageBasedHeightMap {

    public WorldHeightMap(Image colorImage) {
        super(colorImage, 1.0f);
    }

    public WorldHeightMap(Image colorImage, float dampen) {
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
            for (int w = imageWidth - 1; w >= 0; w--) {
                int baseIndex = (h * imageWidth + w) * 3;
                float r = data[baseIndex] & 0xff;
                float g = data[baseIndex + 1] & 0xff;
                float b = data[baseIndex + 2] & 0xff;
                heightData[index++] = calculateHeight(r, g, b);


            }
        }




        return true;
    }

    public float calculateHeight(float r, float g, float b) {


        float grayscale = ((r + g + b) * dampen / 3f) - b * 0.05f;

        if (b > 200f || grayscale < -0.2f) {
            return -.25f;
        } else {
            return grayscale / 2.5f;
        }

    }
}
