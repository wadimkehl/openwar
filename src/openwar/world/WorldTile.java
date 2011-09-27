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
    public String region = "";
    public String climate = "";

    public WorldTile(int x, int z, int type, int co, String r, String c) {
        super(x, z);
        region = r;
        groundType = type;
        cost = co;
        climate = c;

    }

    public String shortInfo() {
        String owner = Main.DB.hashedRegions.get(region).owner;
        if (!"".equals(owner)) {
            owner = "     Owner: " + Main.DB.genFactions.get(Main.DB.hashedRegions.get(region).owner).name;
        }
        return super.toString() 
                + "     Region: " + Main.DB.hashedRegions.get(region).name + owner
                + "     Climate: " + Main.DB.hashedClimates.get(climate).name;



    }

    @Override
    public String toString() {
        String owner = Main.DB.hashedRegions.get(region).owner;
        if (!"".equals(owner)) {
            owner = "     Owner: " + Main.DB.genFactions.get(Main.DB.hashedRegions.get(region).owner).name;
        }
        return super.toString()
                + "     Type: " + Main.DB.genTiles.get(groundType).name
                + "     Region: " + Main.DB.hashedRegions.get(region).name
                + "     Climate: " + Main.DB.hashedClimates.get(climate).name
                + owner;
    }
};