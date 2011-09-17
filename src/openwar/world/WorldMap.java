/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package openwar.world;

import com.jme3.asset.AssetManager;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.light.DirectionalLight;

import com.jme3.material.Material;
import com.jme3.material.RenderState.FaceCullMode;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.post.FilterPostProcessor;
import com.jme3.post.filters.BloomFilter;
import com.jme3.renderer.queue.RenderQueue.ShadowMode;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.UpdateControl;
import com.jme3.terrain.geomipmap.TerrainQuad;
import com.jme3.texture.Image;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture2D;
import com.jme3.water.WaterFilter;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;
import openwar.DB.Climate;
import openwar.DB.GenericTile;
import openwar.DB.Region;
import openwar.DB.Settlement;
import openwar.DB.Unit;

import openwar.Main;

/**
 *
 * @author kehl
 */
public class WorldMap {

    // Basic (x,z)-Tile
    // Holds information about selected (highlighted) tiles
    public class SelectionTile extends Tile {

        public float intensity;
        public Vector3f color;

        public SelectionTile(int x, int z, float i) {
            super(x, z);
            intensity = i;
        }

        public SelectionTile(int x, int z) {
            super(x, z);
            intensity = 1f;
        }

        public SelectionTile(int x, int z, Vector3f col) {
            super(x, z);
            color = col;
        }
    };
    Main app;
    public int width, height;
    public TerrainQuad terrain;
    BulletAppState bulletState;
    public Node scene = new Node("worldmap"), rootScene;
    public Material matTerrain, matTerrainDebug;
    AssetManager assetManager;
    WorldHeightMap heightMap;
    Texture key0Image, key1Image, key2Image, gridImage;
    public WorldTile[][] worldTiles;
    ArrayList<SelectionTile> selectedTiles = new ArrayList<SelectionTile>();
    boolean selectedTilesChanged = false;
    Geometry selectedTilesOverlay;
    Material matOverlay;
    ArrayList<Army> Armies = new ArrayList<Army>();
    Army selectedArmy;
    Settlement selectedSettlement;
    FilterPostProcessor fpp;
    PathFinder pathFinder = new PathFinder(this);
    private static final Logger logger = Logger.getLogger(WorldMap.class.getName());

    public WorldMap(Main app, Node scene) {
        this.app = app;
        this.bulletState = app.bulletState;
        this.assetManager = app.getAssetManager();
        this.rootScene = scene;
        heightMap = null;
        this.scene.addControl(new UpdateControl());


    }

    public void setWorldTile(int x, int z, int type) {
        worldTiles[x][z] = new WorldTile(x, z, type, getGroundTypeCost(type),
                worldTiles[x][z].region, worldTiles[x][z].climate);
    }

    public boolean createKeyTextures() {
        int size = width * height * 4;
        ByteBuffer buf0 = ByteBuffer.allocateDirect(size);
        ByteBuffer buf1 = ByteBuffer.allocateDirect(size);
        ByteBuffer buf2 = ByteBuffer.allocateDirect(size);
        byte[] data0 = new byte[size];
        byte[] data1 = new byte[size];
        byte[] data2 = new byte[size];
        for (int z = 0; z < height; z++) {
            for (int x = 0; x < width; x++) {
                int base = (z * width + x) * 4;
                if (!computeKeys(getVisualGroundType(worldTiles[x][height - 1 - z].groundType), base, data0, data1, data2)) {
                    logger.log(Level.SEVERE,
                            "Unknown tile at ({0},{1}): {2} ", new Object[]{x, height - 1 - z, worldTiles[x][height - 1 - z].groundType});
                    return false;
                }
            }
        }
        for (int i = 0; i < size; i++) {
            buf0.put(data0[i]);
            buf1.put(data1[i]);
            buf2.put(data2[i]);

        }
        key0Image = new Texture2D(new Image(Image.Format.RGBA8, width, height, buf0));
        key1Image = new Texture2D(new Image(Image.Format.RGBA8, width, height, buf1));
        key2Image = new Texture2D(new Image(Image.Format.RGBA8, width, height, buf2));
        matTerrain.setTexture("AlphaMap", key0Image);
        matTerrain.setTexture("AlphaMap_1", key1Image);
        matTerrain.setTexture("AlphaMap_2", key2Image);

        return true;
    }

    public void createGridTexture() {
        int grid_res = 32;
        ByteBuffer buf = ByteBuffer.allocateDirect(grid_res * grid_res * 4);
        for (int y = 0; y < grid_res; y++) {
            for (int x = 0; x < grid_res; x++) {
                if (x == 0 || y == 0 || x == grid_res - 1 || y == grid_res - 1) {
                    buf.putInt(Integer.parseInt("7F00007F", 16));
                } else {
                    buf.putInt(Integer.parseInt("000000", 16));

                }

            }
        }
        gridImage = new Texture2D(new Image(Image.Format.RGBA8, grid_res, grid_res, buf));
        gridImage.setWrap(Texture.WrapMode.Repeat);
        matTerrain.setTexture("GridMap", gridImage);
        matTerrain.setFloat("GridMap_scale", Math.min(width, height));
    }

    public boolean createTerrain() {

        // Create standard material with all textures and needed values
        matTerrain = new Material(assetManager, "materials/TerrainLighting.j3md");
        matTerrain.setTexture("DiffuseMap", Main.DB.map.tileTextures.get(0));
        Main.DB.map.tileTextures.get(0).setWrap(Texture.WrapMode.Repeat);
        matTerrain.setFloat("DiffuseMap_0_scale", Main.DB.map.tileTextures_scales.get(0));
        for (int i = 1; i < Main.DB.map.tilesTexturesCount; i++) {
            matTerrain.setTexture("DiffuseMap_" + i, Main.DB.map.tileTextures.get(i));
            matTerrain.setFloat("DiffuseMap_" + i + "_scale", Main.DB.map.tileTextures_scales.get(i));
            Main.DB.map.tileTextures.get(i).setWrap(Texture.WrapMode.Repeat);

        }


        if (!createKeyTextures()) {
            return false;
        }
        createGridTexture();



        // Create debug material to display ground types, regions, climates etc.
        matTerrainDebug = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");

        // Create mesh data with material and place its north-western edge to the origin
        heightMap = new WorldHeightMap(Main.DB.map.heightmapTex,
                Main.DB.map.terrain.heightmap.factor0,
                Main.DB.map.terrain.heightmap.factor1,
                Main.DB.map.terrain.heightmap.offset);

        heightMap.load(false, false);
        terrain = new TerrainQuad("terrain", 32, heightMap.getSize(), heightMap.getHeightMap());
        terrain.setMaterial(matTerrain);
        terrain.setLocalTranslation(width / 2f, 0f, height / 2f);

        return true;
    }

    public boolean createWorldTiles() {

        // Create worldTiles array and read according region and climate from images
        worldTiles = new WorldTile[width][height];
        ByteBuffer types = Main.DB.map.typesTex.getImage().getData(0);
        ByteBuffer regs = Main.DB.map.regionsTex.getImage().getData(0);
        ByteBuffer clis = Main.DB.map.climatesTex.getImage().getData(0);
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                int base = (j * width + i) * 3;
                int r, g, b, r1, g1, b1, r2, g2, b2;
                r = types.get(base + 0) & 0xff;
                g = types.get(base + 1) & 0xff;
                b = types.get(base + 2) & 0xff;
                r1 = regs.get(base + 0) & 0xff;
                g1 = regs.get(base + 1) & 0xff;
                b1 = regs.get(base + 2) & 0xff;
                r2 = clis.get(base + 0) & 0xff;
                g2 = clis.get(base + 1) & 0xff;
                b2 = clis.get(base + 2) & 0xff;

                // TODO: Find out why the groundtype image is bgr and the others aren't
                int type = RGBtoGroundType(b, g, r);
                Climate climate = getClimateByRGB(r2, g2, b2);
                Region region = getRegionByRGB(new Vector3f(r1, g1, b1));
                GenericTile tile = Main.DB.map.tiles.get(type);
                if (region != null && climate != null && tile != null) {
                    worldTiles[i][height - 1 - j] = new WorldTile(i, height - 1 - j, type, tile.cost, region.refName, climate.refName);
                } else {
                    logger.log(Level.SEVERE,"Error at ({0},{1}) ", new Object[]{i, height - 1 - j});                   
                    return false;
                }
            }
        }

        return true;

    }

    public boolean createEntities() {

        for (Region r : Main.DB.regions) {

            if (r.settlement == null) {
                continue;
            }

            Spatial m = Main.DB.genBuildings.get("city").levels.get(0).model.clone();
            m.setShadowMode(ShadowMode.CastAndReceive);
            Vector3f vec = getGLTileCenter(r.settlement.posX, r.settlement.posZ);
            m.setLocalTranslation(vec);
            r.settlement.model = m;
            scene.attachChild(m);


        }

        return true;

    }

    // Create the whole world map
    public boolean createWorldMap() {

        width = Main.DB.map.typesTex.getImage().getWidth();
        height = Main.DB.map.typesTex.getImage().getHeight();


        if (!createWorldTiles()) {
            System.out.println("Couldn't create world tiles...");
            return false;
        }
        if (!createTerrain()) {
            System.out.println("Couldn't create terrain...");
            return false;
        }
        if (!createEntities()) {
            System.out.println("Couldn't create entities...");
            return false;
        }

        DirectionalLight dlight = new DirectionalLight();
        dlight.setColor(new ColorRGBA(
                Main.DB.map.terrain.sun.color.x / 255f,
                Main.DB.map.terrain.sun.color.y / 255f,
                Main.DB.map.terrain.sun.color.z / 255f, 1));
        dlight.setDirection(Main.DB.map.terrain.sun.direction);
        scene.addLight(dlight);

        fpp = new FilterPostProcessor(assetManager);

        WaterFilter water = new WaterFilter(scene, new Vector3f(0.3f, -0.9f, 1f));
        water.setMaxAmplitude(0.2f);
        water.setWaterTransparency(15f);
        water.setWaterColor(new ColorRGBA(0.01f, 0.5f, 0.7f, 1f));
        water.setSpeed(0.1f);
        water.setFoamHardness(2f);
        water.setFoamExistence(new Vector3f(0.1f, 0.2f, 0.18f));
        fpp.addFilter(water);

        BloomFilter bloom = new BloomFilter();
        bloom.setExposurePower(4f);
        fpp.addFilter(bloom);

        app.getViewPort().addProcessor(fpp);


        selectedTilesOverlay = new Geometry("overlay");
        matOverlay = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        matOverlay.setColor("Color", ColorRGBA.Blue);
        selectedTilesOverlay.setMaterial(matOverlay);
        matOverlay.getAdditionalRenderState().setFaceCullMode(FaceCullMode.Off);

        scene.attachChild(terrain);
        //     scene.attachChild(selectedTilesOverlay);
        rootScene.attachChild(scene);

        terrain.addControl(new RigidBodyControl(0));
        bulletState.getPhysicsSpace().addAll(terrain);


        return true;

    }

    // Returns for a tile the real opengl center coordinates
    public Vector3f getGLTileCenter(int x, int z) {
        float newx = x + 0.5f;
        float newz = z + 0.5f;
        return new Vector3f(newx, heightMap.getInterpolatedHeight(newx, newz), newz);
    }

    // Return for a tile the opengl coordinates of the upper left corner
    public Vector3f getGLTileCorner(int x, int z) {
        return new Vector3f(x, heightMap.getInterpolatedHeight(x, z), z);
    }

    // Select a tile of the terrain (gets highlighting)
    public void selectTile(int x, int z, float i) {
        x = ensureInTerrainX(x);
        z = ensureInTerrainZ(z);
        selectedTiles.add(new SelectionTile(x, z, i));
        selectedTilesChanged = true;

    }

    public void selectTile(int x, int z) {
        selectTile(x, z, 1f);
    }

    // Process all the selected tiles into key 1 texture to make them visible
    private void showSelectedTiles() {
        selectedTilesChanged = false;
    }

    public void deselectAll() {
        deselectTiles();
        selectedArmy = null;
        selectedSettlement = null;
    }

    // Deselects the selected tiles (removes highlighting)
    public void deselectTiles() {
        selectedTiles.clear();
        selectedTilesChanged = true;

    }

    public void update(float tpf) {


        for (Army a : Armies) {
            a.update(tpf);
        }

        if (selectedTilesChanged) {
            //     showSelectedTiles();
        }


    }

    // Spawns a physical army on the world map
    public Army createArmy(int x, int z, int player, ArrayList<Unit> units) {

        Spatial m = assetManager.loadModel("Models/Oto/Oto.mesh.xml");
        Army a = new Army(x, z, player, m, this);

        Armies.add(a);
        scene.attachChild(m);
        bulletState.getPhysicsSpace().add(a.control);

        return a;

    }

    // Removes an army from the world map
    public void removeArmy(Army a) {

        Armies.remove(a);
        scene.detachChild(a.model);


    }

    public int getTileCosts(int x, int z) {
        return worldTiles[ensureInTerrainX(x)][ensureInTerrainZ(z)].cost;
    }

    // Returns army object
    public Army getArmy(Spatial model) {

        for (Army w : Armies) {
            if (w.model == model) {
                return w;
            }
        }
        return null;
    }

    // Returns army object
    public Army getArmy(int x, int z) {

        for (Army w : Armies) {
            if (w.posX == x && w.posZ == z) {
                return w;
            }
        }
        return null;
    }

    public Settlement getSettlement(Spatial model) {

        for (Settlement s : Main.DB.settlements) {
            if (s.model == model) {
                return s;
            }
        }
        return null;
    }

    public Settlement getSettlement(int x, int z) {

        for (Settlement s : Main.DB.settlements) {
            if (s.posX == x && s.posZ == z) {
                return s;
            }
        }
        return null;
    }

    // Marks the army as currently selected
    public void selectArmy(Army army) {
        if (army == null) {
            return;
        }
        selectedArmy = army;
        drawReachableArea(army);

    }

    // Marks the army as currently selected
    public void selectSettlement(Settlement s) {
        if (s == null) {
            return;
        }
        selectedSettlement = s;
        System.out.println(s.name);

    }

    public int ensureMinMax(int value, int min, int max) {
        return Math.min(max, Math.max(min, value));


    }

    public int ensureInTerrainX(int value) {
        return Math.min(width - 1, Math.max(0, value));


    }

    public int ensureInTerrainZ(int value) {
        return Math.min(height - 1, Math.max(0, value));

    }

    // Run BFS to find the reachable tiles for the army
    public void drawReachableArea(Army army) {

        deselectTiles();

        ArrayList<Tile> area = pathFinder.getReachableArea(army);

        for (Tile t : area) {
        }
    }

    // These four functions check if a tile can be walked or sailed on
    public boolean walkableTile(Tile t) {
        return walkableTile(t.x, t.z);
    }

    public boolean walkableTile(int x, int z) {
        return Main.DB.map.tiles.get(worldTiles[x][z].groundType).walkable;
    }

    public boolean sailableTile(Tile t) {
        return sailableTile(t.x, t.z);
    }

    public boolean sailableTile(int x, int z) {
        return Main.DB.map.tiles.get(worldTiles[x][z].groundType).sailable;
    }

    public void marchTo(Army a, Tile t) {
        marchTo(a, t.x, t.z);
    }

    public void marchTo(Army a, Settlement s) {
        marchTo(a, s.posX, s.posZ);
    }

    public void marchTo(Army a, Army goal) {
        marchTo(a, goal.posX, goal.posZ);
    }

    public void marchTo(Army a, int x, int z) {

        Stack<Tile> p = pathFinder.findPath(new Tile(a.posX, a.posZ), new Tile(x, z));
        if (p != null) {
            selectedArmy.setRoute(p);
        }
    }

    public Region getRegionByRGB(Vector3f col) {
        for (Region reg : Main.DB.regions) {
            if (reg.color.equals(col)) {
                return reg;
            }
        }
        return null;
    }

    public Climate getClimateByRGB(int r, int g, int b) {
        return getClimateByRGB(new Vector3f(r, g, b));
    }

    public Climate getClimateByRGB(Vector3f col) {
        for (Climate t : Main.DB.climates) {
            if (t.color.equals(col)) {
                return t;
            }
        }
        return null;
    }

    public int RGBtoGroundType(int r, int g, int b) {
        return RGBtoGroundType(new Vector3f(r, g, b));
    }

    public int RGBtoGroundType(Vector3f col) {
        for (openwar.DB.GenericTile t : Main.DB.map.tiles.values()) {
            if (t.color.equals(col)) {
                return t.type;
            }
        }
        return -1;
    }

    public int RGBtoVisualGroundType(int r, int g, int b) {
        return RGBtoVisualGroundType(new Vector3f(r, g, b));
    }

    public int RGBtoVisualGroundType(Vector3f col) {
        for (openwar.DB.GenericTile t : Main.DB.map.tiles.values()) {
            if (t.color.equals(col)) {
                return t.textureid;
            }
        }
        return -1;
    }

    public int getVisualGroundType(int groundtype) {
        for (openwar.DB.GenericTile t : Main.DB.map.tiles.values()) {
            if (t.type == groundtype) {
                return t.textureid;
            }
        }
        return -1;
    }

    public int getGroundTypeCost(int type) {
        openwar.DB.GenericTile t = Main.DB.map.tiles.get(type);
        if (t != null) {
            return t.cost;
        }

        return -1;
    }

    public boolean isWalkable(int type) {
        openwar.DB.GenericTile t = Main.DB.map.tiles.get(type);
        if (t != null) {
            return t.walkable;
        }

        return false;
    }

    public boolean isSailable(int type) {
        openwar.DB.GenericTile t = Main.DB.map.tiles.get(type);
        if (t != null) {
            return t.sailable;
        }
        return false;
    }

    public String getGroundTypeString(int type) {

        openwar.DB.GenericTile t = Main.DB.map.tiles.get(type);
        if (t != null) {
            return t.name;
        }

        return "N/A";
    }

    // Fills the key textures at a specific base address with the right blending value
    public boolean computeKeys(int groundtype, int base, byte[] data0, byte[] data1, byte[] data2) {
        switch (groundtype) {
            case (0):
                data0[base + 0] = (byte) 255;
                break;
            case (1):
                data0[base + 1] = (byte) 255;
                break;
            case (2):
                data0[base + 2] = (byte) 255;
                break;
            case (3):
                data0[base + 3] = (byte) 255;
                break;
            case (4):
                data1[base + 0] = (byte) 255;
                break;
            case (5):
                data1[base + 1] = (byte) 255;
                break;
            case (6):
                data1[base + 2] = (byte) 255;
                break;
            case (7):
                data1[base + 3] = (byte) 255;
                break;
            case (8):
                data2[base + 0] = (byte) 255;
                break;
            case (9):
                data2[base + 1] = (byte) 255;
                break;
            case (10):
                data2[base + 2] = (byte) 255;
                break;
            case (11):
                data2[base + 3] = (byte) 255;
                break;
            default:
                return false;
        }
        return true;
    }

    public void fadeSeason() {
    }
}
