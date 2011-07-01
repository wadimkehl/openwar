/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package openwar;

import com.jme3.app.Application;
import com.jme3.asset.AssetManager;
import com.jme3.bounding.BoundingBox;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.light.DirectionalLight;

import com.jme3.material.Material;
import com.jme3.material.RenderState.FaceCullMode;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.post.FilterPostProcessor;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.terrain.geomipmap.TerrainQuad;
import com.jme3.texture.Image;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture2D;
import com.jme3.util.SkyFactory;
import com.jme3.water.SimpleWaterProcessor;
import com.jme3.water.WaterFilter;
import java.nio.ByteBuffer;
import java.util.AbstractQueue;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Stack;
import jme3tools.converters.ImageToAwt;

/**
 *
 * @author kehl
 */
public class WorldMap {

    // Basic (x,z)-Tile
    public class Tile {

        int x, z;

        public Tile(int x, int z) {
            this.x = x;
            this.z = z;
        }

        @Override
        public String toString() {
            return "(" + x + "," + z + ")";
        }
    };

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

    // Serves for path finding things
    public class PathTile extends Tile {

        public int distance;
        public PathTile ancestor;

        public PathTile(int x, int z, int d, PathTile a) {
            super(x, z);
            distance = d;
            ancestor = a;
        }
    }

    // Holds ground type information
    public class WorldTile extends Tile {

        int groundType;
        int cost;

        public WorldTile(int x, int z, int type) {
            super(x, z);
            groundType = type;
            cost = GroundTypeManager.getGroundTypeCost(type);

        }
    };
    Application app;
    int width, height;
    TerrainQuad terrain;
    BulletAppState bulletState;
    Node scene, rootScene;
    Material matTerrain;
    AssetManager assetManager;
    WorldHeightMap heightMap;
    Texture[] textures;
    Texture key0Image, key1Image, key1Image_overlay;
    Texture groundTypeImage;
    Texture heightMapImage;
    Texture regionsImage;
    WorldTile[][] worldTiles;
    ArrayList<SelectionTile> selectedTiles;
    boolean selectedTilesChanged = false;
    Geometry selectedTilesOverlay;
    Material matOverlay;
    Vector3f sunDirection = new Vector3f(-0.3f, -0.8f, -1f).normalize();
    ArrayList<WorldArmy> worldArmies;
    ArrayList<WorldCity> worldCities;
    WorldArmy selectedArmy;
    WorldCity selectedCity;
    WorldArmy armyToDelete = null;

    public WorldMap(
            Application app, AssetManager assetman, BulletAppState bullet, Node scene) {
        this.app = app;
        this.bulletState = bullet;
        this.assetManager = assetman;
        this.rootScene = scene;


    }

    public boolean create() {

        heightMap = null;
        scene = new Node("worldmap");
        worldArmies = new ArrayList<WorldArmy>();
        worldCities = new ArrayList<WorldCity>();

        try {

            heightMapImage = assetManager.loadTexture("map/heights.tga");
            groundTypeImage = assetManager.loadTexture("map/types.tga");
//            regionsImage = assetManager.loadTexture("map/regions.tga");


            // Create key textures
            width = groundTypeImage.getImage().getWidth();
            height = groundTypeImage.getImage().getHeight();
            ByteBuffer buf0 = ByteBuffer.allocateDirect(width * height * 4);
            ByteBuffer buf1 = ByteBuffer.allocateDirect(width * height * 4);
            if (!GroundTypeManager.CreateKeyTextures(groundTypeImage.getImage(), buf0, buf1)) {
                return false;

            }
            key0Image = new Texture2D(new Image(Image.Format.RGBA8, width, height, buf0));
            key1Image = new Texture2D(new Image(Image.Format.RGBA8, width, height, buf1));

            matTerrain = new Material(assetManager, "materials/terrain/terrain.j3md");
            matTerrain.setTexture("Key0", key0Image);
            matTerrain.setTexture("Key1", key1Image);

            // Create worldTiles array
            worldTiles = new WorldTile[width][height];
            ByteBuffer buf = groundTypeImage.getImage().getData(0);
            for (int i = 0; i < width; i++) {
                for (int j = 0; j < height; j++) {
                    int base = (j * width + i) * 3;
                    int r, g, b;
                    r = buf.get(base + 0) & 0xff;
                    g = buf.get(base + 1) & 0xff;
                    b = buf.get(base + 2) & 0xff;

                    worldTiles[i][height - 1 - j] = new WorldTile(i, height - 1 - j, GroundTypeManager.RGBtoGroundType(r, g, b));
                }
            }

            // Create graphical structures
            textures = new Texture[14];
            textures[0] = assetManager.loadTexture("textures/0.tga");
            textures[1] = assetManager.loadTexture("textures/1.tga");
            textures[2] = assetManager.loadTexture("textures/2.tga");
            textures[3] = assetManager.loadTexture("textures/3.tga");
            textures[4] = assetManager.loadTexture("textures/4.tga");
            textures[5] = assetManager.loadTexture("textures/5.tga");
            textures[6] = assetManager.loadTexture("textures/6.tga");
            textures[7] = assetManager.loadTexture("textures/7.tga");
            textures[8] = assetManager.loadTexture("textures/8.tga");
            textures[9] = assetManager.loadTexture("textures/9.tga");
            textures[10] = assetManager.loadTexture("textures/10.jpg");
            textures[11] = assetManager.loadTexture("textures/11.tga");
            textures[12] = assetManager.loadTexture("textures/overlay.png");
            textures[13] = assetManager.loadTexture("textures/overlay.png");
            for (int i = 0; i < 14; i++) {
                textures[i].setWrap(Texture.WrapMode.Repeat);
            }
            matTerrain.setTexture("Tex0", textures[0]);
            matTerrain.setTexture("Tex1", textures[1]);
            matTerrain.setTexture("Tex2", textures[2]);
            matTerrain.setTexture("Tex3", textures[3]);
            matTerrain.setTexture("Tex4", textures[4]);
            matTerrain.setTexture("Tex5", textures[5]);
            matTerrain.setTexture("Tex6", textures[6]);
            matTerrain.setTexture("Tex7", textures[7]);
            matTerrain.setTexture("Tex8", textures[8]);
            matTerrain.setTexture("Tex9", textures[9]);
            matTerrain.setTexture("Tex10", textures[10]);
            matTerrain.setTexture("Tex11", textures[11]);
            matTerrain.setTexture("Tex12", textures[12]);
            matTerrain.setTexture("Tex13", textures[13]);

            heightMap = new WorldHeightMap(ImageToAwt.convert(heightMapImage.getImage(), false, false, 0), 0.1f);
            heightMap.load(false, false);

            terrain = new TerrainQuad("terrain", 128, heightMap.getSize(), heightMap.getHeightMap());
            terrain.setMaterial(matTerrain);
            terrain.setLocalTranslation(width / 2f, 0f, height / 2f);

            selectedTiles = new ArrayList<SelectionTile>();



            FilterPostProcessor fpp = new FilterPostProcessor(assetManager);
            WaterFilter water = new WaterFilter(scene, sunDirection);

            water.setWaterHeight(-0.1f);
            water.setMaxAmplitude(0.15f);
            water.setSpeed(0.25f);
            water.setFoamHardness(1.5f);
            water.setFoamExistence(new Vector3f(0.1f, 0.2f, 0.15f));
            fpp.addFilter(water);
            app.getViewPort().addProcessor(fpp);


            DirectionalLight dlight = new DirectionalLight();
            dlight.setColor(ColorRGBA.White);
            dlight.setDirection(sunDirection);
            scene.addLight(dlight);

            selectedTilesOverlay = new Geometry("overlay");
            matOverlay = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
            matOverlay.setColor("Color", ColorRGBA.Blue);
            selectedTilesOverlay.setMaterial(matOverlay);
            matOverlay.getAdditionalRenderState().setFaceCullMode(FaceCullMode.Off);


            scene.attachChild(terrain);
//            scene.attachChild(selectedTilesOverlay);
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

        ByteBuffer b1 = key1Image.getImage().getData(0);
        ByteBuffer buf1 = ByteBuffer.allocateDirect(width * height * 4);

        // Draw standard key 1 values
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                int index = ((j * width + i) * 4);
                buf1.put(index, b1.get(index));
                buf1.put(index + 1, b1.get(index + 1));
                buf1.put(index + 2, b1.get(index + 2));
                buf1.put(index + 3, b1.get(index + 3));
            }
        }

        //Set new key 1 texture values (activate overlay0)
        for (SelectionTile t : selectedTiles) {
            int index = ((t.z * width + t.x) * 4);
            buf1.put(index + 3, (byte) (255 * t.intensity));
        }


        selectedTilesChanged = false;
        key1Image_overlay = new Texture2D(new Image(Image.Format.RGBA8, width, height, buf1));
        matTerrain.setTexture("Key1", key1Image_overlay);

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

        for (WorldCity c : worldCities) {
            c.update(tpf);
        }

        if (armyToDelete != null) {
            removeArmy(armyToDelete);
            armyToDelete = null;
        }


        if (selectedTilesChanged) {
            showSelectedTiles();


        }
    }

    public WorldArmy createArmy(int x, int z, int player) {

        Spatial m = (Spatial) assetManager.loadModel("Models/Oto/Oto.mesh.xml");
        WorldArmy a = new WorldArmy(x, z, player, m, this);

        worldArmies.add(a);
        scene.attachChild(m);
        bulletState.getPhysicsSpace().add(a.control);
        a.units.add(new ArmyUnit(10));

        return a;

    }

    public void removeArmy(WorldArmy a) {

        worldArmies.remove(a);
        scene.detachChild(a.model);


    }

    public WorldCity createCity(int x, int z, int player) {
        Spatial m = (Spatial) assetManager.loadModel("Models/Sign Post/Sign Post.mesh.xml");
        WorldCity c = new WorldCity(x, z, player, m, "Buxtehude", this);

        worldCities.add(c);
        scene.attachChild(c.model);
        return c;

    }

    public int getTileCosts(int x, int z) {
        return worldTiles[ensureInTerrainX(x)][ensureInTerrainZ(z)].cost;
    }

    public WorldArmy getArmy(Spatial model) {

        for (WorldArmy w : worldArmies) {
            if (w.model == model) {
                return w;
            }
        }
        return null;
    }

    public WorldArmy getArmy(int x, int z) {

        for (WorldArmy w : worldArmies) {
            if (w.posX == x && w.posZ == z) {
                return w;
            }
        }
        return null;
    }

    public WorldCity getCity(Spatial model) {

        for (WorldCity w : worldCities) {
            if (w.model == model) {
                return w;
            }
        }
        return null;
    }

    public WorldCity getCity(int x, int z) {

        for (WorldCity w : worldCities) {
            if (w.posX == x && w.posZ == z) {
                return w;
            }
        }
        return null;
    }

    public void selectArmy(WorldArmy army) {
        if (army == null) {
            return;
        }
        selectedArmy = army;
        drawReachableArea(army);

    }

    public void selectCity(WorldCity city) {
        if (city == null) {
            return;
        }
        selectedCity = city;


    }

    private int ensureMinMax(int value, int min, int max) {
        return Math.min(max, Math.max(min, value));


    }

    private int ensureInTerrainX(int value) {
        return Math.min(width - 1, Math.max(0, value));


    }

    private int ensureInTerrainZ(int value) {
        return Math.min(height - 1, Math.max(0, value));

    }

    // Run BFS to find the reachable tiles for the army
    public void drawReachableArea(WorldArmy army) {

        deselectTiles();
        int points = army.calculateMovePoints();
        if (points <= 0) {
            selectTile(army.posX, army.posZ, 0.3f);
            return;
        }

        // Holds global distance values discovered yet
        int[][] distance = new int[2 * points][2 * points];
        for (int x = 0; x < 2 * points; x++) {
            for (int z = 0; z < 2 * points; z++) {
                distance[x][z] = 10000;
            }
        }
        distance[points][points] = 0;

        // Do BFS for all tiles in question starting from army's position
        LinkedList<PathTile> q = new LinkedList<PathTile>();
        q.add(new PathTile(army.posX, army.posZ, 0, null));
        while (!q.isEmpty()) {

            PathTile t = q.remove();
            for (int x = -1; x < 2; x++) {
                for (int z = -1; z < 2; z++) {

                    int new_d = getTileCosts(t.x + x, t.z + z) + t.distance;
                    if (new_d >= points) {
                        continue;
                    }
                    int offset_x = ensureMinMax(points - army.posX + t.x + x, 0, 2 * points - 1);
                    int offset_z = ensureMinMax(points - army.posZ + t.z + z, 0, 2 * points - 1);
                    if (new_d < distance[offset_x][offset_z]) {
                        distance[offset_x][offset_z] = new_d;
                        q.add(new PathTile(t.x + x, t.z + z, new_d, t));
                    }
                }
            }
        }

        // Draw reachable tiles with strong border (closed hull)
        for (int x = -points; x < points; x++) {
            for (int z = -points; z < points; z++) {

                if (distance[points + x][points + z] <= points) {

                    boolean isBorder = false;
                    for (int _x = -1; _x < 2; _x++) {
                        for (int _z = -1; _z < 2; _z++) {
                            int new_x = ensureMinMax(points + x + _x, 0, 2 * points - 1);
                            int new_z = ensureMinMax(points + z + _z, 0, 2 * points - 1);
                            if ((distance[new_x][new_z] >= points - 1)) {
                                isBorder = true;
                            }
                        }
                    }

                    if (isBorder) {
                        selectTile(army.posX + x, army.posZ + z, 0.3f);
                    } else {
                        selectTile(army.posX + x, army.posZ + z, 0.3f);
                    }

                    // TODO: Checks for enemys, buildings, trees, rivers etc.
                }
            }

        }
    }

    public boolean walkableTile(Tile t) {
        return walkableTile(t.x, t.z);
    }

    public boolean walkableTile(int x, int z) {
        if (worldTiles[x][z].cost >= 1000) {
            return false;
        }

        return true;
    }

    // Calculates the shortest path between two tiles with A*
    // Not really because I dont calculate the heuristic part of the distance
    public Stack<PathTile> findPath(Tile start, Tile end) {

        if (!walkableTile(start) || !walkableTile(end)) {
            return null;
        }

        LinkedList<PathTile> open = new LinkedList<PathTile>();
        LinkedList<PathTile> closed = new LinkedList<PathTile>();
        open.add(new PathTile(start.x, start.z, 0, null));

        PathTile p = null;
        while (!open.isEmpty()) {

            // find in open list best candidate (minimal distance) and remove
            int min = 100000;
            PathTile best = null;
            for (PathTile temp : open) {
                if (temp.distance < min) {
                    min = temp.distance;
                    best = temp;
                }
            }
            open.remove(best);


            // check if we reached the goal
            if (best.x == end.x && best.z == end.z) {
                p = best;
                break;
            }

            // expand the candidate, i.e. run through all neighbors and check 'em
            for (int i = -1; i < 2; i++) {
                for (int j = -1; j < 2; j++) {
                    int newx = ensureInTerrainX(best.x + i);
                    int newz = ensureInTerrainZ(best.z + j);

                    // check if already in closed list -> ignore
                    boolean alreadyClosed = false;
                    for (PathTile temp : closed) {
                        if (temp.x == newx && temp.z == newz) {
                            alreadyClosed = true;
                        }
                    }
                    if (alreadyClosed) {
                        continue;
                    }

                    int new_distance = best.distance + worldTiles[newx][newz].cost;

                    // check if in open list
                    boolean alreadyOpen = false;
                    for (PathTile temp : open) {
                        if (temp.x == newx && temp.z == newz) {
                            alreadyOpen = true;

                            // check if we found a shorter path
                            if (new_distance < temp.distance) {
                                temp.ancestor = best;
                                temp.distance = new_distance;
                            }
                        }
                    }

                    if (!alreadyOpen) {
                        open.add(new PathTile(newx, newz, new_distance, best));
                    }

                }
            }

            // And finally add to closed list
            closed.add(best);

        }

        // Build the path recursively
        Stack<PathTile> path = new Stack<PathTile>();
        path.push(p);
        while (path.peek().ancestor != null) {
            path.push(path.peek().ancestor);
        }
        return path;
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

        Stack<PathTile> p = findPath(new Tile(a.posX, a.posZ), new Tile(x, z));
        if (p != null) {
            selectedArmy.setRoute(p);
        }
    }
}
