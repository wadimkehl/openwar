/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package openwar.world;

/**
 *
 * @author kehl
 */
 public class Tile {

        int x, z;

        public Tile(int x, int z) {
            this.x = x;
            this.z = z;
        }

        @Override
        public String toString() {
            return "(" + x + "," + z + ")";
        }
    };
