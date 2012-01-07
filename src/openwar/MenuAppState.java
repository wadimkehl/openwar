/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package openwar;

import com.jme3.app.Application;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.screen.Screen;
import de.lessvoid.nifty.screen.ScreenController;

/**
 *
 * @author kehl
 */
public class MenuAppState extends AbstractAppState implements ScreenController {

    public enum Status {

        None,
        Main,
        NewGame,
        Options,
        Quit
    }
    Main game;
    AppStateManager manager;
    Status status;
    Nifty nifty;
    Screen screen;

    public MenuAppState() {
        status = Status.None;
    }

    @Override
    public void initialize(AppStateManager stateManager, Application app) {
        this.initialize(stateManager, (Main) app);
    }

    public void initialize(AppStateManager stateManager, Main main) {
        game = main;
        manager = stateManager;
        initialized = true;



    }

    public void startGame() {
        Main.DB.playerFaction = "romans";
        // Make sure that the player faction is the first in the list
        Main.DB.factions.remove(Main.DB.hashedFactions.get(Main.DB.playerFaction));
        Main.DB.factions.add(0, Main.DB.hashedFactions.get(Main.DB.playerFaction));

        game.gameLoaderState.loadWorldMap();
    }

    public void quitGame() {
        game.wishToQuit = true;
    }

    public void enterMainMenu() {
        game.nifty.fromXml("ui/menu/ui.xml", "start", this);
    }

    @Override
    public void update(float tpf) {
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
}
