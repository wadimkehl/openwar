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
    public int cost, modifier;
    public String region = "";
    public String climate = "";
    public WorldEntity entity=null;

    public WorldTile(int x, int z, int type, int co, String r, String c) {
        super(x, z);
        region = r;
        groundType = type;
        cost = co;
        climate = c;
        modifier = 0;
    }

    public String shortInfo() {
        String owner = Main.DB.regions.get(region).owner;
        if (!"".equals(owner)) {
            owner = "     Owner: " + Main.DB.genFactions.get(Main.DB.regions.get(region).owner).name;
        }
        return super.toString()
                + "     Region: " + Main.DB.regions.get(region).name + owner
                + "     Climate: " + Main.DB.climates.get(climate).name;



    }

    public String MinimapInfo() {
        String owner = Main.DB.regions.get(region).owner;
        if (!"".equals(owner)) {
            owner = "\nOwner: " + Main.DB.genFactions.get(Main.DB.regions.get(region).owner).name;
        }
        return "Region: " + Main.DB.regions.get(region).name
                + owner;

    }

    @Override
    public String toString() {
        String owner = Main.DB.regions.get(region).owner;
        if (!"".equals(owner)) {
            owner = "     Owner: " + Main.DB.genFactions.get(Main.DB.regions.get(region).owner).name;
        }
        return super.toString()
                + "     Type: " + Main.DB.genTiles.get(groundType).name
                + "     Region: " + Main.DB.regions.get(region).name
                + "     Climate: " + Main.DB.climates.get(climate).name
                + owner;
    }
};