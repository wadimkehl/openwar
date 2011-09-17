/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package openwar.world;

import openwar.Main;

/**
 *
 * @author kehl
 */
 // Holds ground type information
    public class WorldTile extends Tile {

        public int groundType;
        public int cost;
        public String region;
        public String climate;

        public WorldTile(int x, int z, int type, int co, String r, String c) {
            super(x, z);
            region = r;
            groundType = type;
            cost = co;
            climate = c;

        }

        @Override
        public String toString() {
            return super.toString()
                    + " Type: " + Main.DB.map.tiles.get(groundType).name
                    + "     Region: " + Main.DB.hashedRegions.get(region).name
                    + "     Climate: " + climate;
        }
    };