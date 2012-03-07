/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package openwar;

import com.jme3.app.Application;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.bullet.BulletAppState.ThreadingType;
import com.jme3.bullet.PhysicsSpace.BroadphaseType;
import com.jme3.math.Vector3f;
import com.jme3.scene.control.UpdateControl;
import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.screen.Screen;
import de.lessvoid.nifty.screen.ScreenController;
import java.util.concurrent.Callable;
import openwar.DB.XMLDataLoader;
import openwar.battle.BattleAppState;

/**
 *
 * @author kehl
 */
public class GameLoaderAppState extends AbstractAppState implements ScreenController {

    public enum Status {

        None,
        Init,
        MainMenu,
        Idle,
        LoadingWorldMap,
        LoadingBattle
    }
    Main game;
    AppStateManager manager;
    Status status;
    Nifty nifty;
    Screen screen;
    public XMLDataLoader DataLoader;
    Object loadObject;

    public GameLoaderAppState() {
        status = Status.None;
    }

    @Override
    public void bind(Nifty n, Screen s) {
        nifty = n;
        screen = s;
    }

    @Override
    public void onStartScreen() {
    }

    @Override
    public void onEndScreen() {
    }

    @Override
    public void initialize(AppStateManager stateManager, Application app) {
        this.initialize(stateManager, (Main) app);
    }

    public void initialize(AppStateManager stateManager, Main main) {
        game = main;
        manager = stateManager;
        status = Status.Init;
        initialized = true;



    }

    public void loadBattle(BattleAppState battle) {
        game.nifty.fromXml("ui/loading/ui.xml", "start", this);
        game.audioState.setMusicMode(AudioAppState.MusicMode.Loading);
        status = Status.LoadingBattle;
        loadObject = battle;
    }

    public void loadWorldMap() {
        game.nifty.fromXml("ui/loading/ui.xml", "start", this);
        //game.audioState.setMusicMode(AudioAppState.MusicMode.Loading);
        status = Status.LoadingWorldMap;

    }

    @Override
    public void update(float tpf) {


        switch (status) {

            case Init:
                InitGame();
                break;

            case MainMenu:
                game.mainMenuState.enterMainMenu();
                game.audioState.setMusicMode(AudioAppState.MusicMode.Menu);
                status = Status.Idle;
                break;


            case LoadingWorldMap:
                manager.attach(game.worldMapState);
                if (Main.devMode) {
                    manager.attach(game.debugState);
                }
                game.audioState.setMusicMode(AudioAppState.MusicMode.WorldMapIdle);
                status = Status.Idle;
                break;

            case LoadingBattle:

                if (game.getStateManager().hasState(game.worldMapState)) {
                    game.worldMapState.disableStateAndScene();
                }
                manager.attach((BattleAppState) loadObject);
                loadObject = null;
                status = Status.Idle;
                break;
        }



    }

    public void InitGame() {

        manager.attach(game.audioState);
        manager.attach(game.screenshotState);
        manager.attach(game.mainMenuState);
        game.bulletState.setThreadingType(ThreadingType.PARALLEL);
        game.bulletState.setBroadphaseType(BroadphaseType.DBVT);
        manager.attach(game.bulletState);
        
        
        status = Status.MainMenu;

       
        if (Main.devMode) {
            game.bulletState.getPhysicsSpace().enableDebug(game.getAssetManager());
        }

        DataLoader = new XMLDataLoader(game);
        game.wishToQuit = !DataLoader.loadAll();
  

        game.audioState.setMusicVolume(0.05f);
    }
}
