/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package openwar.world;

import com.jme3.asset.AssetManager;
import com.jme3.asset.TextureKey;
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

    // Holds ground type information
    public class WorldTile extends Tile {

        int groundType;
        int cost;
        String region;
        String climate;

        public WorldTile(int x, int z, int type, String r, String c) {
            super(x, z);
            region = r;
            groundType = type;
            cost = app.DB.map.tiles.get(type).cost;
            climate = c;

        }

        @Override
        public String toString() {
            return super.toString()
                    + " Type: " + getGroundTypeString(groundType)
                    + "     Region: " + app.DB.hashedRegions.get(region).name
                    + "     Climate: " + climate;
        }
    };
    Main app;
    int width, height;
    TerrainQuad terrain;
    BulletAppState bulletState;
    Node scene = new Node("worldmap"), rootScene;
    Material matTerrain, matTerrainDebug;
    AssetManager assetManager;
    WorldHeightMap heightMap;
    Texture key0Image, key1Image, key2Image;
    WorldTile[][] worldTiles;
    ArrayList<SelectionTile> selectedTiles = new ArrayList<SelectionTile>();
    boolean selectedTilesChanged = false;
    Geometry selectedTilesOverlay;
    Material matOverlay;
    ArrayList<WorldArmy> worldArmies = new ArrayList<WorldArmy>();
    WorldArmy selectedArmy;
    Settlement selectedSettlement;
    FilterPostProcessor fpp;
    WorldMapPathFinder pathFinder = new WorldMapPathFinder(this);
    private static final Logger logger = Logger.getLogger(WorldMap.class.getName());

    public WorldMap(Main app, Node scene) {
        this.app = app;
        this.bulletState = app.bulletState;
        this.assetManager = app.getAssetManager();
        this.rootScene = scene;
        heightMap = null;
        this.scene.addControl(new UpdateControl());


    }

    // Overlays the terrain with the specific images for world map creation
    public void displayDebugMaterial(int l) {

        switch (l) {

            case 0:
                matTerrainDebug.setTexture("ColorMap", app.DB.map.typesTex);
                break;
            case 1:
                matTerrainDebug.setTexture("ColorMap", app.DB.map.regionsTex);
                break;
            case 2:
                matTerrainDebug.setTexture("ColorMap", app.DB.map.climatesTex);
                break;
        }

        terrain.setMaterial(matTerrainDebug);

    }

    public void displayStandardMaterial() {
        terrain.setMaterial(matTerrain);

    }

    public boolean createTerrain() {

        // Create key textures
        int size = width * height * 4;
        ByteBuffer buf0 = ByteBuffer.allocateDirect(size);
        ByteBuffer buf1 = ByteBuffer.allocateDirect(size);
        ByteBuffer buf2 = ByteBuffer.allocateDirect(size);
        byte[] data0 = new byte[size];
        byte[] data1 = new byte[size];
        byte[] data2 = new byte[size];
        ByteBuffer buffer = app.DB.map.typesTex.getImage().getData(0);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int base = (y * width + x) * 4;
                int r, g, b;
                r = buffer.get() & 0xff;
                g = buffer.get() & 0xff;
                b = buffer.get() & 0xff;
                if (!computeKeys(r, g, b, base, data0, data1, data2)) {
                    logger.log(Level.SEVERE,
                            "Unknown tile at ({0},{1}): {2} {3} {4}", new Object[]{x, y, r, g, b});
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


        // Create standard material with all textures and needed values
        matTerrain = new Material(assetManager, "Common/MatDefs/Terrain/TerrainLighting.j3md");
        matTerrain.setTexture("DiffuseMap", app.DB.map.tileTextures.get(0));
        app.DB.map.tileTextures.get(0).setWrap(Texture.WrapMode.Repeat);

        matTerrain.setFloat("DiffuseMap_0_scale", 4f);
        for (int i = 1; i < app.DB.map.tilesCount; i++) {
            matTerrain.setTexture("DiffuseMap_" + i, app.DB.map.tileTextures.get(i));
            matTerrain.setFloat("DiffuseMap_" + i + "_scale", 4f);
            app.DB.map.tileTextures.get(i).setWrap(Texture.WrapMode.Repeat);

        }
        matTerrain.setTexture("AlphaMap", key0Image);
        matTerrain.setTexture("AlphaMap_1", key1Image);
        matTerrain.setTexture("AlphaMap_2", key2Image);



        // Create debug material to display ground types, regions, climates etc.
        matTerrainDebug = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");


        // Create mesh data with material and place its north-western edge to the origin
        heightMap = new WorldHeightMap(app.DB.map.heightmapTex,
                app.DB.map.terrain.heightmap.factor0,
                app.DB.map.terrain.heightmap.factor1,
                app.DB.map.terrain.heightmap.offset);

        heightMap.load(false, false);
        terrain = new TerrainQuad("terrain", 32, heightMap.getSize(), heightMap.getHeightMap());
        terrain.setMaterial(matTerrain);
        terrain.setLocalTranslation(width / 2f, 0f, height / 2f);

        return true;
    }

    public boolean createRegions() {

        // Create worldTiles array and read according region and climate from images
        worldTiles = new WorldTile[width][height];
        ByteBuffer buf = app.DB.map.typesTex.getImage().getData(0);
        ByteBuffer regs = app.DB.map.regionsTex.getImage().getData(0);
        ByteBuffer clis = app.DB.map.climatesTex.getImage().getData(0);
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                int base = (j * width + i) * 3;
                int r, g, b, r1, g1, b1, r2, g2, b2;
                r = buf.get(base + 0) & 0xff;
                g = buf.get(base + 1) & 0xff;
                b = buf.get(base + 2) & 0xff;
                r1 = regs.get(base + 0) & 0xff;
                g1 = regs.get(base + 1) & 0xff;
                b1 = regs.get(base + 2) & 0xff;
                r2 = clis.get(base + 0) & 0xff;
                g2 = clis.get(base + 1) & 0xff;
                b2 = clis.get(base + 2) & 0xff;

                int type = RGBtoGroundType(r, g, b);
                String climate = getClimateByRGB(r2, g2, b2);
                Region region = getRegionByRGB(new Vector3f(r1, g1, b1));
                if (region != null) {
                    worldTiles[i][height - 1 - j] = new WorldTile(i, height - 1 - j, type, region.refName, climate);
                }
            }
        }

        return true;

    }

    public boolean createEntities() {

        for (Region r : app.DB.regions) {

            if ("Ocean".equals(r.name)) {
                continue;
            }

            Spatial m = app.DB.genBuildings.get("city").levels.get(0).model.clone();
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

        width = app.DB.map.typesTex.getImage().getWidth();
        height = app.DB.map.typesTex.getImage().getHeight();


        try {
            if (!createRegions()) {
                System.out.println("Couldn't create regions...");
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
                    app.DB.map.terrain.sun.color.x / 255f,
                    app.DB.map.terrain.sun.color.y / 255f,
                    app.DB.map.terrain.sun.color.z / 255f, 1));
            dlight.setDirection(app.DB.map.terrain.sun.direction);
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



        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return true;

    }

    // Returns for a tile the real opengl center coordinates
    public Vector3f getGLTileCenter(int x, int z) {
        float newx = 0.5f * (1f - 1f / (float) width) + (x * (1f + 1f / (float) width));
        float newz = 0.5f * (1f - 1f / (float) height) + (z * (1f + 1f / (float) height));
        return new Vector3f(newx, heightMap.getInterpolatedHeight(newx, newz), newz);
    }

    // Return for a tile the opengl coordinates of the upper left corner
    public Vector3f getGLTileCorner(int x, int z) {
        float newx = (x * (1f + 1f / (float) width));
        float newz = (z * (1f + 1f / (float) height));
        return new Vector3f(newx, heightMap.getInterpolatedHeight(newx, newz), newz);
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


        for (WorldArmy a : worldArmies) {
            a.update(tpf);
        }



        if (selectedTilesChanged) {
            //     showSelectedTiles();
        }


    }

    // Spawns a physical army on the world map
    public WorldArmy createArmy(int x, int z, int player, ArrayList<Unit> units) {

        Spatial m = (Spatial) assetManager.loadModel("Models/Oto/Oto.mesh.xml");
        WorldArmy a = new WorldArmy(x, z, player, m, this);

        worldArmies.add(a);
        scene.attachChild(m);
        bulletState.getPhysicsSpace().add(a.control);

        return a;

    }

    // Removes an army from the world map
    public void removeArmy(WorldArmy a) {

        worldArmies.remove(a);
        scene.detachChild(a.model);


    }

    public int getTileCosts(int x, int z) {
        return worldTiles[ensureInTerrainX(x)][ensureInTerrainZ(z)].cost;
    }

    // Returns army object
    public WorldArmy getArmy(Spatial model) {

        for (WorldArmy w : worldArmies) {
            if (w.model == model) {
                return w;
            }
        }
        return null;
    }

    // Returns army object
    public WorldArmy getArmy(int x, int z) {

        for (WorldArmy w : worldArmies) {
            if (w.posX == x && w.posZ == z) {
                return w;
            }
        }
        return null;
    }

    public Settlement getSettlement(Spatial model) {

        for (Settlement s : app.DB.settlements) {
            if (s.model == model) {
                return s;
            }
        }
        return null;
    }

    public Settlement getSettlement(int x, int z) {

        for (Settlement s : app.DB.settlements) {
            if (s.posX == x && s.posZ == z) {
                return s;
            }
        }
        return null;
    }

    // Marks the army as currently selected
    public void selectArmy(WorldArmy army) {
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
    public void drawReachableArea(WorldArmy army) {

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
        return app.DB.map.tiles.get(worldTiles[x][z].groundType).walkable;
    }

    public boolean sailableTile(Tile t) {
        return sailableTile(t.x, t.z);
    }

    public boolean sailableTile(int x, int z) {
        return app.DB.map.tiles.get(worldTiles[x][z].groundType).sailable;
    }

    public void marchTo(WorldArmy a, Tile t) {
        marchTo(a, t.x, t.z);
    }

    public void marchTo(WorldArmy a, Settlement s) {
        marchTo(a, s.posX, s.posZ);
    }

    public void marchTo(WorldArmy a, WorldArmy goal) {
        marchTo(a, goal.posX, goal.posZ);
    }

    public void marchTo(WorldArmy a, int x, int z) {

        Stack<Tile> p = pathFinder.findPath(new Tile(a.posX, a.posZ), new Tile(x, z));
        if (p != null) {
            selectedArmy.setRoute(p);
        }
    }

    public Region getRegionByRGB(Vector3f col) {
        for (Region reg : app.DB.regions) {
            if (reg.color.equals(col)) {
                return reg;
            }
        }
        return null;
    }

    public String getClimateByRGB(int r, int g, int b) {
        return getClimateByRGB(new Vector3f(r, g, b));
    }

    public String getClimateByRGB(Vector3f col) {
        for (openwar.DB.Map.Climate t : app.DB.map.climates) {
            if (t.color.equals(col)) {
                return t.name;
            }
        }
        return "N/A";
    }

    public int RGBtoGroundType(int r, int g, int b) {
        return RGBtoGroundType(new Vector3f(r, g, b));
    }

    public int RGBtoGroundType(Vector3f col) {
        for (openwar.DB.GenericTile t : app.DB.map.tiles.values()) {
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
        for (openwar.DB.GenericTile t : app.DB.map.tiles.values()) {
            if (t.color.equals(col)) {
                return t.textureid;
            }
        }
        return -1;
    }

    public int getGroundTypeCost(int type) {
        openwar.DB.GenericTile t = app.DB.map.tiles.get(type);
        if (t != null) {
            return t.cost;
        }

        return -1;
    }

    public boolean isWalkable(int type) {
        openwar.DB.GenericTile t = app.DB.map.tiles.get(type);
        if (t != null) {
            return t.walkable;
        }

        return false;
    }

    public boolean isSailable(int type) {
        openwar.DB.GenericTile t = app.DB.map.tiles.get(type);
        if (t != null) {
            return t.sailable;
        }
        return false;
    }

    public String getGroundTypeString(int type) {

        openwar.DB.GenericTile t = app.DB.map.tiles.get(type);
        if (t != null) {
            return t.name;
        }

        return "N/A";
    }

    // Fills the key textures at a specific base address with the right blending value
    public boolean computeKeys(int r, int g, int b, int base, byte[] data0, byte[] data1, byte[] data2) {

        int type = RGBtoVisualGroundType(r, g, b);
        switch (type) {
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
