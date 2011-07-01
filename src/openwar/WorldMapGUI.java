/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package openwar;

import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.screen.Screen;
import de.lessvoid.nifty.screen.ScreenController;

/**
 *
 * @author kehl
 */
public class WorldMapGUI implements ScreenController {

    Nifty nifty;
    Screen screen;
    
    public WorldMapGUI() {
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
}
