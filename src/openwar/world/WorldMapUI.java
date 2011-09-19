/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package openwar.world;

import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.screen.Screen;
import de.lessvoid.nifty.screen.ScreenController;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.script.ScriptException;
import openwar.Main;

/**
 *
 * @author kehl
 */
public class WorldMapUI implements ScreenController {

    Nifty nifty;
    Screen screen;
    public Main game;

    public WorldMapUI() {
    }

    @Override
    public void bind(Nifty nifty, Screen screen) {
        this.nifty = nifty;
        this.screen = screen;
    }

    @Override
    public void onStartScreen() {
    }

    @Override
    public void onEndScreen() {
    }

    public void onClick(String s) {
        try {
            Main.scriptEngine.eval("onWorldMapUIClicked('" + s + "')");
        } catch (ScriptException ex) {
            Logger.getLogger(WorldMapUI.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public void onMinimapClick() {
        Vector2f p = game.getInputManager().getCursorPosition();
        p = game.worldMapState.map.minimap.screenToMinimap(p);
        Vector3f l = game.getCamera().getLocation();
        Vector3f d = game.getCamera().getDirection();
        Vector3f goal = new Vector3f(p.x,0f,p.y);
        float t = -l.y / d.y;
        game.getCamera().setLocation(goal.add(d.mult(-t)));

    }
}
