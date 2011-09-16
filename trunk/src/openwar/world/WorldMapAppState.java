/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package openwar.world;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.collision.CollisionResult;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.AnalogListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.terrain.geomipmap.TerrainPatch;
import openwar.DB.Settlement;
import openwar.Main;

/**
 *
 * @author kehl
 */
public class WorldMapAppState extends AbstractAppState {

    Node sceneNode;
    boolean debug = false;
    WorldMap map;
    Main app;
    private ActionListener actionListener = new ActionListener() {

        @Override
        public void onAction(String name, boolean pressed, float tpf) {

            if (name.equals("mouse_left") && !pressed) {
                CollisionResult r = app.getNiftyMousePick(map.scene);
                if (r == null) {
                    return;
                }

                Vector3f pt = r.getContactPoint();
                int x = (int) (pt.x);
                int z = (int) (pt.z);

                if (r.getGeometry() instanceof TerrainPatch) {
                    System.out.println(map.worldTiles[x][z]);
                    map.deselectTiles();
                    map.selectTile(x, z);
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

                CollisionResult r = app.getNiftyMousePick(map.scene);
                if (r == null) {
                    return;
                }
                Vector3f pt = r.getContactPoint();
                int x = (int) (pt.x - pt.x / map.width);
                int z = (int) (pt.z - pt.z / map.height);


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

            } else if (name.equals("standard") && !pressed) {

                map.displayStandardMaterial();

            } else if (name.equals("l0") && !pressed) {

                map.displayDebugMaterial(0);

            } else if (name.equals("l1") && !pressed) {

                map.displayDebugMaterial(1);

            } else if (name.equals("l2") && !pressed) {

                map.displayDebugMaterial(2);

            } else if (name.equals("lol") && !pressed) {

                map.fadeSeason();

            } else if (name.equals("cursor") && !pressed) {
                app.getInputManager().setCursorVisible(app.getFlyByCamera().isEnabled());
                app.getFlyByCamera().setEnabled(!app.getFlyByCamera().isEnabled());

            }
        }
    };
    private AnalogListener analogListener = new AnalogListener() {

        @Override
        public void onAnalog(String name, float value, float tpf) {

            if (name.equals("map_strafeup")) {
                app.getCamera().setLocation(app.getCamera().getLocation().addLocal(0, 0, tpf * -50f));
            } else if (name.equals("map_strafedown")) {
                app.getCamera().setLocation(app.getCamera().getLocation().addLocal(0, 0, tpf * 50f));
            } else if (name.equals("map_strafeleft")) {
                app.getCamera().setLocation(app.getCamera().getLocation().addLocal(tpf * -50f, 0, 0));
            } else if (name.equals("map_straferight")) {
                app.getCamera().setLocation(app.getCamera().getLocation().addLocal(tpf * 50f, 0, 0));
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

        app = main;

        app.getFlyByCamera().setMoveSpeed(50);

        app.getInputManager().addMapping("ScreenShot", new KeyTrigger(KeyInput.KEY_P));
        app.getInputManager().addMapping("standard", new KeyTrigger(KeyInput.KEY_1));
        app.getInputManager().addMapping("l0", new KeyTrigger(KeyInput.KEY_2));
        app.getInputManager().addMapping("l1", new KeyTrigger(KeyInput.KEY_3));
        app.getInputManager().addMapping("l2", new KeyTrigger(KeyInput.KEY_4));
        app.getInputManager().addMapping("mouse_left", new MouseButtonTrigger(0));
        app.getInputManager().addMapping("mouse_right", new MouseButtonTrigger(1));
        app.getInputManager().addMapping("cursor", new KeyTrigger(KeyInput.KEY_X));

        app.getInputManager().addMapping("map_strafeup", new KeyTrigger(KeyInput.KEY_U));
        app.getInputManager().addMapping("map_strafedown", new KeyTrigger(KeyInput.KEY_J));
        app.getInputManager().addMapping("map_strafeleft", new KeyTrigger(KeyInput.KEY_H));
        app.getInputManager().addMapping("map_straferight", new KeyTrigger(KeyInput.KEY_K));

        app.getInputManager().addMapping("lol", new KeyTrigger(KeyInput.KEY_0));


        app.getInputManager().addListener(actionListener, "standard");
        app.getInputManager().addListener(actionListener, "mouse_left");
        app.getInputManager().addListener(actionListener, "mouse_right");
        app.getInputManager().addListener(actionListener, "cursor");
        app.getInputManager().addListener(actionListener, "l0");
        app.getInputManager().addListener(actionListener, "l1");
        app.getInputManager().addListener(actionListener, "l2");
        app.getInputManager().addListener(actionListener, "lol");


        app.getInputManager().addListener(analogListener, "map_strafeup");
        app.getInputManager().addListener(analogListener, "map_strafedown");
        app.getInputManager().addListener(analogListener, "map_strafeleft");
        app.getInputManager().addListener(analogListener, "map_straferight");

        sceneNode = new Node("WorldMap");
        map = new WorldMap(main, sceneNode);
        if (!map.createWorldMap()) {
            app.stop();
        }


//            audioRenderer.playSource(new AudioNode(assetManager, "music/lol.ogg", false));

        app.getRootNode().attachChild(sceneNode);
        app.getCamera().setLocation(new Vector3f(map.width / 2, 15, map.height / 2));
        app.getCamera().lookAtDirection(new Vector3f(0f, -.9f, -1f).normalizeLocal(), Vector3f.UNIT_Y);

        app.nifty.fromXml("ui/worldmap/worldmap.xml", "start");

        initialized = true;
    }

    @Override
    public void update(float tpf) {

        map.update(tpf);

    }
}
