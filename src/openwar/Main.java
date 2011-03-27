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
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.system.AppSettings;

public class Main extends SimpleApplication {

    public BulletAppState bulletState = new BulletAppState();
    public ScreenshotAppState screenshotState = new ScreenshotAppState();
    public AppState worldMapState = new AbstractAppState() {

        Application app;
        Node sceneNode;
        boolean grid = true;
        WorldMap map;
        private ActionListener actionListener = new ActionListener() {

            public void onAction(String name, boolean pressed, float tpf) {

                if (name.equals("mouse_left") && !pressed) {
                    CollisionResults results = new CollisionResults();
                    Ray ray = new Ray(cam.getLocation(), cam.getDirection());
                    map.scene.collideWith(ray, results);
                    if (results.size() > 0) {
                        CollisionResult r = results.getClosestCollision();
                        Vector3f pt = r.getContactPoint();
                        int x = (int) Math.floor(pt.x - 0.5f);
                        int z = (int) Math.floor(pt.z - 0.5f);
                        x = 255 - x;
                        z = 255 - z;

                        if (r.getGeometry() == null) {
                            System.out.print("Terrain tile: (");
                            System.out.print(x);
                            System.out.print(",");
                            System.out.print(z);
                            System.out.print(")  Type: ");
                            System.out.println(GroundTypeManager.getGroundTypeString(map.worldTiles[x][z].groundType));
                            map.deselectTiles();

                        } else {
                            WorldArmy a = map.getArmy((Spatial) r.getGeometry().getParent());
                            a.control.jump();
                            map.selectArmy(a);
                            

                            
                        }
                    }
                }

                if (name.equals("mouse_right") && !pressed) {
                    CollisionResults results = new CollisionResults();
                    Ray ray = new Ray(cam.getLocation(), cam.getDirection());
                    map.scene.collideWith(ray, results);
                    if (results.size() > 0) {
                        CollisionResult r = results.getClosestCollision();
                        Vector3f pt = r.getContactPoint();
                        int x = (int) Math.floor(pt.x - 0.5f);
                        int z = (int) Math.floor(pt.z - 0.5f);
                        x = 255 - x;
                       
                    }
                }

                if (name.equals("grid") && !pressed) {
                    grid = !grid;
                    map.material.setBoolean("useGrid", grid);
                    map.deselectTiles();
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
            inputManager.addListener(actionListener, "grid");
            inputManager.addListener(actionListener, "mouse_left");
            inputManager.addListener(actionListener, "mouse_right");

            sceneNode = new Node("WorldMap");
            assetManager.registerLocator("data/", FileLocator.class.getName());
            map = new WorldMap(app, assetManager, bulletState, sceneNode);
            if (map.create()) {
                map.material.setBoolean("useGrid", grid);


            } else {
                app.stop();

            }

            map.createArmy(128, 128, 0);
            map.createArmy(84, 157, 0);
            map.createArmy(112, 26, 0);

            audioRenderer.playSource(new AudioNode(assetManager, "music/lol.ogg", false));

            rootNode.attachChild(sceneNode);
            getCamera().getLocation().x = 128;
            getCamera().getLocation().y = 9;
            getCamera().getLocation().z = 108;
            getCamera().setDirection(new Vector3f(0f, -.05f, 1f));
            initialized = true;
        }

        @Override
        public void update(float tpf) {

            map.update();

        }
    };

    public static void main(String[] args) {
        Main app = new Main();

        app.setShowSettings(false);
        app.setSettings(new AppSettings(true));
        app.settings.setTitle("OpenWar");
        app.settings.setFrameRate(30);
        app.start();




    }

    @Override
    public void simpleInitApp() {

        this.stateManager.attach(bulletState);
        this.stateManager.attach(worldMapState);
        this.stateManager.attach(screenshotState);



    }
}
