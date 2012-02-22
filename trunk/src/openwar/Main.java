/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package openwar;

import com.jme3.app.Application;
import java.io.UnsupportedEncodingException;
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
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseAxisTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.math.FastMath;

import com.jme3.math.Ray;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.niftygui.NiftyJmeDisplay;
import com.jme3.renderer.Camera;
import com.jme3.renderer.ViewPort;
import com.jme3.renderer.queue.RenderQueue.Bucket;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.Spatial.CullHint;
import com.jme3.scene.control.UpdateControl;
import com.jme3.system.AppSettings;
import de.lessvoid.nifty.Nifty;
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

import de.lessvoid.nifty.elements.Element;
import java.net.URLDecoder;
import java.util.concurrent.Callable;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;

public class Main extends Application {

    static public String version = "$Revision$";
    public String locatorRoot;
    public ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(4);
    public Nifty nifty;
    public BulletAppState bulletState = new BulletAppState();
    public ScreenshotAppState screenshotState = new ScreenshotAppState();
    public WorldMapAppState worldMapState = new WorldMapAppState();
    public GameLoaderAppState gameLoaderState = new GameLoaderAppState();
    public DevModeAppState debugState = new DevModeAppState();
    public AudioAppState audioState = new AudioAppState(this);
    public MenuAppState mainMenuState = new MenuAppState();
    static public GameDatabase DB = new GameDatabase();
    static public ScriptEngine scriptEngine;
    static public boolean devMode;
    static public boolean debugUI;
    private AppActionListener actionListener = new AppActionListener();
    public Node rootNode = new Node("Root Node"), guiNode = new Node("GUI Node");
    protected FlyByCamera camera;
    public boolean wishToQuit;
    private static final Logger logger = Logger.getLogger(Main.class.getName());
    public HashMap<String, String> hashedPopUpId;

    private String getJarFolder() {
        String path = Main.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        path = path.substring(0, path.lastIndexOf("/") + 1);
        try {
            return URLDecoder.decode(path, "UTF-8");
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public boolean readXMLConfig() {
        try {

            String rootPath = "";


            // TODO: This line has to be included for deploying
            //rootPath = getJarFolder(); 



            File config = new File(rootPath + "config.xml");
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document dom = db.parse(config.getCanonicalPath());
            org.w3c.dom.Element root = dom.getDocumentElement();
            org.w3c.dom.Element display = (org.w3c.dom.Element) root.getElementsByTagName("display").item(0);
            org.w3c.dom.Element module = (org.w3c.dom.Element) root.getElementsByTagName("module").item(0);
            org.w3c.dom.Element dev = (org.w3c.dom.Element) root.getElementsByTagName("dev").item(0);


            settings = new AppSettings(true);
            settings.setWidth(Integer.parseInt(display.getAttribute("x")));
            settings.setHeight(Integer.parseInt(display.getAttribute("y")));
            settings.setFullscreen(!Boolean.parseBoolean(display.getAttribute("windowed")));


            if (settings.isFullscreen() && System.getProperty("os.name").equals("Mac OS X")) {
                settings.setFrequency(0);
            }


            locatorRoot = rootPath + module.getAttribute("dir") + File.separator;

            devMode = Boolean.parseBoolean(dev.getAttribute("devMode"));
            debugUI = Boolean.parseBoolean(dev.getAttribute("debugUI"));

            if (devMode) {
                Logger.getLogger("").setLevel(Level.WARNING);
            }




        } catch (Exception ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }

        return true;
    }

    public static void main(String[] args) {

        Main app = new Main();
        Logger.getLogger("").setLevel(Level.SEVERE);


        if (!app.readXMLConfig()) {
            System.err.println("Cannot find config.xml. Aborting...");
            return;
        }

        app.settings.setTitle("openwar  " + version);
        app.settings.setFrameRate(30);
        app.settings.setVSync(true);


        app.start();

    }

    public Main() {
        super();
    }

    @Override
    public void start() {
        super.start();
    }

    @Override
    public void initialize() {
        super.initialize();

        assetManager.registerLocator(locatorRoot, FileLocator.class.getName());
        ScriptEngineManager factory = new ScriptEngineManager();
        scriptEngine = factory.getEngineByName("groovy");
        Bindings bindings = scriptEngine.createBindings();
        bindings.put("game", this);
        scriptEngine.setBindings(bindings, ScriptContext.ENGINE_SCOPE);



        NiftyJmeDisplay niftyDisplay = new NiftyJmeDisplay(
                assetManager, inputManager, audioRenderer, guiViewPort);
        nifty = niftyDisplay.getNifty();
        nifty.fromXml("ui/loading/ui.xml", "start", mainMenuState);

        rootNode.addControl(new UpdateControl());
        viewPort.attachScene(rootNode);
        guiViewPort.attachScene(guiNode);
        guiNode.setQueueBucket(Bucket.Gui);
        guiNode.setCullHint(CullHint.Never);

        Camera guiCam = new Camera(settings.getWidth(), settings.getHeight());
        ViewPort guiViewPort2 = renderManager.createPostView("guiNode Viewport", guiCam);
        guiViewPort2.addProcessor(niftyDisplay);
        guiViewPort2.setClearFlags(false, false, false);




        if (debugUI) {
            nifty.setDebugOptionPanelColors(true);
        }


        hashedPopUpId = new HashMap<String, String>();


        inputManager.clearMappings();
        camera = new FlyByCamera(cam);
        camera.registerWithInput(inputManager);
        camera.setMoveSpeed(35);

        getInputManager().addMapping("show_grid", new KeyTrigger(KeyInput.KEY_G));
        getInputManager().addMapping("quit_game", new KeyTrigger(KeyInput.KEY_ESCAPE));
        getInputManager().addListener(actionListener, "quit_game");

        getInputManager().addMapping("ScreenShot", new KeyTrigger(KeyInput.KEY_P));

        getInputManager().addMapping("mouse_left", new MouseButtonTrigger(0));
        getInputManager().addMapping("mouse_right", new MouseButtonTrigger(1));

        getInputManager().addMapping("map_scrollup", new MouseAxisTrigger(MouseInput.AXIS_WHEEL, false));
        getInputManager().addMapping("map_scrolldown", new MouseAxisTrigger(MouseInput.AXIS_WHEEL, true));
  getInputManager().addMapping("battle_strafeup", new MouseAxisTrigger(MouseInput.AXIS_WHEEL, false));
        getInputManager().addMapping("battle_strafedown", new MouseAxisTrigger(MouseInput.AXIS_WHEEL, true));


        getInputManager().addMapping("shift", new KeyTrigger(KeyInput.KEY_LSHIFT));
        getInputManager().addMapping("shift", new KeyTrigger(KeyInput.KEY_RSHIFT));
        getInputManager().addMapping("ctrl", new KeyTrigger(KeyInput.KEY_LCONTROL));
        getInputManager().addMapping("ctrl", new KeyTrigger(KeyInput.KEY_RCONTROL));
        getInputManager().addMapping("alt", new KeyTrigger(KeyInput.KEY_LMENU));
        getInputManager().addMapping("alt", new KeyTrigger(KeyInput.KEY_RMENU));
 
        getInputManager().setCursorVisible(true);
        camera.setEnabled(false);


        if (!devMode) {
            getInputManager().addMapping("map_strafeup", new KeyTrigger(KeyInput.KEY_W));
            getInputManager().addMapping("map_strafedown", new KeyTrigger(KeyInput.KEY_S));
            getInputManager().addMapping("map_strafeleft", new KeyTrigger(KeyInput.KEY_A));
            getInputManager().addMapping("map_straferight", new KeyTrigger(KeyInput.KEY_D));
            getInputManager().addMapping("battle_forward", new KeyTrigger(KeyInput.KEY_W));
            getInputManager().addMapping("battle_backward", new KeyTrigger(KeyInput.KEY_S));
            getInputManager().addMapping("battle_strafeleft", new KeyTrigger(KeyInput.KEY_A));
            getInputManager().addMapping("battle_straferight", new KeyTrigger(KeyInput.KEY_D));

        } else {



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
            getInputManager().addMapping("cursor", new KeyTrigger(KeyInput.KEY_C));
            getInputManager().addMapping("dump", new KeyTrigger(KeyInput.KEY_RETURN));

        }

       
        

    }

    public CollisionResult getPick(Ray r, Spatial s) {
        CollisionResults results = new CollisionResults();
        s.collideWith(r, results);
        if (results.size() > 0) {
            return results.getClosestCollision();
        }
        return null;
    }

    public CollisionResult getPick(Vector3f start, Vector3f end, Spatial s) {
        return getPick(new Ray(start, end.subtract(start).normalizeLocal()), s);
    }

    public CollisionResult getPick(Vector2f pos, Spatial s) {
        Vector3f t0 = cam.getWorldCoordinates(pos, 0f);
        Vector3f t1 = cam.getWorldCoordinates(pos, 1f);
        return getPick(t0, t1, s);
    }

    // Calculates a mouse pick with a spatial and returns nearest result or null
    public CollisionResult getMousePick(Spatial s) {
        return getPick(inputManager.getCursorPosition(), s);

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
                wishToQuit = true;

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

        if (wishToQuit) {
            stop();
        }


        if (gameLoaderState.status == GameLoaderAppState.Status.None) {
            stateManager.attach(gameLoaderState);

        }

    }

    public void playSound(String name) {
        AudioNode n = DB.soundNodes.get(name);
        if (n == null) {
            logger.log(Level.SEVERE, "Cannot find sound: {0}", name);
            return;
        }
        n.stop();
        n.play();
    }

    public void playMusic(String name) {
        AudioNode n = DB.musicNodes.get(name);
        if (n == null) {
            logger.log(Level.SEVERE, "Cannot find music: {0}", name);
            return;
        }
        n.play();

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
