/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package openwar;

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
import java.util.Locale;
import java.util.Scanner;
import java.util.Stack;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

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
        WorldRegion region;
        String climate;

        public WorldTile(int x, int z, int type, WorldRegion r, String c) {
            super(x, z);
            region = r;
            groundType = type;
            cost = GroundTypeManager.getGroundTypeCost(type);
            climate = c;

        }

        @Override
        public String toString() {
            return super.toString()
                    + " Type: " + GroundTypeManager.getGroundTypeString(groundType)
                    + "     Region: " + region.name
                    + "     Climate: " + climate;
        }
    };
    Main app;
    int width, height;
    TerrainQuad terrain;
    BulletAppState bulletState;
    Node scene=new Node("worldmap"), rootScene;
    Material matTerrain, matTerrainDebug;
    AssetManager assetManager;
    TWWorldHeightMap heightMap;
    Texture[] textures;
    Texture key0Image, key1Image, key2Image;
    Texture groundTypeImage;
    Texture heightMapImage;
    Texture regionsImage;
    Texture climatesImage;
    WorldTile[][] worldTiles;
    ArrayList<SelectionTile> selectedTiles=new ArrayList<SelectionTile>();
    boolean selectedTilesChanged = false;
    Geometry selectedTilesOverlay;
    Material matOverlay;
    Vector3f sunDirection = new Vector3f(-0.3f, -0.8f, -1f).normalize();
    ArrayList<WorldRegion> worldRegions=new ArrayList<WorldRegion>();
    ArrayList<WorldArmy> worldArmies= new ArrayList<WorldArmy>();
    ArrayList<WorldCity> worldCities=new ArrayList<WorldCity>();
    WorldArmy selectedArmy;
    WorldCity selectedCity;
    FilterPostProcessor fpp;
    WorldMapPathFinder pathFinder = new WorldMapPathFinder(this);

    public WorldMap(Main app, AssetManager assetman, BulletAppState bullet, Node scene) {
        this.app = app;
        this.bulletState = bullet;
        this.assetManager = assetman;
        this.rootScene = scene;
        heightMap = null;
        this.scene.addControl(new UpdateControl());


    }

    // Overlays the terrain with the specific images for world map creation
    public void displayDebugMaterial(int l) {

        switch (l) {

            case 0:
                matTerrainDebug.setTexture("ColorMap", groundTypeImage);
                break;
            case 1:
                matTerrainDebug.setTexture("ColorMap", regionsImage);
                break;
            case 2:
                matTerrainDebug.setTexture("ColorMap", climatesImage);
                break;
        }

        terrain.setMaterial(matTerrainDebug);

    }

    public void displayStandardMaterial() {
        terrain.setMaterial(matTerrain);

    }

    public boolean createTerrain(Element root) {


        // Create key textures
        ByteBuffer buf0 = ByteBuffer.allocateDirect(width * height * 4);
        ByteBuffer buf1 = ByteBuffer.allocateDirect(width * height * 4);
        ByteBuffer buf2 = ByteBuffer.allocateDirect(width * height * 4);

        if (!GroundTypeManager.CreateKeyTextures(groundTypeImage.getImage(), buf0, buf1, buf2)) {
            return false;

        }
        key0Image = new Texture2D(new Image(Image.Format.RGBA8, width, height, buf0));
        key1Image = new Texture2D(new Image(Image.Format.RGBA8, width, height, buf1));
        key2Image = new Texture2D(new Image(Image.Format.RGBA8, width, height, buf2));


        // Create standard material with all textures and needed values
        matTerrain = new Material(assetManager, "Common/MatDefs/Terrain/TerrainLighting.j3md");
        textures = new Texture[12];
        for (int i = 0; i < 12; i++) {
            textures[i] = assetManager.loadTexture("map/textures/" + i + ".tga");
            textures[i].setWrap(Texture.WrapMode.Repeat);
        }
        matTerrain.setTexture("DiffuseMap", textures[0]);
        matTerrain.setFloat("DiffuseMap_0_scale", 4f);
        for (int i = 1; i < 12; i++) {
            matTerrain.setTexture("DiffuseMap_" + i, textures[i]);
            matTerrain.setFloat("DiffuseMap_" + i + "_scale", 4f);
        }
        matTerrain.setTexture("AlphaMap", key0Image);
        matTerrain.setTexture("AlphaMap_1", key1Image);
        matTerrain.setTexture("AlphaMap_2", key2Image);



        // Create debug material to display ground types, regions, climates etc.
        matTerrainDebug = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");


        // Read heightmap coefficients
        Element el = (Element) root.getElementsByTagName("terrain").item(0);
        el = (Element) el.getElementsByTagName("heightmap").item(0);
        Scanner s = new Scanner(el.getAttribute("factor0"));
        s.useLocale(Locale.ENGLISH);
        float factor0 = s.nextFloat();
        s = new Scanner(el.getAttribute("factor1"));
        s.useLocale(Locale.ENGLISH);
        float factor1 = s.nextFloat();
        s = new Scanner(el.getAttribute("offset"));
        s.useLocale(Locale.ENGLISH);
        float offset = s.nextFloat();


        // Create mesh data with material and place its north-western edge to the origin
        heightMap = new TWWorldHeightMap(heightMapImage, factor0, factor1, offset);
        heightMap.load(false, true);
        terrain = new TerrainQuad("terrain", 32, heightMap.getSize(), heightMap.getHeightMap());
        terrain.setMaterial(matTerrain);
        terrain.setLocalTranslation(width / 2f, 0f, height / 2f);

        return true;
    }

    public boolean createRegions(Element root) {


        // Add the ocean as the 0'th region 
        worldRegions.add(new WorldRegion("Ocean", null, 0, 0, 0, this));

        // Run through the file and read all regions
        NodeList nl = root.getElementsByTagName("region");
        if (nl != null && nl.getLength() > 0) {
            for (int i = 0; i < nl.getLength(); i++) {
                Element el = (Element) nl.item(i);
                String name = el.getAttribute("name");
                String city = el.getAttribute("city");
                Scanner s = new Scanner(el.getAttribute("color"));
                int r = s.nextInt();
                int g = s.nextInt();
                int b = s.nextInt();
                worldRegions.add(new WorldRegion(name, city, r, g, b, this));

            }
        }


        // Create worldTiles array and read according region and climate from images
        worldTiles = new WorldTile[width][height];
        ByteBuffer buf = groundTypeImage.getImage().getData(0);
        ByteBuffer regs = regionsImage.getImage().getData(0);
        ByteBuffer clis = climatesImage.getImage().getData(0);
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
                WorldRegion region;

                // City found, get according region and save it
                if ((r1 == 255) && (g1 == 255) && (b1 == 255)) {
                    region = worldTiles[i][j - 1].region;
                    region.city.posX = i;
                    region.city.posZ = j;
                } else {
                    region = getRegionByRGB(r1, g1, b1);
                }
                int type = GroundTypeManager.RGBtoGroundType(r, g, b);
                String climate = getClimateByRGB(r2, g2, b2);
                worldTiles[i][j] = new WorldTile(i, j, type, region, climate);
            }
        }

        return true;

    }

    public boolean createEntities(Element root) {

        for (WorldRegion r : worldRegions) {

            if ("Ocean".equals(r.name)) {
                continue;
            }

            worldCities.add(r.city);

            Spatial m = (Spatial) assetManager.loadModel("Models/Sign Post/Sign Post.mesh.xml");
            m.setShadowMode(ShadowMode.CastAndReceive);
            m.setLocalScale(0.5f);
            Vector3f vec = getGLTileCenter(r.city.posX, r.city.posZ);
            m.setLocalTranslation(vec);
            r.city.model = m;
            scene.attachChild(m);

        }

        return true;

    }

    // Create the whole world map
    public boolean createWorldMap() {


        // Load all important information for the world map
        heightMapImage = assetManager.loadTexture(new TextureKey("map/base/heights.tga", false));
        groundTypeImage = assetManager.loadTexture(new TextureKey("map/base/types.tga", false));
        regionsImage = assetManager.loadTexture(new TextureKey("map/base/regions.tga", false));
        climatesImage = assetManager.loadTexture(new TextureKey("map/base/climates.tga", false));
        width = groundTypeImage.getImage().getWidth();
        height = groundTypeImage.getImage().getHeight();


        try {

            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document dom = db.parse(app.locatorRoot + "map/base/world.xml");
            Element root = dom.getDocumentElement();


            if (!createRegions(root)) {
                System.out.println("Couldn't create regions...");
                return false;
            }
            if (!createTerrain(root)) {
                System.out.println("Couldn't create terrain...");
                return false;
            }
            if (!createEntities(root)) {
                System.out.println("Couldn't create entities...");
                return false;
            }

            Element el = (Element) root.getElementsByTagName("terrain").item(0);
            el = (Element) el.getElementsByTagName("sun").item(0);
            Scanner s = new Scanner(el.getAttribute("color"));
            float r = s.nextFloat();
            float g = s.nextFloat();
            float b = s.nextFloat();
            s = new Scanner(el.getAttribute("direction"));
            s.useLocale(Locale.ENGLISH);
            float x = s.nextFloat();
            float y = s.nextFloat();
            float z = s.nextFloat();
            sunDirection = new Vector3f(x, y, z);

            DirectionalLight dlight = new DirectionalLight();
            dlight.setColor(new ColorRGBA(r / 255f, g / 255f, b / 255f, 1));
            dlight.setDirection(sunDirection);
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
        selectedCity = null;
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

        for (WorldRegion r : worldRegions) {
            r.update(tpf);
        }

        if (selectedTilesChanged) {
            //     showSelectedTiles();
        }


    }

    // Spawns a physical army on the world map
    public WorldArmy createArmy(int x, int z, int player, ArrayList<ArmyUnit> units) {

        Spatial m = (Spatial) assetManager.loadModel("Models/Oto/Oto.mesh.xml");
        WorldArmy a = new WorldArmy(x, z, player, m, this);

        worldArmies.add(a);
        scene.attachChild(m);
        bulletState.getPhysicsSpace().add(a.control);
        a.units.add(new ArmyUnit(10));

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

    // Returns city object
    public WorldCity getCity(Spatial model) {

        for (WorldCity w : worldCities) {
            if (w.model == model) {
                return w;
            }
        }
        return null;
    }

    // Returns city object
    public WorldCity getCity(int x, int z) {

        for (WorldCity w : worldCities) {
            if (w.posX == x && w.posZ == z) {
                return w;
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
    public void selectCity(WorldCity city) {
        if (city == null) {
            return;
        }
        selectedCity = city;
        System.out.println(city.name);

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
        return GroundTypeManager.isWalkable(worldTiles[x][z].groundType);
    }

    public boolean sailableTile(Tile t) {
        return sailableTile(t.x, t.z);
    }

    public boolean sailableTile(int x, int z) {
        return GroundTypeManager.isSailable(worldTiles[x][z].groundType);
    }

    public void marchTo(WorldArmy a, Tile t) {
        marchTo(a, t.x, t.z);
    }

    public void marchTo(WorldArmy a, WorldCity c) {
        marchTo(a, c.posX, c.posZ);
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

    public WorldRegion getRegionByRGB(int r, int g, int b) {
        ColorRGBA col = new ColorRGBA(r, g, b, 0);
        for (WorldRegion reg : worldRegions) {
            if (reg.regionColor.equals(col)) {
                return reg;
            }
        }
        return null;
    }

    public String getClimateByRGB(int r, int g, int b) {

        if ((r == 0) && (g == 0) && (b == 0)) {
            return "Maritim";
        }

        if ((r == 255) && (g == 0) && (b == 0)) {
            return "Arid";
        }

        if ((r == 0) && (g == 255) && (b == 0)) {
            return "Humid";
        }

        if ((r == 0) && (g == 0) && (b == 255)) {
            return "Tundra";
        }

        return "N/A";
    }

    public void fadeSeason() {
    }
}
