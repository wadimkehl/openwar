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

    public HashMap<String, Unit> units;
    public HashMap<String, Faction> factions;
    public HashMap<String, GenericBuilding> buildings;
    public openwar.DB.Map map;

    public GameDatabase() {
        units = new HashMap<String, Unit>();
        factions = new HashMap<String, Faction>();
        buildings = new HashMap<String, GenericBuilding>();
        map = new openwar.DB.Map();
    }
}
