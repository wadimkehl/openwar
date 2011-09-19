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
import com.jme3.material.Material;
import com.jme3.math.Vector3f;
import com.jme3.terrain.geomipmap.TerrainPatch;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import openwar.world.WorldTile;

/**
 *
 * @author kehl
 */
public class DevModeAppState extends AbstractAppState {

    Main game;
    String currTexture = "types";
    int currType;
    boolean drawing;
    Material matTerrainDev;
    private static final Logger logger = Logger.getLogger(DevModeAppState.class.getName());
    public ActionListener actionListener = new ActionListener() {

        @Override
        public void onAction(String name, boolean pressed, float tpf) {

            if (name.equals("mouse_left") && !pressed) {
                CollisionResult r = game.getNiftyMousePick(game.worldMapState.sceneNode);
                if (r == null) {
                    return;
                }

                Vector3f pt = r.getContactPoint();
                int x = (int) pt.x;
                int z = (int) pt.z;

                if (r.getGeometry() instanceof TerrainPatch) {

                    if (!drawing) {
                        return;
                    }
                    game.worldMapState.map.setWorldTile(x, z, currType);
                    game.worldMapState.map.createKeyTextures();
                    return;

                }



            } else if (name.equals("texture_types") && !pressed) {

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
                        currType = Main.DB.regions.size() - 1;
                    }
                    currSelection = Main.DB.regions.get(currType).name;
                } else if ("climates".equals(currTexture)) {
                    if (currType < 0) {
                        currType = Main.DB.climates.size() - 1;

                    }
                    currSelection = Main.DB.climates.get(currType).name;
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
                } else if ("climates".equals(currTexture)) {
                    if (currType >= Main.DB.climates.size()) {
                        currType = 0;
                    }
                    currSelection = Main.DB.climates.get(currType).name;
                }
                if (!drawing) {
                    return;
                }
                System.err.println("Current draw selection: " + currTexture + " - " + currSelection + " - " + currType);


            } else if (name.equals("cursor") && !pressed) {
                game.getInputManager().setCursorVisible(game.getFlyByCamera().isEnabled());
                game.getFlyByCamera().setEnabled(!game.getFlyByCamera().isEnabled());
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




        initialized = true;

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

        BufferedImage im = new BufferedImage(game.worldMapState.map.width,
                game.worldMapState.map.height, 1);

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
                    Vector3f col = Main.DB.hashedRegions.get(
                            game.worldMapState.map.worldTiles[x][z].region).color;
                    int color = ((0xff & 255) << 24) | ((0xff & (int) col.x) << 16) | ((0xff & (int) col.y) << 8) | (0xff & (int) col.z);
                    im.setRGB(x, z, color);
                }
            }
        }

        if ("climates".equals(currTexture)) {
            for (int z = 0; z < game.worldMapState.map.height; z++) {
                for (int x = 0; x < game.worldMapState.map.width; x++) {
                    Vector3f col = Main.DB.hashedClimates.get(
                            game.worldMapState.map.worldTiles[x][z].climate).color;
                    int color = ((0xff & 255) << 24) | ((0xff & (int) col.x) << 16) | ((0xff & (int) col.y) << 8) | (0xff & (int) col.z);
                    im.setRGB(x, z, color);
                }
            }
        }


        try {
            ImageIO.write(im, "png", new File(currTexture + "_dump.png"));
            logger.log(Level.WARNING, "{0} image dumped", currTexture);

        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Error while saving screenshot", ex);
        }
    }

    @Override
    public void update(float tpf) {
    }
}
