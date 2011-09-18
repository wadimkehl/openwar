/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package openwar;

import com.jme3.app.Application;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.collision.CollisionResult;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.math.Vector3f;
import com.jme3.terrain.geomipmap.TerrainPatch;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import openwar.DB.Region;
import openwar.world.WorldTile;

/**
 *
 * @author kehl
 */
public class DevModeAppState extends AbstractAppState {

    Main app;
    String currTexture = "types";
    int currType;
    boolean drawing;
    private static final Logger logger = Logger.getLogger(DevModeAppState.class.getName());
    public ActionListener actionListener = new ActionListener() {

        @Override
        public void onAction(String name, boolean pressed, float tpf) {

            if (name.equals("mouse_left") && !pressed) {
                CollisionResult r = app.getNiftyMousePick(app.worldMapState.sceneNode);
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
                    app.worldMapState.map.setWorldTile(x, z, currType);
                    app.worldMapState.map.createKeyTextures();
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
                System.err.println("Drawing: " + drawing);




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
                app.getInputManager().setCursorVisible(app.getFlyByCamera().isEnabled());
                app.getFlyByCamera().setEnabled(!app.getFlyByCamera().isEnabled());
            } else if (name.equals("dump") && !pressed) {
                dumpImage();
            }


        }
    };

    @Override
    public void initialize(AppStateManager stateManager, Application app) {
        this.initialize(stateManager, (Main) app);
    }

    public void initialize(AppStateManager stateManager, Main main) {
        app = main;

     

        app.getInputManager().addListener(actionListener, "texture_types");
        app.getInputManager().addListener(actionListener, "texture_regions");
        app.getInputManager().addListener(actionListener, "texture_climates");
        app.getInputManager().addListener(actionListener, "draw_mode");
        app.getInputManager().addListener(actionListener, "previousType");
        app.getInputManager().addListener(actionListener, "nextType");
        app.getInputManager().addListener(actionListener, "cursor");
        app.getInputManager().addListener(actionListener, "mouse_left");
        app.getInputManager().addListener(actionListener, "mouse_right");
        app.getInputManager().addListener(actionListener, "dump");



        initialized = true;


    }

    // Overlays the terrain with the specific images for world map creation
    public void displayTerrainTexture(String t) {
        currTexture = t;
        if ("types".equals(t)) {
            app.worldMapState.map.terrain.setMaterial(app.worldMapState.map.matTerrain);
            return;
        }
        if ("regions".equals(t)) {
            app.worldMapState.map.matTerrainDebug.setTexture("ColorMap", Main.DB.regionsTex);
        } else if ("climates".equals(t)) {
            app.worldMapState.map.matTerrainDebug.setTexture("ColorMap", Main.DB.climatesTex);
        }
        app.worldMapState.map.terrain.setMaterial(app.worldMapState.map.matTerrainDebug);

    }

    public void dumpImage() {

        BufferedImage im = new BufferedImage(app.worldMapState.map.width,
                app.worldMapState.map.height, 1);

        if ("types".equals(currTexture)) {
            for (int z = 0; z < app.worldMapState.map.height; z++) {
                for (int x = 0; x < app.worldMapState.map.width; x++) {
                    WorldTile wt = app.worldMapState.map.worldTiles[x][z];
                    Vector3f col = Main.DB.genTiles.get(wt.groundType).color;
                    int color = ((0xff & 255) << 24) | ((0xff & (int) col.x) << 16) | ((0xff & (int) col.y) << 8) | (0xff & (int) col.z);
                    im.setRGB(x, z, color);
                }
            }
        }

        if ("regions".equals(currTexture)) {
            for (int z = 0; z < app.worldMapState.map.height; z++) {
                for (int x = 0; x < app.worldMapState.map.width; x++) {
                   Vector3f col = Main.DB.hashedRegions.get(
                           app.worldMapState.map.worldTiles[x][z].region).color;
                    int color = ((0xff & 255) << 24) | ((0xff & (int) col.x) << 16) | ((0xff & (int) col.y) << 8) | (0xff & (int) col.z);
                    im.setRGB(x, z, color);
                }
            }
        }

        if ("climates".equals(currTexture)) {
            for (int z = 0; z < app.worldMapState.map.height; z++) {
                for (int x = 0; x < app.worldMapState.map.width; x++) {
                     Vector3f col = Main.DB.hashedClimates.get(
                           app.worldMapState.map.worldTiles[x][z].climate).color;
                    int color = ((0xff & 255) << 24) | ((0xff & (int) col.x) << 16) | ((0xff & (int) col.y) << 8) | (0xff & (int) col.z);
                    im.setRGB(x, z, color);
                }
            }
        }


        try {
            ImageIO.write(im, "png", new File(currTexture + "_dump.png"));
            logger.log(Level.WARNING, "Imaged dumped");

        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Error while saving screenshot", ex);
        }
    }

    @Override
    public void update(float tpf) {
    }
}
