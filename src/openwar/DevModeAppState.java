/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package openwar;

import com.jme3.app.Application;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.collision.CollisionResult;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.AnalogListener;
import com.jme3.material.Material;
import com.jme3.math.Vector3f;
import com.jme3.terrain.geomipmap.TerrainPatch;
import com.jme3.texture.Image;
import com.jme3.texture.Texture2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.ByteBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import openwar.DB.Climate;
import openwar.DB.Region;
import openwar.world.WorldTile;

/**
 *
 * @author kehl
 */
public class DevModeAppState extends AbstractAppState {

    Main game;
    String currTexture = "types";
    int currType;
    String currRegion, currClimate;
    boolean drawing;
    boolean flyMode;
    Material matTerrainDev;
    private static final Logger logger = Logger.getLogger(DevModeAppState.class.getName());
    private AnalogListener analogListener = new AnalogListener() {

        @Override
        public void onAnalog(String name, float value, float tpf) {

            if (!drawing) {
                return;
            }

            if (name.equals("mouse_left")) {
                CollisionResult r = game.getMousePick(game.worldMapState.sceneNode);
                if (r == null) {
                    return;
                }

                Vector3f pt = r.getContactPoint();
                int x = (int) pt.x;
                int z = (int) pt.z;

                if (r.getGeometry() instanceof TerrainPatch) {

                    if ("types".equals(currTexture)) {
                        game.worldMapState.map.setWorldTile(x, z, currType);
                        updateTexture(currTexture);
                    } else if ("regions".equals(currTexture)) {
                        game.worldMapState.map.worldTiles[x][z].region = currRegion;
                        updateTexture(currTexture);
                        matTerrainDev.setTexture("ColorMap", Main.DB.regionsTex);

                    } else if ("climates".equals(currTexture)) {
                        game.worldMapState.map.worldTiles[x][z].climate = currClimate;
                        updateTexture(currTexture);
                        matTerrainDev.setTexture("ColorMap", Main.DB.climatesTex);

                    }

                }



            }
        }
    };
    public ActionListener actionListener = new ActionListener() {

        @Override
        public void onAction(String name, boolean pressed, float tpf) {




            if (name.equals("texture_types") && !pressed) {

                displayTerrainTexture("types");

            } else if (name.equals("texture_regions") && !pressed) {

                displayTerrainTexture("regions");

            } else if (name.equals("texture_climates") && !pressed) {

                displayTerrainTexture("climates");

            } else if (name.equals("draw_mode") && !pressed) {
                drawing = !drawing;
                System.err.println("Drawing mode " + drawing);




            } else if (name.equals("previousType") && !pressed) {
                currType--;
                String currSelection = "";
                if ("types".equals(currTexture)) {
                    if (currType < 0) {
                        currType = Main.DB.genTiles.keySet().size() - 1;
                    }
                    currSelection = Main.DB.genTiles.get(currType).name;

                } else if ("regions".equals(currTexture)) {
                    if (currType < 0) {
                        currType = Main.DB.regions.values().size() - 1;
                    }
                    currSelection = Main.DB.regions.get(currType).name;
                    currRegion = Main.DB.regions.get(currType).refName;
                } else if ("climates".equals(currTexture)) {
                    if (currType < 0) {
                        currType = Main.DB.climates.size() - 1;

                    }
                    currSelection = Main.DB.climates.get(currType).name;
                    currClimate = Main.DB.climates.get(currType).refName;

                }
                if (!drawing) {
                    return;
                }
                System.err.println("Current draw selection: " + currTexture + " - " + currSelection + " - " + currType);

            } else if (name.equals("nextType") && !pressed) {
                currType++;
                String currSelection = "";
                if ("types".equals(currTexture)) {
                    if (currType >= Main.DB.genTiles.keySet().size()) {
                        currType = 0;
                    }
                    currSelection = Main.DB.genTiles.get(currType).name;

                } else if ("regions".equals(currTexture)) {
                    if (currType >= Main.DB.regions.size()) {
                        currType = 0;
                    }
                    currSelection = Main.DB.regions.get(currType).name;
                    currRegion = Main.DB.regions.get(currType).refName;

                } else if ("climates".equals(currTexture)) {
                    if (currType >= Main.DB.climates.size()) {
                        currType = 0;
                    }
                    currSelection = Main.DB.climates.get(currType).name;
                    currClimate = Main.DB.climates.get(currType).refName;
                }
                if (!drawing) {
                    return;
                }
                System.err.println("Current draw selection: " + currTexture + " - " + currSelection + " - " + currType);


            } else if (name.equals("cursor") && !pressed) {
                flyMode = !flyMode;
                game.camera.setEnabled(flyMode);
                game.getInputManager().setCursorVisible(!flyMode);
            } else if (name.equals("dump") && !pressed) {
                dumpImage();
            } else if (name.equals("show_grid") && !pressed) {

                matTerrainDev.setTexture("GridMap", game.worldMapState.map.gridImage);
                matTerrainDev.setFloat("GridMap_scale",
                        Math.min(game.worldMapState.map.width, game.worldMapState.map.height));

            }

        }
    };

    @Override
    public void initialize(AppStateManager stateManager, Application app) {
        this.initialize(stateManager, (Main) app);
    }

    public void initialize(AppStateManager stateManager, Main main) {
        game = main;

        game.getInputManager().addListener(analogListener, "mouse_left");



        game.getInputManager().addListener(actionListener, "texture_types");
        game.getInputManager().addListener(actionListener, "texture_regions");
        game.getInputManager().addListener(actionListener, "texture_climates");
        game.getInputManager().addListener(actionListener, "draw_mode");
        game.getInputManager().addListener(actionListener, "previousType");
        game.getInputManager().addListener(actionListener, "nextType");
        game.getInputManager().addListener(actionListener, "cursor");
        game.getInputManager().addListener(actionListener, "mouse_left");
        game.getInputManager().addListener(actionListener, "mouse_right");
        game.getInputManager().addListener(actionListener, "dump");
        game.getInputManager().addListener(actionListener, "show_grid");


        currRegion = ((Region) Main.DB.regions.values().toArray()[0]).refName;
        currClimate = ((Climate) Main.DB.climates.values().toArray()[0]).refName;

        initialized = true;
        
        updateTexture("climates");
        updateTexture("regions");

        // Create dev terrain material to display ground types, regions, climates etc.
        matTerrainDev = new Material(game.getAssetManager(), "materials/Unshaded.j3md");


    }

    // Overlays the terrain with the specific images for world map creation
    public void displayTerrainTexture(String t) {
        currTexture = t;
        if ("types".equals(t)) {
            game.worldMapState.map.terrain.setMaterial(game.worldMapState.map.matTerrain);
            return;
        }
        if ("regions".equals(t)) {
            matTerrainDev.setTexture("ColorMap", Main.DB.regionsTex);
        } else if ("climates".equals(t)) {
            matTerrainDev.setTexture("ColorMap", Main.DB.climatesTex);
        }
        game.worldMapState.map.terrain.setMaterial(matTerrainDev);

    }

    public void dumpImage() {

        if (!drawing) {
            return;
        }

        BufferedImage im = new BufferedImage(game.worldMapState.map.width,
                game.worldMapState.map.height, BufferedImage.TYPE_INT_RGB);

        if ("types".equals(currTexture)) {
            for (int z = 0; z < game.worldMapState.map.height; z++) {
                for (int x = 0; x < game.worldMapState.map.width; x++) {
                    WorldTile wt = game.worldMapState.map.worldTiles[x][z];
                    Vector3f col = Main.DB.genTiles.get(wt.groundType).color;
                    int color = ((0xff & 255) << 24) | ((0xff & (int) col.x) << 16) | ((0xff & (int) col.y) << 8) | (0xff & (int) col.z);
                    im.setRGB(x, z, color);
                }
            }
        }

        if ("regions".equals(currTexture)) {
            for (int z = 0; z < game.worldMapState.map.height; z++) {
                for (int x = 0; x < game.worldMapState.map.width; x++) {
                    Vector3f col = Main.DB.regions.get(
                            game.worldMapState.map.worldTiles[x][z].region).color;
                    int color = ((0xff & 255) << 24) | ((0xff & (int) col.x) << 16) | ((0xff & (int) col.y) << 8) | (0xff & (int) col.z);
                    im.setRGB(x, z, color);
                }
            }
        }

        if ("climates".equals(currTexture)) {
            for (int z = 0; z < game.worldMapState.map.height; z++) {
                for (int x = 0; x < game.worldMapState.map.width; x++) {
                    Vector3f col = Main.DB.climates.get(
                            game.worldMapState.map.worldTiles[x][z].climate).color;
                    int color = ((0xff & 255) << 24) | ((0xff & (int) col.x) << 16) | ((0xff & (int) col.y) << 8) | (0xff & (int) col.z);
                    im.setRGB(x, z, color);
                }
            }
        }
        try {
            ImageIO.write(im, "png", new File(currTexture + ".png"));
            logger.log(Level.WARNING, "{0} image dumped", currTexture);

        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Error while saving screenshot", ex);
        }

    }

    @Override
    public void update(float tpf) {
    }

    public void updateTexture(String mode) {

        if ("types".equals(mode)) {
            game.worldMapState.map.createKeyTextures();

        } else if ("regions".equals(mode)) {
            int w = game.worldMapState.map.width;
            int h = game.worldMapState.map.height;
            int size = w * h * 3;
            ByteBuffer buf0 = ByteBuffer.allocateDirect(size);

            for (int z = h - 1; z >= 0; z--) {

                for (int x = 0; x < w; x++) {

                    Vector3f col = Main.DB.regions.get(game.worldMapState.map.worldTiles[x][z].region).color;

                    buf0.put((byte) ((0xff & (int) col.x)));
                    buf0.put((byte) ((0xff & (int) col.y)));
                    buf0.put((byte) ((0xff & (int) col.z)));

                }
            }

            Main.DB.regionsTex = new Texture2D(new Image(Image.Format.RGB8, w, h, buf0));

        } else if ("climates".equals(mode)) {
            int w = game.worldMapState.map.width;
            int h = game.worldMapState.map.height;
            int size = w * h * 3;
            ByteBuffer buf0 = ByteBuffer.allocateDirect(size);

            for (int z = h - 1; z >= 0; z--) {

                for (int x = 0; x < w; x++) {

                    Vector3f col = Main.DB.climates.get(game.worldMapState.map.worldTiles[x][z].climate).color;

                    buf0.put((byte) ((0xff & (int) col.x)));
                    buf0.put((byte) ((0xff & (int) col.y)));
                    buf0.put((byte) ((0xff & (int) col.z)));

                }
            }

            Main.DB.climatesTex = new Texture2D(new Image(Image.Format.RGB8, w, h, buf0));

        }
    }
}
