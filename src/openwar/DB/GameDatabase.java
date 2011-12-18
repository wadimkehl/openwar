package openwar.DB;

import com.jme3.audio.AudioNode;
import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;
import com.jme3.texture.Texture;
import java.util.ArrayList;
import java.util.HashMap;
import openwar.world.WorldDecoration;

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
    public HashMap<Integer, GenericTile> genTiles;
    public Texture heightmapTex, regionsTex, climatesTex, typesTex;
    public HashMap<String, Spatial> decorations;
    public ArrayList<WorldDecoration> worldDecorations;
    public HashMap<String, AudioNode> musicNodes;
    public HashMap<String, AudioNode> soundNodes;
    public ArrayList<Texture> tileTextures;
    public ArrayList<Float> tileTextures_scales;
    public int tilesTexturesCount;
    public ArrayList<Region> regions;
    public HashMap<String, Region> hashedRegions;
    public ArrayList<Settlement> settlements;
    public HashMap<String, Settlement> hashedSettlements; // String is refname of region!
    public ArrayList<Climate> climates;
    public HashMap<String, Climate> hashedClimates;
    public ArrayList<Faction> factions;
    public HashMap<String, Faction> hashedFactions;
    public String playerFaction = "roman";
    public int currentRound = 0;
    public String currentTurn = "roman";
    public Vector3f heightmapParams, sun_color, sun_direction, water_color;
    public boolean hasWater;
    public float waterHeight;

    public GameDatabase() {

        genUnits = new HashMap<String, GenericUnit>();
        genFactions = new HashMap<String, GenericFaction>();
        genBuildings = new HashMap<String, GenericBuilding>();
        genTiles = new HashMap<Integer, GenericTile>();

        musicNodes = new HashMap<String, AudioNode>();
        soundNodes = new HashMap<String, AudioNode>();

        decorations = new HashMap<String, Spatial>();
        worldDecorations = new ArrayList<WorldDecoration>();


        tileTextures = new ArrayList<Texture>();
        tileTextures_scales = new ArrayList<Float>();

        regions = new ArrayList<Region>();
        settlements = new ArrayList<Settlement>();
        climates = new ArrayList<Climate>();
        factions = new ArrayList<Faction>();

        hashedFactions = new HashMap<String, Faction>();
        hashedRegions = new HashMap<String, Region>();
        hashedClimates = new HashMap<String, Climate>();
        hashedSettlements = new HashMap<String, Settlement>();


    }
}
