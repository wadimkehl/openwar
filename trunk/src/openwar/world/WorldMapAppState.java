/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package openwar.world;

import openwar.DB.Army;
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
import java.util.ArrayList;
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
    public WorldMapUI uiController;
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

                if (r.getGeometry() instanceof TerrainPatch || r.getGeometry().getName().equals("reachableArea")) {

                    if (Main.devMode) {
                        System.err.println(map.worldTiles[x][z]);
                    }

                    map.deselectAll();
                    return;

                }

                // TODO: BLENDER exports spatial into two cascaded nodes!
                Spatial s = (Spatial) r.getGeometry();
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
                if (r.getGeometry() instanceof TerrainPatch || r.getGeometry().getName().equals("reachableArea")) {
                    map.marchTo(map.selectedArmy, x, z);
                    return;
                }

                // TODO: BLENDER exports spatial into two cascaded nodes!
                //Spatial s = (Spatial) r.getGeometry().getParent().getParent();
                Spatial s = (Spatial) r.getGeometry();
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

            } else if (name.equals("show_grid") && !pressed) {
                map.showGrid(showGrid = !showGrid);
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


            Vector3f loc = game.getCamera().getLocation().clone();
            loc.x = ensureMinMax((int) loc.x, 0, map.width);
            loc.z = ensureMinMax((int) loc.z, (int) (map.height * 0.3f), (int) (map.height * 1.3f));
            game.getCamera().setLocation(loc);


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

        uiController = new WorldMapUI();
        game.nifty.fromXml("ui" + File.separator + "worldmap"
                + File.separator + "ui.xml", "start", uiController);
        uiController.game = game;

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
            game.forceQuit = true;
        }

        game.rootNode.attachChild(sceneNode);
        game.getCamera().lookAtDirection(new Vector3f(0f, -1f, -1f).normalizeLocal(), Vector3f.UNIT_Y);
        game.getCamera().getLocation().y = 15f;

        moveCameraTo(Main.DB.hashedSettlements.get(Main.DB.hashedFactions.get(Main.DB.playerFaction).capital));

        initialized = true;

        if (Main.devMode) {
            return;
        }

        game.doScript("playMusic('main_menu')");
    }

    @Override
    public void update(float tpf) {

        map.update(tpf);


    }

    public void moveCameraTo(Tile t) {
        moveCameraTo(t.x, t.z);
    }

    public void moveCameraTo(Spatial s) {
        moveCameraTo((int) s.getWorldTranslation().x, (int) s.getWorldTranslation().z);
    }

    public void moveCameraTo(Army a) {
        moveCameraTo(a.posX, a.posZ);
    }

    public void moveCameraTo(Settlement s) {
        moveCameraTo(s.posX, s.posZ);
    }

    public void moveCameraTo(int x, int z) {
        Vector3f l = game.getCamera().getLocation();
        Vector3f d = game.getCamera().getDirection();
        Vector3f goal = new Vector3f(x + 0.5f, 0f, z + 0.5f);
        game.getCamera().setLocation(goal.add(d.mult(l.y / d.y)));

    }

    public int ensureMinMax(int value, int min, int max) {
        return Math.min(max, Math.max(min, value));


    }

    public int battle(ArrayList<Army> a1, ArrayList<Army> a2) {
        int power1 = 0, power2 = 0;

        for (Army a : a1) {
            power1 += a.units.size();
        }
        for (Army a : a2) {
            power2 += a.units.size();
        }

        if (power1 > power2) {
            for (Army a : a2) {
                map.removeArmy(a);
            }
            return 1;
        }
        if (power1 < power2) {
            for (Army a : a1) {
                map.removeArmy(a);
            }
            return 2;
        } else {
            return 0;
        }
    }
}
