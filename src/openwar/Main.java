/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package openwar;

import java.io.FileNotFoundException;
import javax.script.ScriptException;
import openwar.DB.XMLDataLoader;
import openwar.world.WorldMapAppState;
import com.jme3.asset.plugins.*;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.ScreenshotAppState;
import com.jme3.audio.AudioNode;
import com.jme3.bullet.BulletAppState;
import com.jme3.collision.CollisionResult;
import com.jme3.collision.CollisionResults;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseButtonTrigger;

import com.jme3.math.Ray;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.niftygui.NiftyJmeDisplay;
import com.jme3.scene.Spatial;
import com.jme3.system.AppSettings;
import de.lessvoid.nifty.Nifty;
import java.io.File;
import java.io.FileReader;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import openwar.DB.GameDatabase;

public class Main extends SimpleApplication {

    static public int version = 1;
    public String locatorRoot = "data" + File.separator;
    public ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(4);
    public Nifty nifty;
    public BulletAppState bulletState = new BulletAppState();
    public ScreenshotAppState screenshotState = new ScreenshotAppState();
    public WorldMapAppState worldMapState = new WorldMapAppState();
    public DevModeAppState debugState = new DevModeAppState();
    static public GameDatabase DB = new GameDatabase();
    public XMLDataLoader DataLoader;
    public ScriptEngine scriptEngine;

    public static void main(String[] args) {
        Main app = new Main();
        Logger.getLogger("").setLevel(Level.WARNING);

        app.setShowSettings(true);
        app.setSettings(new AppSettings(true));
        app.settings.setTitle("openwar    r" + version);
        app.settings.setFrameRate(30);
        app.start();


    }

    @Override
    public void simpleInitApp() {

        assetManager.registerLocator(locatorRoot, FileLocator.class.getName());

        ScriptEngineManager factory = new ScriptEngineManager();
        scriptEngine = factory.getEngineByName("groovy");
        Bindings bindings = scriptEngine.createBindings();
        bindings.put("game", this);
        scriptEngine.setBindings(bindings, ScriptContext.ENGINE_SCOPE);

        DataLoader = new XMLDataLoader(this);
        DataLoader.loadAll();
        
         try {
            scriptEngine.eval("onBuildingBuilt('humans','Berlin','irrigation',2)");
        } catch (Exception ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }


        //AudioNode music = new AudioNode(assetManager, "music/lol.ogg", true);
        // music.play();


        guiNode.detachAllChildren();
        NiftyJmeDisplay niftyDisplay = new NiftyJmeDisplay(
                assetManager, inputManager, audioRenderer, guiViewPort);
        nifty = niftyDisplay.getNifty();
        guiViewPort.addProcessor(niftyDisplay);

        this.stateManager.attach(debugState);


        this.stateManager.attach(bulletState);
        this.stateManager.attach(worldMapState);
        this.stateManager.attach(screenshotState);

        getFlyByCamera().setMoveSpeed(50);

        getInputManager().addMapping("ScreenShot", new KeyTrigger(KeyInput.KEY_P));

        getInputManager().addMapping("mouse_left", new MouseButtonTrigger(0));
        getInputManager().addMapping("mouse_right", new MouseButtonTrigger(1));

        getInputManager().addMapping("map_strafeup", new KeyTrigger(KeyInput.KEY_U));
        getInputManager().addMapping("map_strafedown", new KeyTrigger(KeyInput.KEY_J));
        getInputManager().addMapping("map_strafeleft", new KeyTrigger(KeyInput.KEY_H));
        getInputManager().addMapping("map_straferight", new KeyTrigger(KeyInput.KEY_K));

        getInputManager().addMapping("texture_types", new KeyTrigger(KeyInput.KEY_1));
        getInputManager().addMapping("texture_regions", new KeyTrigger(KeyInput.KEY_2));
        getInputManager().addMapping("texture_climates", new KeyTrigger(KeyInput.KEY_3));
        getInputManager().addMapping("draw_mode", new KeyTrigger(KeyInput.KEY_TAB));
        getInputManager().addMapping("previousType", new KeyTrigger(KeyInput.KEY_B));
        getInputManager().addMapping("nextType", new KeyTrigger(KeyInput.KEY_N));
        getInputManager().addMapping("cursor", new KeyTrigger(KeyInput.KEY_X));
        getInputManager().addMapping("dump", new KeyTrigger(KeyInput.KEY_RETURN));


        getInputManager().addMapping("show_grid", new KeyTrigger(KeyInput.KEY_G));



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
