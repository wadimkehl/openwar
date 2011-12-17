/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package openwar;

import com.jme3.app.Application;
import javax.script.ScriptException;
import openwar.DB.XMLDataLoader;
import openwar.world.WorldMapAppState;
import com.jme3.asset.plugins.*;
import com.jme3.app.state.ScreenshotAppState;
import com.jme3.audio.AudioNode;
import com.jme3.bullet.BulletAppState;
import com.jme3.collision.CollisionResult;
import com.jme3.collision.CollisionResults;
import com.jme3.input.FlyByCamera;
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
import com.jme3.system.JmeSystem;
import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.elements.Element;
import java.io.File;
import java.util.HashMap;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import openwar.DB.GameDatabase;

public class Main extends Application {

    static public int version = 4;
    public String locatorRoot = "data" + File.separator;
    public ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(4);
    public Nifty nifty;
    public BulletAppState bulletState = new BulletAppState();
    public ScreenshotAppState screenshotState = new ScreenshotAppState();
    public WorldMapAppState worldMapState = new WorldMapAppState();
    public DevModeAppState debugState = new DevModeAppState();
    static public GameDatabase DB = new GameDatabase();
    public XMLDataLoader DataLoader;
    static public ScriptEngine scriptEngine;
    static public boolean devMode;
    static public boolean debugUI;
    private AppActionListener actionListener = new AppActionListener();
    public Node rootNode = new Node("Root Node"), guiNode = new Node("GUI Node");
    protected FlyByCamera camera;
    public boolean forceQuit;
    private static final Logger logger = Logger.getLogger(Main.class.getName());
    public HashMap<String, String> hashedPopUpId;

    @Override
    public void start() {
        // set some default settings in-case
        // settings dialog is not shown
        boolean loadSettings = false;
        if (settings == null) {
            setSettings(new AppSettings(true));
            loadSettings = true;
        }
        // show settings dialog
        if (true) {
            if (!JmeSystem.showSettingsDialog(settings, loadSettings)) {
                return;
            }
        }
        //re-setting settings they can have been merged from the registry.
        setSettings(settings);
        super.start();
    }

    public static void main(String[] args) {



        Main app = new Main();
        Logger.getLogger("").setLevel(Level.SEVERE);

        app.setSettings(new AppSettings(true));
        app.settings.setTitle("openwar    r" + version);
        app.settings.setFrameRate(30);


        for (String s : args) {
            if ("--dev".equals(s)) {
                Logger.getLogger("").setLevel(Level.WARNING);
                devMode = true;
            }
            if ("--debug-ui".equals(s)) {
                debugUI = true;
            }
        }

        app.start();


    }

    public Main() {
        super();
    }

    @Override
    public void initialize() {
        super.initialize();
        viewPort.attachScene(rootNode);
        guiViewPort.attachScene(guiNode);
        assetManager.registerLocator(locatorRoot, FileLocator.class.getName());

        ScriptEngineManager factory = new ScriptEngineManager();
        scriptEngine = factory.getEngineByName("groovy");
        Bindings bindings = scriptEngine.createBindings();
        bindings.put("game", this);
        scriptEngine.setBindings(bindings, ScriptContext.ENGINE_SCOPE);

        DataLoader = new XMLDataLoader(this);
        if (!DataLoader.loadAll()) {
            forceQuit = true;
            return;
        }
        setMusicVolume(0.2f);

        NiftyJmeDisplay niftyDisplay = new NiftyJmeDisplay(
                assetManager, inputManager, audioRenderer, guiViewPort);
        nifty = niftyDisplay.getNifty();
        guiViewPort.addProcessor(niftyDisplay);


        if (devMode) {
            this.stateManager.attach(debugState);
        }

        if (debugUI) {
            nifty.setDebugOptionPanelColors(true);
        }


        this.stateManager.attach(bulletState);
        this.stateManager.attach(worldMapState);
        this.stateManager.attach(screenshotState);


        camera = new FlyByCamera(cam);
        camera.registerWithInput(inputManager);
        camera.setMoveSpeed(35);

        if (!devMode) {
            inputManager.clearMappings();
            getInputManager().addMapping("map_strafeup", new KeyTrigger(KeyInput.KEY_W));
            getInputManager().addMapping("map_strafedown", new KeyTrigger(KeyInput.KEY_S));
            getInputManager().addMapping("map_strafeleft", new KeyTrigger(KeyInput.KEY_A));
            getInputManager().addMapping("map_straferight", new KeyTrigger(KeyInput.KEY_D));

        }


        hashedPopUpId = new HashMap<String, String>();

        //doScript("onGameBegin()");

        getInputManager().addMapping("quit_game", new KeyTrigger(KeyInput.KEY_ESCAPE));
        getInputManager().addListener(actionListener, "quit_game");

        getInputManager().addMapping("ScreenShot", new KeyTrigger(KeyInput.KEY_P));

        getInputManager().addMapping("mouse_left", new MouseButtonTrigger(0));
        getInputManager().addMapping("mouse_right", new MouseButtonTrigger(1));

        getInputManager().addMapping("shift", new KeyTrigger(KeyInput.KEY_LSHIFT));
        getInputManager().addMapping("shift", new KeyTrigger(KeyInput.KEY_RSHIFT));
        getInputManager().addMapping("ctrl", new KeyTrigger(KeyInput.KEY_LCONTROL));
        getInputManager().addMapping("ctrl", new KeyTrigger(KeyInput.KEY_RCONTROL));



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


        getInputManager().setCursorVisible(true);
        camera.setEnabled(false);



    }

// Calculates a mouse pick with a spatial and returns nearest result or null
    public CollisionResult getMousePick(Spatial s) {

        Vector2f mouse = inputManager.getCursorPosition();
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

    public Object doReturnScript(String line) {
        try {
            return scriptEngine.eval(line);
        } catch (ScriptException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public void doScript(String line) {
        try {
            scriptEngine.eval(line);
        } catch (ScriptException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private class AppActionListener implements ActionListener {

        @Override
        public void onAction(String name, boolean value, float tpf) {
            if (!value) {
                return;
            }

            if (name.equals("quit_game")) {
                forceQuit = true;

            }
        }
    }

    @Override
    public void update() {
        super.update();
        if (speed == 0 || paused) {
            return;
        }

        float tpf = timer.getTimePerFrame() * speed;
        stateManager.update(tpf);
        rootNode.updateLogicalState(tpf);
        rootNode.updateGeometricState();
        guiNode.updateLogicalState(tpf);
        guiNode.updateGeometricState();
        stateManager.render(renderManager);
        renderManager.render(tpf, context.isRenderable());
        stateManager.postRender();

        if (forceQuit) {
            stop();
        }
    }

    public void playSound(String name) {
        AudioNode n = DB.soundNodes.get(name);
        if (n == null) {
            logger.log(Level.SEVERE, "Cannot find sound: {0}", name);
            return;
        }
        if (n.getStatus() != AudioNode.Status.Playing) {
            n.play();
        }
    }

    public void playMusic(String name) {
        AudioNode n = DB.musicNodes.get(name);
        if (n == null) {
            logger.log(Level.SEVERE, "Cannot find music: {0}", name);
            return;
        }
        n.play();

    }

    public void setMusicVolume(float v) {
        for (AudioNode n : DB.musicNodes.values()) {
            n.setVolume(v);
        }
    }

    public void setSoundVolume(float v) {
        for (AudioNode n : DB.soundNodes.values()) {
            n.setVolume(v);
        }
    }

    public void changeUIScreen(String name) {

        if (nifty.getScreen(name) == null) {
            logger.log(Level.SEVERE, "Cannot find ui screen: {0}", name);
            return;
        }
        nifty.gotoScreen(name);
    }

    public void showUIElement(String name, boolean show) {
        Element element = nifty.getCurrentScreen().findElementByName(name);
        if (element == null) {
            logger.log(Level.SEVERE, "Cannot find ui element: {0}", name);
            return;
        }
        element.setVisible(show);
        return;
    }

    public void toggleUIElement(String name) {
        Element element = nifty.getCurrentScreen().findElementByName(name);
        if (element == null) {
            logger.log(Level.SEVERE, "Cannot find ui element: {0}", name);
            return;
        }
        element.setVisible(!element.isVisible());
        return;
    }

    public void showUIPopUp(String name) {
        String element = nifty.createPopup(name).getId();
        if (element == null) {
            logger.log(Level.SEVERE, "Cannot find popup template: {0}", name);
            return;
        }
        nifty.showPopup(nifty.getCurrentScreen(), element, null);
        hashedPopUpId.put(name, element);
    }

    public void closeUIPopUp(String name) {
        if (name == null || hashedPopUpId.get(name) == null) {
            logger.log(Level.SEVERE, "Cannot find popup with id: {0}", name);
            return;
        }
        nifty.closePopup(hashedPopUpId.get(name));
    }
}
