/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package openwar;

import com.jme3.texture.Image;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture2D;
import java.nio.ByteBuffer;

/**
 *
 * @author kehl
 */
public class GroundTypeManager {

    public GroundTypeManager() {
    }

    static public boolean CreateKeyTextures(Image im, ByteBuffer buf0, ByteBuffer buf1) {

        int w = im.getWidth();
        int h = im.getHeight();

        byte[] data0 = new byte[w * h * 4];
        byte[] data1 = new byte[w * h * 4];

        ByteBuffer buffer = im.getData(0);


        // jME loads the image x-flipped, so start at the right

        for (int y = h-1; y >= 0; y--) {
            for (int x = 0; x < w; x++) {
                int base = (y * w + x) * 4;
                int r, g, b;

                r = buffer.get() & 0xff;
                g = buffer.get() & 0xff;
                b = buffer.get() & 0xff;


                if (!computeKeys(r, g, b, base, data0, data1)) {
                    // System.err.print("Unknown tile at (" + ((Object) x).toString() + "," + ((Object) y).toString() + "): ");
                    // System.err.println(((Object) r).toString() + " " + ((Object) g).toString() + " " + ((Object) b).toString());
                    //return false;
                }
            }

        }


        for (int i = 0; i < w * h * 4; i++) {
            buf0.put(data0[i]);
            buf1.put(data1[i]);
        }

        return true;


    }

    // Ground type pixel color to tile type conversion (i.e. RGBA key texture values)
    static public boolean computeKeys(int r, int g, int b, int base, byte[] data0, byte[] data1) {

        int groundtype = RGBtoGroundType(r, g, b);
        switch (groundtype) {
            case (0):
                data0[base + 0] = (byte) 255;
                data1[base + 0] = (byte) 255;
                break;
            case (1):
                data0[base + 1] = (byte) 255;
                data1[base + 0] = (byte) 255;
                break;
            case (2):
                data0[base + 2] = (byte) 255;
                data1[base + 0] = (byte) 255;
                break;
            case (3):
                data0[base + 3] = (byte) 255;
                data1[base + 0] = (byte) 255;
                break;
            case (4):
                data0[base + 0] = (byte) 255;
                data1[base + 1] = (byte) 255;
                break;
            case (5):
                data0[base + 1] = (byte) 255;
                data1[base + 1] = (byte) 255;
                break;
            case (6):
                data0[base + 2] = (byte) 255;
                data1[base + 1] = (byte) 255;
                break;
            case (7):
                data0[base + 3] = (byte) 255;
                data1[base + 1] = (byte) 255;
                break;
            case (8):
                data0[base + 0] = (byte) 255;
                data1[base + 2] = (byte) 255;
                break;
            case (9):
                data0[base + 1] = (byte) 255;
                data1[base + 2] = (byte) 255;
                break;
            case (10):
                data0[base + 2] = (byte) 255;
                data1[base + 2] = (byte) 255;
                break;
            case (11):
                data0[base + 3] = (byte) 255;
                data1[base + 2] = (byte) 255;
                break;
            default:
                return false;

        }

        return true;
    }

    // Returns for a given ground type pixel its ground tile number (0-11)
    static public int RGBtoGroundType(int r, int g, int b) {


        if (r == 255 && g == 255 && b == 255) {
            return 0;
        } // t1
        else if (r == 101 && g == 124 && b == 0) {
            return 1;
        } // t2
        else if (r == 0 && g == 128 && b == 128) {
            return 2;
        } // t3
        else if (r == 96 && g == 160 && b == 64) {
            return 3;
        } // t4
        else if (r == 0 && g == 64 && b == 0) {
            return 4;
        } // t5
        else if (r == 0 && g == 128 && b == 0) {
            return 5;
        } // t6
        else if (r == 128 && g == 128 && b == 64) {
            return 6;
        } // t7
        else if (r == 64 && g == 64 && b == 64) {
            return 7;
        } // t8
        else if (r == 196 && g == 128 && b == 128) {
            return 8;
        } // t9
        else if (r == 98 && g == 65 && b == 65) {
            return 9;
        } // t10
        else if ((r == 64 && g == 0 && b == 0)
                || (r == 128 && g == 0 && b == 0)
                || (r == 196 && g == 0 && b == 0)) {
            return 10;
        } // t11
        else if (r == 0 && g == 255 && b == 128) {
            return 11;
        } else if (r == 0 && g == 0 && b == 0) {
            return 4;
        }

        return 3;
        //return -1;

    }

    // Returns for a given ground type its march costs
    static public int getGroundTypeCost(int type) {

        switch (type) {
            case (0):
                return 1000;
            case (1):
                return 1;
            case (2):
                return 1;
            case (3):
                return 1;
            case (4):
                return 1;
            case (5):
                return 1000;
            case (6):
                return 2;
            case (7):
                return 2;
            case (8):
                return 3;
            case (9):
                return 1000;
            case (10):
                return 1000;
            case (11):
                return 3;

        }

        return 1000;
    }

    // Returns for a given ground type its string name
    static public String getGroundTypeString(int type) {

        switch (type) {
            case (0):
                return "0";
            case (1):
                return "1";
            case (2):
                return "2";
            case (3):
                return "3";
            case (4):
                return "4";
            case (5):
                return "5";
            case (6):
                return "6";
            case (7):
                return "7";
            case (8):
                return "8";
            case (9):
                return "9";
            case (10):
                return "10";
            case (11):
                return "11";

        }

        return "N/A";
    }
}
