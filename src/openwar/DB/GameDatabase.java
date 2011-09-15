package openwar.DB;

import com.jme3.texture.Texture;
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

    public GameDatabase() {
        genUnits = new HashMap<String, GenericUnit>();
        genFactions = new HashMap<String, GenericFaction>();
        genBuildings = new HashMap<String, GenericBuilding>();
        map = new openwar.DB.Map();
    }
}
