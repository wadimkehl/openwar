/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package openwar.world;

import openwar.DB.Army;
import com.jme3.asset.AssetManager;
import com.jme3.light.DirectionalLight;

import com.jme3.material.Material;
import com.jme3.material.RenderState.BlendMode;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.post.FilterPostProcessor;
import com.jme3.post.filters.BloomFilter;
import com.jme3.renderer.queue.RenderQueue.Bucket;
import com.jme3.renderer.queue.RenderQueue.ShadowMode;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.VertexBuffer.Type;
import com.jme3.scene.control.UpdateControl;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Quad;
import com.jme3.scene.shape.Sphere;
import com.jme3.shadow.PssmShadowRenderer;
import com.jme3.terrain.geomipmap.TerrainQuad;
import com.jme3.texture.Image;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture2D;
import com.jme3.water.WaterFilter;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Stack;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;
import openwar.DB.Climate;
import openwar.DB.Faction;
import openwar.DB.GenericTile;
import openwar.DB.Region;
import openwar.DB.Settlement;
import openwar.DB.Settlement.Construction;
import openwar.DB.Unit;

import openwar.Main;

/**
 *
 * @author kehl
 */
public class WorldMap {

    public Main game;
    public int width, height;
    public TerrainQuad terrain;
    public Node scene = new Node("worldmap"), rootScene;
    public Material matTerrain;
    AssetManager assetManager;
    WorldHeightMap heightMap;
    public Texture key0Image, key1Image, key2Image, gridImage;
    public WorldTile[][] worldTiles;
    public Geometry reachableArea;
    public Army selectedArmy;
    public Settlement selectedSettlement;
    FilterPostProcessor fpp;
    public TilePathFinder pathFinder = new TilePathFinder(this);
    private static final Logger logger = Logger.getLogger(WorldMap.class.getName());
    public WorldMinimap minimap;

    public WorldMap(Main app, Node scene) {
        game = app;
        assetManager = app.getAssetManager();
        rootScene = scene;
        heightMap = null;


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

    public void showGrid(boolean on) {
        int color;
        if (on) {
            color = Integer.parseInt("7FFFFF7F", 16);
        } else {
            color = Integer.parseInt("000000", 16);
        }
        int grid_res = 16;
        ByteBuffer buf = ByteBuffer.allocateDirect(grid_res * grid_res * 4);
        for (int y = 0; y < grid_res; y++) {
            for (int x = 0; x < grid_res; x++) {
                if (x == 0 || y == 0 || x == grid_res - 1 || y == grid_res - 1) {
                    buf.putInt(color);
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
        matTerrain.setTexture("DiffuseMap", Main.DB.tileTextures.get(0));
        Main.DB.tileTextures.get(0).setWrap(Texture.WrapMode.Repeat);
        matTerrain.setFloat("DiffuseMap_0_scale", Main.DB.tileTextures_scales.get(0));
        for (int i = 1; i < Main.DB.tilesTexturesCount; i++) {
            matTerrain.setTexture("DiffuseMap_" + i, Main.DB.tileTextures.get(i));
            matTerrain.setFloat("DiffuseMap_" + i + "_scale", Main.DB.tileTextures_scales.get(i));
            Main.DB.tileTextures.get(i).setWrap(Texture.WrapMode.Repeat);

        }

        if (!createKeyTextures()) {
            return false;
        }

        showGrid(false);

        // Create mesh data with material and place its north-western edge to the origin
        heightMap = new WorldHeightMap(Main.DB.heightmapTex, Main.DB.heightmapParams.x,
                Main.DB.heightmapParams.y, Main.DB.heightmapParams.z);
        heightMap.load(false, false);
        terrain = new TerrainQuad("terrain", 32, heightMap.getSize(), heightMap.getHeightMap());
        terrain.setMaterial(matTerrain);
        terrain.setLocalTranslation(width / 2f, 0f, height / 2f);

        // Create 4 planes that hide the water effect outside of the game map
        Geometry plane = new Geometry("", new Quad(2 * width, 2 * height));
        Material mat = new Material(game.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
        plane.setMaterial(mat);
        plane.setLocalRotation(new Quaternion().fromAngles((float) Math.PI / 2f, 0f, 0f));
        plane.setLocalTranslation(-width / 2, 1f, -height / 2);
        // this.scene.attachChild(plane);
        return true;
    }

    public boolean createWorldTiles() {

        // Create worldTiles array and read according region and climate from images
        worldTiles = new WorldTile[width][height];
        ByteBuffer types = Main.DB.typesTex.getImage().getData(0);
        ByteBuffer regs = Main.DB.regionsTex.getImage().getData(0);
        ByteBuffer clis = Main.DB.climatesTex.getImage().getData(0);
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                //TODO: base maps must NOT be RGBA, but RGB
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

                // TODO: Find out why three textures are bgr after dumping them
                int type = RGBtoGroundType(b, g, r);
                Climate climate = getClimateByRGB(b2, g2, r2);
                Region region = getRegionByRGB(new Vector3f(b1, g1, r1));
                GenericTile tile = Main.DB.genTiles.get(type);
                if (region != null && climate != null && tile != null) {
                    worldTiles[i][height - 1 - j] = new WorldTile(i, height - 1 - j, type, tile.cost, region.refName, climate.refName);
                } else {
                    String cause;
                    if (region==null) cause = "region";
                    else if (climate==null) cause = "climate";
                    else cause = "tile";
                    logger.log(Level.SEVERE, "Error: " +cause+ " at ({0},{1}) ", new Object[]{i, height - 1 - j});
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

            r.settlement.createData(this);

        }

        for (Faction f : Main.DB.factions) {
            for (Army a : f.armies) {
                a.createData(this);
            }
        }
        
        for(WorldDecoration d : Main.DB.worldDecorations)
        {
            d.createData(this);
        }

        return true;

    }

    // Create the whole world map
    public boolean createWorldMap() {

        width = Main.DB.typesTex.getImage().getWidth();
        height = Main.DB.typesTex.getImage().getHeight();


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

        minimap = new WorldMinimap(this);

        scene.addControl(new UpdateControl());



        DirectionalLight dlight = new DirectionalLight();
        Vector3f col = Main.DB.sun_color;
        dlight.setColor(new ColorRGBA(col.x / 255f, col.y / 255f, col.z / 255f, 1));
        dlight.setDirection(Main.DB.sun_direction);
        scene.addLight(dlight);

        fpp = new FilterPostProcessor(assetManager);


        if (Main.DB.hasWater) {
            WaterFilter water = new WaterFilter(scene, new Vector3f(0.3f, -0.9f, 1f));
            water.setMaxAmplitude(0.2f);
            water.setWaterTransparency(15f);
            col = Main.DB.water_color;
            water.setWaterColor(new ColorRGBA(col.x / 255f, col.y / 255f, col.z / 255f, 1));
            water.setWaterHeight(Main.DB.waterHeight);
            water.setSpeed(0.1f);
            water.setFoamHardness(2f);
            water.setFoamExistence(new Vector3f(0.1f, 0.2f, 0.18f));
            fpp.addFilter(water);
        }


        if (!Main.devMode) {
            BloomFilter bloom = new BloomFilter();
            bloom.setExposurePower(8f);
            bloom.setBloomIntensity(1f);
            fpp.addFilter(bloom);

            PssmShadowRenderer pssm = new PssmShadowRenderer(assetManager, 1024, 8);
            pssm.setDirection(Main.DB.sun_direction);
            game.getViewPort().addProcessor(pssm);

        }

        game.getViewPort().addProcessor(fpp);


        reachableArea = new Geometry();
        scene.attachChild(terrain);
        rootScene.attachChild(scene);

        terrain.setShadowMode(ShadowMode.Receive);

        return true;

    }

    // Returns for a tile the real opengl center coordinates
    public Vector3f getGLTileCenter(int x, int z) {
        float newx = x + 0.5f;
        float newz = z + 0.5f;
        return new Vector3f(newx, heightMap.getInterpolatedHeight(newx, newz), newz);
    }

    public Vector3f getGLTileCenter(Tile t) {
        return getGLTileCenter(t.x, t.z);
    }

    // Return for a tile the opengl coordinates of the upper left corner
    public Vector3f getGLTileCorner(int x, int z) {
        return new Vector3f(x, heightMap.getInterpolatedHeight(x, z), z);
    }

    public void update(float tpf) {


        minimap.update();

        for (Faction f : Main.DB.factions) {
            for (Army a : f.armies) {
                a.update(tpf);
            }
        }



    }

    // Spawns a physical army on the world map
    public Army createArmy(int x, int z, String owner, ArrayList<Unit> units) {

        return null;

    }

    // Removes an army from the world map
    public void removeArmy(final Army a) {

        scene.getControl(UpdateControl.class).enqueue(new Callable() {

            @Override
            public Object call() throws Exception {

                Main.DB.hashedFactions.get(a.owner).armies.remove(a);
                scene.detachChild(a.node);

                if (selectedArmy == a) {
                    deselectAll();
                } else if (selectedArmy != null) {
                    selectArmy(selectedArmy);
                } else if (selectedSettlement != null) {
                    selectSettlement(selectedSettlement);
                }

                return null;
            }
        });




    }

    public int getTileCosts(Tile t) {
        return getTileCosts(t.x, t.z);
    }

    public int getTileCosts(int x, int z) {
        return worldTiles[ensureInTerrainX(x)][ensureInTerrainZ(z)].cost;
    }

    // Returns army object
    public Army getArmy(Spatial model) {
        for (Faction f : Main.DB.factions) {
            for (Army w : f.armies) {
                if (w.model == model) {
                    return w;
                }
            }
        }
        return null;
    }

    // Returns army object
    public Army getArmy(Tile t) {
        return getArmy(t.x, t.z);
    }

    // Returns army object
    public Army getArmy(int x, int z) {
        for (Faction f : Main.DB.factions) {
            for (Army w : f.armies) {
                if (w.posX == x && w.posZ == z) {
                    return w;
                }
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

    public Settlement getSettlement(Tile t) {
        return getSettlement(t.x, t.z);
    }

    public Settlement getSettlement(int x, int z) {

        for (Settlement s : Main.DB.settlements) {
            if (s.posX == x && s.posZ == z) {
                return s;
            }
        }
        return null;
    }

    public void deselectAll() {
        selectedArmy = null;
        selectedSettlement = null;
        game.worldMapState.uiController.deselectAll();
        scene.detachChild(reachableArea);

        for (int i = 0; i < 20; i++) {
            game.worldMapState.uiController.setImage("unit" + i, null);
        }

        //game.playSound("world_deselect_all");


    }

    // Marks the army as currently selected
    public void selectArmy(Army army) {
        if (army == null) {
            return;
        }

        deselectAll();
        selectedArmy = army;
        game.worldMapState.uiController.switchToUnitsLayer(army.units);
        army.calculateMovePoints();
        drawReachableArea(army);

        game.doScript("onArmySelected()");


    }

    // Marks the army as currently selected
    public void selectSettlement(Settlement s) {
        if (s == null) {
            return;
        }

        deselectAll();
        selectedSettlement = s;
        
        game.worldMapState.uiController.switchToBuildingsLayer(s.buildings);
        game.showUIElement("settlement_layer", true);


        game.doScript("onSettlementSelected()");


    }

    public Tile ensureInTerrain(Tile t) {
        return new Tile(ensureInTerrainX(t.x), ensureInTerrainZ(t.z));
    }

    public int ensureInTerrainX(float value) {
        return (int) Math.min(width - 1, Math.max(0, value));
    }

    public int ensureInTerrainZ(float value) {
        return (int) Math.min(height - 1, Math.max(0, value));

    }

    public void drawReachableArea(ArrayList<Unit> units, int posX, int posZ) {

        ArrayList<Tile> area = pathFinder.getReachableArea(units, posX, posZ, true, false);


        ArrayList<Vector2f> corners = new ArrayList<Vector2f>();
        for (Tile t : area) {
            corners.add(new Vector2f(t.x, t.z + 1));
            corners.add(new Vector2f(t.x + 1, t.z + 1));
            corners.add(new Vector2f(t.x + 1, t.z));
            corners.add(new Vector2f(t.x, t.z));
        }

        Mesh m = new Mesh();
        float[] verts = new float[corners.size() * 3];
        float[] colors = new float[corners.size() * 4];
        int[] indices = new int[area.size() * 6];

        for (int i = 0; i < corners.size(); i++) {
            float x = corners.get(i).x;
            float z = corners.get(i).y;
            verts[i * 3] = corners.get(i).x;
            verts[i * 3 + 1] = heightMap.getInterpolatedHeight(x, z) + 0.01f;
            verts[i * 3 + 2] = corners.get(i).y;

            colors[i * 4] = 0f;
            colors[i * 4 + 1] = 1f;
            colors[i * 4 + 2] = 0f;


        }

        for (int i = 0; i < area.size(); i++) {
            int base_ind = i * 6;
            int base_vert = i * 4;
            indices[base_ind] = base_vert;
            indices[base_ind + 1] = base_vert + 1;
            indices[base_ind + 2] = base_vert + 2;
            indices[base_ind + 3] = base_vert + 0;
            indices[base_ind + 4] = base_vert + 2;
            indices[base_ind + 5] = base_vert + 3;
        }

        m.setBuffer(Type.Position, 3, verts);
        m.setBuffer(Type.Index, 1, indices);
        m.setBuffer(Type.Color, 4, colors);
        m.setMode(Mesh.Mode.Triangles);
        m.setStatic();
        m.updateBound();

        scene.detachChild(reachableArea);
        reachableArea = new Geometry("reachableArea", m);
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setBoolean("VertexColor", true);
        mat.getAdditionalRenderState().setBlendMode(BlendMode.Modulate);
        reachableArea.setMaterial(mat);
        reachableArea.setQueueBucket(Bucket.Transparent);
        scene.attachChild(reachableArea);

    }

    // Run BFS to find the reachable tiles for the army
    public void drawReachableArea(Army army) {

        drawReachableArea(army.units, army.posX, army.posZ);

    }

    // These four functions check if a tile can be walked or sailed on
    public boolean walkableTile(Tile t) {
        return walkableTile(t.x, t.z);
    }

    public boolean walkableTile(int x, int z) {
        return Main.DB.genTiles.get(worldTiles[x][z].groundType).walkable;
    }

    public boolean sailableTile(Tile t) {
        return sailableTile(t.x, t.z);
    }

    public boolean sailableTile(int x, int z) {
        return Main.DB.genTiles.get(worldTiles[x][z].groundType).sailable;
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

        a.calculateMovePoints();
        Stack<Tile> p = pathFinder.findPath(new Tile(a.posX, a.posZ), new Tile(x, z), true, false);
        if (p != null) {
            a.setRoute(p);
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
        for (openwar.DB.GenericTile t : Main.DB.genTiles.values()) {
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
        for (openwar.DB.GenericTile t : Main.DB.genTiles.values()) {
            if (t.color.equals(col)) {
                return t.textureid;
            }
        }
        return -1;
    }

    public int getVisualGroundType(int groundtype) {
        for (openwar.DB.GenericTile t : Main.DB.genTiles.values()) {
            if (t.type == groundtype) {
                return t.textureid;
            }
        }
        return -1;
    }

    public boolean insideTerrain(Tile t) {
        return insideTerrain(t.x, t.z);
    }

    public boolean insideTerrain(int x, int z) {
        return (x >= 0 && z >= 0 && x < width && z < height);
    }

    public int getGroundTypeCost(int type) {
        openwar.DB.GenericTile t = Main.DB.genTiles.get(type);
        if (t != null) {
            return t.cost;
        }

        return -1;
    }

    public boolean isWalkable(int type) {
        openwar.DB.GenericTile t = Main.DB.genTiles.get(type);
        if (t != null) {
            return t.walkable;
        }

        return false;
    }

    public boolean isSailable(int type) {
        openwar.DB.GenericTile t = Main.DB.genTiles.get(type);
        if (t != null) {
            return t.sailable;
        }
        return false;
    }

    public String getGroundTypeString(int type) {

        openwar.DB.GenericTile t = Main.DB.genTiles.get(type);
        if (t != null) {
            return t.name;
        }

        return "N/A";
    }

    // Fills the key textures at a specific base_ind address with the right blending value
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
