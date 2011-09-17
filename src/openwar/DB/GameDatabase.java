package openwar.DB;

import com.jme3.texture.Texture;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author kehl
 */
public class GameDatabase {

    public HashMap<String, GenericUnit> genUnits;
    public HashMap<String, GenericFaction> genFactions;
    public HashMap<String, GenericBuilding> genBuildings;
    public openwar.DB.Map map;
    public ArrayList<Region> regions;
    public HashMap<String, Region> hashedRegions;
    public ArrayList<Settlement> settlements;
    public HashMap<String, Settlement> hashedSettlements; // String is refname of region!
    public ArrayList<Climate> climates;
    public HashMap<String, Climate> hashedClimates;

    public GameDatabase() {
        genUnits = new HashMap<String, GenericUnit>();
        genFactions = new HashMap<String, GenericFaction>();
        genBuildings = new HashMap<String, GenericBuilding>();
        map = new openwar.DB.Map();

        regions = new ArrayList<Region>();
        settlements = new ArrayList<Settlement>();
        climates = new ArrayList<Climate>();

        hashedRegions = new HashMap<String, Region>();
        hashedClimates = new HashMap<String, Climate>();
        hashedSettlements = new HashMap<String, Settlement>();


    }
}
