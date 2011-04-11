/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package openwar;

import com.jme3.app.Application;
import com.jme3.app.state.AppStateManager;
import com.jme3.asset.plugins.*;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppState;
import com.jme3.app.state.ScreenshotAppState;
import com.jme3.audio.AudioNode;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.collision.CollisionResult;
import com.jme3.collision.CollisionResults;
import com.jme3.font.BitmapText;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseButtonTrigger;

import com.jme3.math.Ray;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.niftygui.NiftyJmeDisplay;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.system.AppSettings;
import com.jme3.terrain.geomipmap.TerrainPatch;
import de.lessvoid.nifty.Nifty;
import java.util.concurrent.ScheduledThreadPoolExecutor;

public class Main extends SimpleApplication {
    
    ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(4);
    public Nifty nifty;
    public BulletAppState bulletState = new BulletAppState();
    public ScreenshotAppState screenshotState = new ScreenshotAppState();
    public AppState worldMapState = new AbstractAppState() {

        Application app;
        Node sceneNode;
        boolean grid = true;
        WorldMap map;
        private ActionListener actionListener = new ActionListener() {

            @Override
            public void onAction(String name, boolean pressed, float tpf) {

                if (name.equals("mouse_left") && !pressed) {
                    CollisionResult r = getNiftyMousePick(map.scene);
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

                    CollisionResult r = getNiftyMousePick(map.scene);
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


                if (name.equals(
                        "grid") && !pressed) {
                    grid = !grid;
                    map.matTerrain.setBoolean("useGrid", grid);
                    map.deselectTiles();
                }

                if (name.equals(
                        "cursor") && !pressed) {
                    app.getInputManager().setCursorVisible(flyCam.isEnabled());
                    flyCam.setEnabled(!flyCam.isEnabled());

                }
            }
        };

        @Override
        public void initialize(AppStateManager stateManager, Application app) {

            this.app = app;

            flyCam.setMoveSpeed(50);

            inputManager.addMapping("ScreenShot", new KeyTrigger(KeyInput.KEY_P));
            inputManager.addMapping("grid", new KeyTrigger(KeyInput.KEY_G));
            inputManager.addMapping("mouse_left", new MouseButtonTrigger(0));
            inputManager.addMapping("mouse_right", new MouseButtonTrigger(1));
            inputManager.addMapping("cursor", new KeyTrigger(KeyInput.KEY_X));
            inputManager.addListener(actionListener, "grid");
            inputManager.addListener(actionListener, "mouse_left");
            inputManager.addListener(actionListener, "mouse_right");
            inputManager.addListener(actionListener, "cursor");

            sceneNode = new Node("WorldMap");
            map = new WorldMap(app, assetManager, bulletState, sceneNode);
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
            
            map.createCity(128,130, 0);


//            audioRenderer.playSource(new AudioNode(assetManager, "music/lol.ogg", false));

            rootNode.attachChild(sceneNode);
            getCamera().setLocation(new Vector3f(map.width / 2, 9, map.height / 2));
            getCamera().setDirection(new Vector3f(0f, -.9f, -1f).normalizeLocal());

            nifty.fromXml("data/ui/worldmap/worldmap.xml", "start");

            initialized = true;
        }

        @Override
        public void update(float tpf) {

            map.update(tpf);

        }
    };

    public static void main(String[] args) {
        Main app = new Main();


        app.setShowSettings(true);
        app.setSettings(new AppSettings(true));
        app.settings.setTitle("OpenWar");
        app.settings.setVSync(true);

        app.settings.setFrameRate(30);

        app.start();


    }

    @Override
    public void simpleInitApp() {

        assetManager.registerLocator("data/", FileLocator.class.getName());
        this.stateManager.attach(bulletState);
        this.stateManager.attach(worldMapState);
        this.stateManager.attach(screenshotState);

        guiNode.detachAllChildren();
        NiftyJmeDisplay niftyDisplay =
                new NiftyJmeDisplay(assetManager, inputManager, audioRenderer, guiViewPort);
        nifty = niftyDisplay.getNifty();

        guiViewPort.addProcessor(niftyDisplay);
    }

// Calculates a mouse pick with a spatial and returns nearest result or null
    public CollisionResult getNiftyMousePick(Spatial s) {

        int x = nifty.getNiftyMouse().getX();
        int y = cam.getHeight() - nifty.getNiftyMouse().getY();
        Vector2f mouse = new Vector2f(x, y);
        Vector3f t0 = cam.getWorldCoordinates(mouse, 0f);
        Vector3f t1 = cam.getWorldCoordinates(mouse, 1f);
        CollisionResults results = new CollisionResults();
        Ray ray = new Ray(t0, t1.subtractLocal(t0).normalizeLocal());

        s.collideWith(ray, results);


        if (results.size() > 0) {
            return results.getClosestCollision();
        }

        return null;
    }
}
