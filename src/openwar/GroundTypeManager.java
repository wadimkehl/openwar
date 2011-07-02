/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package openwar;

import com.jme3.texture.Image;
import java.nio.ByteBuffer;

/**
 *
 * @author kehl
 */
public class GroundTypeManager {

    // Recieves an image and returns the three key textures in a RGBA byte buffer
    static public boolean CreateKeyTextures(Image im, ByteBuffer buf0, ByteBuffer buf1, ByteBuffer buf2) {
        int w = im.getWidth();
        int h = im.getHeight();

        byte[] data0 = new byte[w * h * 4];
        byte[] data1 = new byte[w * h * 4];
        byte[] data2 = new byte[w * h * 4];


        ByteBuffer buffer = im.getData(0);
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int base = (y * w + x) * 4;
                int r, g, b;

                r = buffer.get() & 0xff;
                g = buffer.get() & 0xff;
                b = buffer.get() & 0xff;


                if (!computeKeys(r, g, b, base, data0, data1, data2)) {
                    System.err.println(
                            "Unknown tile at (" + x + "," + y + "): " + r + " " + g + " " + b);
                    return false;
                }
            }

        }


        for (int i = 0; i < w * h * 4; i++) {
            buf0.put(data0[i]);
            buf1.put(data1[i]);
            buf2.put(data2[i]);

        }

        return true;


    }

    // Fills the key textures at a specific base address with the right blending value
    static public boolean computeKeys(int r, int g, int b, int base, byte[] data0, byte[] data1, byte[] data2) {

        int groundtype = RGBtoVisualGroundType(r, g, b);
        switch (groundtype) {
            case (0):
                data0[base + 0] = (byte) 255;
                break;
            case (1):
                data0[base + 1] = (byte) 255;
                break;
            case (2):
                data0[base + 2] = (byte) 255;
                break;
            case (3):
                data0[base + 3] = (byte) 255;
                break;
            case (4):
                data1[base + 0] = (byte) 255;
                break;
            case (5):
                data1[base + 1] = (byte) 255;
                break;
            case (6):
                data1[base + 2] = (byte) 255;
                break;
            case (7):
                data1[base + 3] = (byte) 255;
                break;
            case (8):
                data2[base + 0] = (byte) 255;
                break;
            case (9):
                data2[base + 1] = (byte) 255;
                break;
            case (10):
                data2[base + 2] = (byte) 255;
                break;
            case (11):
                data2[base + 3] = (byte) 255;
                break;
            default:
                return false;
                

        }
        return true;
    }

    // Returns for a given ground type pixel its real ground tile number (0-16+)
    static public int RGBtoGroundType(int r, int g, int b) {


        if (r == 255 && g == 255 && b == 255) {
            return 0; //beach
        }
        else if ((r == 101 && g == 124 && b == 0) 
            || (r == 0 && g == 128 && b == 128) 
            || (r == 96 && g == 160 && b == 64)) {
            return 4; //all fertile to grass
            
        } 
        else if (r == 0 && g == 64 && b == 0) {
            return 8; //dense forest
        } 
        
        
        else if (r == 0 && g == 128 && b == 0) {
            return 6; // sparse forest to grass rough
        }
        else if (r == 128 && g == 128 && b == 64) {
            return 7; // grass hilly
        } 
        
        else if (r == 64 && g == 64 && b == 64) {
            return 16; // impassable
        } 
        
        else if (r == 196 && g == 128 && b == 128) {
            return 11;
        } // mountains high
        
        else if (r == 98 && g == 65 && b == 65) {
            return 7; // grass hilly
        } 
        
        else if (r == 64 && g == 0 && b == 0)
                return 15;
        else if (r == 128 && g == 0 && b == 0)
            return 14;
        else if( (r == 196 && g == 0 && b == 0)) {
            return 13;
        } 
        
        else if (r == 0 && g == 255 && b == 128) {
            return 10;
            
        } else if (r == 0 && g == 0 && b == 0) {
            return 8;
        }

        return -1;

    }

    // Returns for a given ground type pixel its VISUAL!!! ground tile number (0-11)
    static public int RGBtoVisualGroundType(int r, int g, int b) {

        // First get the real ground type
        int type = RGBtoGroundType(r, g, b);

        // And now discern if we have tile that shares its visual tile      
        switch (type) {

            // beach and all water tiles
            case 12:
            case 13:
            case 14:
            case 15:
                return 0;

            // impassable
            case 16:
                return 11;

            default:
                return type;
        }


    }


    // Returns for a given ground type its march costs
    static public int getGroundTypeCost(int type) {

        switch (type) {
            case (0):
                return 2;
            case (1):
                return 2;
            case (2):
                return 2;
            case (3):
                return 3;
            case (4):
                return 1;
            case (5):
                return 1;
            case (6):
                return 2;
            case (7):
                return 3;
            case (8):
                return 4;
            case (9):
                return 4;
            case (10):
                return 5;
            case (11):
                return 10;
            case (12):
                return 2;
            case (13):
                return 1;
            case (14):
                return 3;
            case (15):
                return 8;
            case (16):
                return 1000;
            default:
                return 1000;

        }

    }
    
        
    // Tells whether an army can walk on a tile type
    static public boolean isWalkable(int type)
    {
        if (type < 13) return true;
        return false;
    }
    
    // Tells whether a boat can sail on this type
    static public boolean isSailable(int type)
    {
        if ((type > 11 && type <16)) return true;
        return false;
    }

    // Returns for a given ground type its string name
    static public String getGroundTypeString(int type) {

        switch (type) {
            case (0):
                return "desert";
            case (1):
                return "desert fertile";
            case (2):
                return "desert rough";
            case (3):
                return "desert hilly";
            case (4):
                return "grass";
            case (5):
                return "grass fertile";
            case (6):
                return "grass rough";
            case (7):
                return "grass hilly";
            case (8):
                return "grass forest";
            case (9):
                return "snow";
            case (10):
                return "swamp";
            case (11):
                return "mountains";
            case (12):
                return "beach";
            case (13):
                return "shallow water";
            case (14):
                return "deep water";
            case (15):
                return "ocean";
            case (16):
                return "impassable";

        }

        return "N/A???";
    }
}
