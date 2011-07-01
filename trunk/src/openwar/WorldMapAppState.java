/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package openwar;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.collision.CollisionResult;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.terrain.geomipmap.TerrainPatch;

/**
 *
 * @author kehl
 */
public class WorldMapAppState extends AbstractAppState {

    Node sceneNode;
    boolean grid = true;
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
                int x = (int) (pt.x - pt.x / map.width);
                int z = (int) (pt.z - pt.z / map.height);

                if (r.getGeometry() instanceof TerrainPatch) {
                    System.out.print("Terrain tile: (");
                    System.out.print(x);
                    System.out.print(",");
                    System.out.print(z);
                    System.out.print(")  Type: ");
                    System.out.println(GroundTypeManager.getGroundTypeString(map.worldTiles[x][z].groundType));
                    map.deselectTiles();
                    map.selectTile(x, z);
                    return;

                }

                WorldArmy a = map.getArmy((Spatial) r.getGeometry().getParent());
                if (a != null) {
                    map.selectArmy(a);
                    return;
                }

                WorldCity c = map.getCity((Spatial) r.getGeometry().getParent());
                if (c != null) {
                    map.selectCity(c);
                    return;
                }



            }



            if (name.equals("mouse_right") && !pressed) {

                CollisionResult r = app.getNiftyMousePick(map.scene);
                if (r == null) {
                    return;
                }
                Vector3f pt = r.getContactPoint();
                int x = (int) (pt.x - pt.x / map.width);
                int z = (int) (pt.z - pt.z / map.height);


                // Check if we ordered a march command
                if (map.selectedArmy != null) {

                    if (r.getGeometry() instanceof TerrainPatch) {
                        map.marchTo(map.selectedArmy, x, z);
                        return;
                    }

                    WorldArmy a = map.getArmy((Spatial) r.getGeometry().getParent());
                    if (a != null) {
                        map.marchTo(map.selectedArmy, a);
                        return;
                    }

                    WorldCity c = map.getCity((Spatial) r.getGeometry().getParent());
                    if (c != null) {
                        map.marchTo(map.selectedArmy, c);
                        return;
                    }
                }
            }


            if (name.equals("grid") && !pressed) {
                grid = !grid;
                map.matTerrain.setBoolean("useGrid", grid);
                map.deselectTiles();
            }

            if (name.equals("cursor") && !pressed) {
                app.getInputManager().setCursorVisible(app.getFlyByCamera().isEnabled());
                app.getFlyByCamera().setEnabled(!app.getFlyByCamera().isEnabled());

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
        app.getInputManager().addMapping("grid", new KeyTrigger(KeyInput.KEY_G));
        app.getInputManager().addMapping("mouse_left", new MouseButtonTrigger(0));
        app.getInputManager().addMapping("mouse_right", new MouseButtonTrigger(1));
        app.getInputManager().addMapping("cursor", new KeyTrigger(KeyInput.KEY_X));
        app.getInputManager().addListener(actionListener, "grid");
        app.getInputManager().addListener(actionListener, "mouse_left");
        app.getInputManager().addListener(actionListener, "mouse_right");
        app.getInputManager().addListener(actionListener, "cursor");

        sceneNode = new Node("WorldMap");
        map = new WorldMap(main, main.getAssetManager(), main.bulletState, sceneNode);
        if (!map.create()) {
            app.stop();
        }

        map.matTerrain.setBoolean("useGrid", grid);

        map.createArmy(0, 0, 0);
        map.createArmy(16, 16, 0);

        map.createArmy(31, 31, 0);
        map.createArmy(125, 190, 0);

        map.createArmy(128, 128, 0);
        map.createArmy(84, 157, 0);
        map.createArmy(112, 26, 0);
        map.createArmy(254, 254, 0);

        map.createCity(128, 130, 0);


//            audioRenderer.playSource(new AudioNode(assetManager, "music/lol.ogg", false));

        app.getRootNode().attachChild(sceneNode);
        app.getCamera().setLocation(new Vector3f(map.width / 2, 9, map.height / 2));
        app.getCamera().lookAtDirection(new Vector3f(0f, -.9f, -1f).normalizeLocal(), Vector3f.UNIT_Y);

        app.nifty.fromXml("ui/worldmap/worldmap.xml", "start");

        initialized = true;
    }

    @Override
    public void update(float tpf) {

        map.update(tpf);

    }
}
