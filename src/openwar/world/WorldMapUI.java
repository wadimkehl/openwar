/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package openwar.world;

import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.niftygui.RenderImageJme;
import com.jme3.texture.Texture2D;
import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.elements.Element;
import de.lessvoid.nifty.elements.render.ImageRenderer;
import de.lessvoid.nifty.render.NiftyImage;
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
    Element unitImage[] = new Element[20];

    public WorldMapUI() {
    }

    @Override
    public void bind(Nifty nifty, Screen screen) {
        this.nifty = nifty;
        this.screen = screen;
    }

    @Override
    public void onStartScreen() {

        for (int i = 0; i < 20; i++) {
            unitImage[i] = nifty.getCurrentScreen().findElementByName("unit" + i);
        }


    }

    @Override
    public void onEndScreen() {
    }

    public void onClick(String s, String b) {
        try {
            Main.scriptEngine.eval("onWorldMapUIClicked('" + s + "'," + b + ")");
        } catch (ScriptException ex) {
            Logger.getLogger(WorldMapUI.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public void onMinimapClick(final int mouseX, final int mouseY) {
        Vector2f p = new Vector2f(mouseX, mouseY);
        p = game.worldMapState.map.minimap.screenToMinimap(p);
        Vector3f l = game.getCamera().getLocation();
        Vector3f d = game.getCamera().getDirection();
        Vector3f goal = new Vector3f(p.x, 0f, p.y);
        game.getCamera().setLocation(goal.add(d.mult(l.y / d.y)));

    }
    
    public void setImage(String id,Texture2D t)
    {
        Element l = nifty.getCurrentScreen().findElementByName(id);
        l.getRenderer(ImageRenderer.class).setImage(
                new NiftyImage(nifty.getRenderEngine(), new RenderImageJme(t)));


    }
}
