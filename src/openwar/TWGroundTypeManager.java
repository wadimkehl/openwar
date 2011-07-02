/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package openwar;

/**
 *
 * @author kehl
 */
public class TWGroundTypeManager extends GroundTypeManager{

    public TWGroundTypeManager() {
    }

 
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
