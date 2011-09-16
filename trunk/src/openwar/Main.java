/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package openwar;

import openwar.DB.XMLDataLoader;
import openwar.world.WorldMapAppState;
import com.jme3.asset.plugins.*;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.ScreenshotAppState;
import com.jme3.audio.AudioNode;
import com.jme3.bullet.BulletAppState;
import com.jme3.collision.CollisionResult;
import com.jme3.collision.CollisionResults;

import com.jme3.math.Ray;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.niftygui.NiftyJmeDisplay;
import com.jme3.scene.Spatial;
import com.jme3.system.AppSettings;
import de.lessvoid.nifty.Nifty;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.logging.Level;
import java.util.logging.Logger;
import openwar.DB.GameDatabase;

public class Main extends SimpleApplication {

    ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(4);
    public Nifty nifty;
    public BulletAppState bulletState = new BulletAppState();
    public ScreenshotAppState screenshotState = new ScreenshotAppState();
    public WorldMapAppState worldMapState = new WorldMapAppState();
    public String locatorRoot = "data/";
    public GameDatabase DB = new GameDatabase();
    public XMLDataLoader DataLoader;

    public static void main(String[] args) {
        Main app = new Main();
        Logger.getLogger("").setLevel(Level.WARNING);

        app.setShowSettings(false);
        app.setSettings(new AppSettings(true));
        app.settings.setTitle("OpenWar");
        app.settings.setVSync(true);
        app.settings.setFrameRate(30);
        app.start();


    }

    @Override
    public void simpleInitApp() {

        assetManager.registerLocator(locatorRoot, FileLocator.class.getName());

        DataLoader = new XMLDataLoader(this);
        DataLoader.loadAll();
        
        //AudioNode music = new AudioNode(assetManager, "music/lol.ogg", true);
       // music.play();
        
        
        guiNode.detachAllChildren();
        NiftyJmeDisplay niftyDisplay =
                new NiftyJmeDisplay(assetManager, inputManager, audioRenderer, guiViewPort);
        nifty = niftyDisplay.getNifty();
        guiViewPort.addProcessor(niftyDisplay);

        this.stateManager.attach(bulletState);
        this.stateManager.attach(worldMapState);
        this.stateManager.attach(screenshotState);
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

    @Override
    public void simpleUpdate(float tpf) {
    }
}
