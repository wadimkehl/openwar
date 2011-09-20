/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package openwar.world;

import com.jme3.app.Application;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.collision.CollisionResult;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.AnalogListener;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.terrain.geomipmap.TerrainPatch;
import java.io.File;
import openwar.DB.Settlement;
import openwar.Main;

/**
 *
 * @author kehl
 */
public class WorldMapAppState extends AbstractAppState {

    public Node sceneNode;
    public boolean showGrid = false;
    public WorldMap map;
    public Main game;
    public WorldMapUI controller;
    public ActionListener actionListener = new ActionListener() {

        @Override
        public void onAction(String name, boolean pressed, float tpf) {

            if (name.equals("mouse_left") && !pressed) {
                CollisionResult r = game.getMousePick(map.scene);
                if (r == null) {
                    return;
                }

                Vector3f pt = r.getContactPoint();
                int x = (int) pt.x;
                int z = (int) pt.z;

                if (r.getGeometry() instanceof TerrainPatch) {

                    if (Main.devMode) {
                        System.err.println(map.worldTiles[x][z]);
                    }
                    
                    map.deselectAll();
                    return;

                }

                // TODO: BLENDER exports spatial into two cascaded nodes!
                Spatial s = (Spatial) r.getGeometry().getParent().getParent();
                Army a = map.getArmy(s);
                if (a != null) {
                    map.selectArmy(a);
                    return;
                }

                Settlement c = map.getSettlement(s);
                if (c != null) {
                    map.selectSettlement(c);
                    return;
                }



            } else if (name.equals("mouse_right") && !pressed) {

                if (map.selectedArmy == null) {
                    return;
                }

                CollisionResult r = game.getMousePick(map.scene);
                if (r == null) {
                    return;
                }
                Vector3f pt = r.getContactPoint();
                int x = (int) pt.x;
                int z = (int) pt.z;


                // Check if we ordered a march command
                if (r.getGeometry() instanceof TerrainPatch) {
                    map.marchTo(map.selectedArmy, x, z);
                    return;
                }

                // TODO: BLENDER exports spatial into two cascaded nodes!
                Spatial s = (Spatial) r.getGeometry().getParent().getParent();
                Army a = map.getArmy(s);
                if (a != null) {
                    map.marchTo(map.selectedArmy, a);
                    return;
                }

                Settlement c = map.getSettlement(s);
                if (c != null) {
                    map.marchTo(map.selectedArmy, c);
                    return;
                }

            } else if (name.equals("lol") && !pressed) {
                map.fadeSeason();

            } else if (name.equals("show_grid") && !pressed) {
                showGrid = !showGrid;
                map.showGrid(showGrid);
            }
        }
    };
    private AnalogListener analogListener = new AnalogListener() {

        @Override
        public void onAnalog(String name, float value, float tpf) {

            if (name.equals("map_strafeup")) {
                game.getCamera().setLocation(game.getCamera().getLocation().addLocal(0, 0, tpf * -50f));
            } else if (name.equals("map_strafedown")) {
                game.getCamera().setLocation(game.getCamera().getLocation().addLocal(0, 0, tpf * 50f));
            } else if (name.equals("map_strafeleft")) {
                game.getCamera().setLocation(game.getCamera().getLocation().addLocal(tpf * -50f, 0, 0));
            } else if (name.equals("map_straferight")) {
                game.getCamera().setLocation(game.getCamera().getLocation().addLocal(tpf * 50f, 0, 0));
            }
        }
    };

    public WorldMapAppState() {
    }

    @Override
    public void initialize(AppStateManager stateManager, Application app) {
        this.initialize(stateManager, (Main) app);
    }

    public void initialize(AppStateManager stateManager, Main main) {

        game = main;

        controller = new WorldMapUI();
        game.nifty.fromXml("ui" + File.separator + "worldmap" 
                + File.separator + "ui.xml", "start",controller);
        controller.game = game;

        game.getInputManager().addListener(actionListener, "mouse_left");
        game.getInputManager().addListener(actionListener, "mouse_right");
        game.getInputManager().addListener(actionListener, "show_grid");


        game.getInputManager().addListener(analogListener, "map_strafeup");
        game.getInputManager().addListener(analogListener, "map_strafedown");
        game.getInputManager().addListener(analogListener, "map_strafeleft");
        game.getInputManager().addListener(analogListener, "map_straferight");

        sceneNode = new Node("WorldMap");
        map = new WorldMap(main, sceneNode);
        if (!map.createWorldMap()) {
            game.stop();
        }

        game.getRootNode().attachChild(sceneNode);
        game.getCamera().setLocation(new Vector3f(map.width / 2, 15, map.height / 2));
        game.getCamera().lookAtDirection(new Vector3f(0f, -.9f, -1f).normalizeLocal(), Vector3f.UNIT_Y);

        initialized = true;

        //game.doScript("playMusic('ambient1')");
    }

    @Override
    public void update(float tpf) {

        map.update(tpf);


        if (Main.devMode) {
            return;
        }

        Vector3f loc = game.getCamera().getLocation();
        loc.x = Math.max(Math.min(map.width, loc.x), 0);
        loc.z = Math.max(Math.min(map.height + map.height / 3, loc.z), map.height / 3);

    }
}
