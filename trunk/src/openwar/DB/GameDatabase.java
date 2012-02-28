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
    public boolean flipOrderClimates, flipOrderTypes, flipOrderRegions;
    public HashMap<String, Spatial> decorations;
    public ArrayList<WorldDecoration> worldDecorations;
    public HashMap<String, AudioNode> musicNodes;
    public HashMap<String, AudioNode> soundNodes;
    public HashMap<String,Model >  models;
    public ArrayList<Texture> tileTextures;
    public ArrayList<Float> tileTextures_scales;
    public int tilesTexturesCount;
    public HashMap<String, Culture> cultures;
    public HashMap<String, Region> regions;
    public HashMap<String, Settlement> settlements; // String is refname of region!
    public HashMap<String, Climate> climates;
    public HashMap<String, Faction> factions;
    public String playerFaction;
    public int currentRound = 0;
    public String currentTurn;
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

        models = new HashMap<String, Model>();

        
        decorations = new HashMap<String, Spatial>();
        worldDecorations = new ArrayList<WorldDecoration>();


        tileTextures = new ArrayList<Texture>();
        tileTextures_scales = new ArrayList<Float>();

        factions = new HashMap<String, Faction>();
        cultures = new HashMap<String, Culture>();
        regions = new HashMap<String, Region>();
        climates = new HashMap<String, Climate>();
        settlements = new HashMap<String, Settlement>();


    }
}
