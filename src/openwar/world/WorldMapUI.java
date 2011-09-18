/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package openwar.world;

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
public class WorldMapUI implements ScreenController{
    
    
     Nifty nifty;
    Screen screen;
    
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
    
    public void onClick(String s)
    {
        try {
            Main.scriptEngine.eval("onWorldMapUIClicked('"+ s + "')");
        } catch (ScriptException ex) {
            Logger.getLogger(WorldMapUI.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    
}
