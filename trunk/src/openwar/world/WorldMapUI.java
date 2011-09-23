/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package openwar.world;

import com.jme3.niftygui.RenderImageJme;
import com.jme3.texture.Texture2D;
import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.effects.EffectEventId;
import de.lessvoid.nifty.elements.Element;
import de.lessvoid.nifty.elements.render.ImageRenderer;
import de.lessvoid.nifty.render.NiftyImage;
import de.lessvoid.nifty.screen.Screen;
import de.lessvoid.nifty.screen.ScreenController;
import java.util.ArrayList;
import openwar.DB.Unit;
import openwar.Main;

/**
 *
 * @author kehl
 */
public class WorldMapUI implements ScreenController {

    Nifty nifty;
    Screen screen;
    public Main game;
    public Element unitImage[] = new Element[20];
    public ArrayList<Unit> selectedUnits = new ArrayList<Unit>();
    public WorldEntity selectedFrom = null;

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
        game.doScript("onWorldMapUIClicked('" + s + "'," + b + ")");


    }

    public void deselectAll() {

        deselectUnits();
        for (int i = 0; i < 20; i++) {
            setUnitImage(i, null);
        }
    }

    public void deselectUnits() {
        selectedUnits.clear();
        for (int i = 0; i < 20; i++) {
            unitImage[i].stopEffect(EffectEventId.onCustom);
        }


    }

    public void selectUnit(int number) {
        // TODO: Nifty always throws exceptions here 
        unitImage[number].startEffect(EffectEventId.onCustom, null, "selected");
        selectedUnits.add(selectedFrom.units.get(number));
        game.playSound("world_select_unit");

        game.doScript("onUnitSelected()");

    }

    public void drawReachableArea() {
        if (!selectedUnits.isEmpty()) {
            int x = game.worldMapState.map.selectedSettlement.posX;
            int z = game.worldMapState.map.selectedSettlement.posZ;
            game.worldMapState.map.drawReachableArea(selectedUnits, x, z);
        }
    }

    public void onUnitClick(String n, String b) {

        int number = Integer.parseInt(n);
        int button = Integer.parseInt(b);
        if (button > 0) {
            return;
        }

        WorldEntity e = game.worldMapState.map.selectedArmy;
        if (e == null) {
            e = game.worldMapState.map.selectedSettlement;
            if (e == null) {
                return;
            }
        }
        selectedFrom = e;


        if (e.units.size() - 1 < number) {
            deselectUnits();
            return;
        }


        if (!game.worldMapState.shiftPressed) {
            deselectUnits();
            selectUnit(number);
            drawReachableArea();

            if (game.worldMapState.map.selectedSettlement != null) {
                drawReachableArea();
            }

            return;
        }

//        int index = e.units.indexOf(selectedUnit);
//        for (int i = Math.min(index, number); i < Math.min(index, number); i++) {
//            selectUnit(i);
//        }

        return;

    }

    public void onMinimapClick(final int mouseX, final int mouseY) {
        Tile t = game.worldMapState.map.minimap.screenToMinimap(mouseX, mouseY);
        game.worldMapState.moveCameraTo(t);
    }

    public void setUnitImage(int number, Texture2D t) {
        if (t == null) {
            unitImage[number].getRenderer(ImageRenderer.class).setImage(null);
        } else {
            unitImage[number].getRenderer(ImageRenderer.class).setImage(
                    new NiftyImage(nifty.getRenderEngine(), new RenderImageJme(t)));
        }

    }

    public void setImage(String id, Texture2D t) {
        Element l = nifty.getCurrentScreen().findElementByName(id);


        if (t == null) {
            l.getRenderer(ImageRenderer.class).setImage(null);
        } else {
            l.getRenderer(ImageRenderer.class).setImage(
                    new NiftyImage(nifty.getRenderEngine(), new RenderImageJme(t)));
        }


    }
}
