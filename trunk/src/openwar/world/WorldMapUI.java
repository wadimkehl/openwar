/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package openwar.world;

import com.jme3.math.Vector2f;
import com.jme3.niftygui.RenderImageJme;
import com.jme3.texture.Texture2D;
import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.effects.Effect;
import de.lessvoid.nifty.effects.EffectEventId;
import de.lessvoid.nifty.elements.Element;
import de.lessvoid.nifty.elements.render.ImageRenderer;
import de.lessvoid.nifty.elements.render.TextRenderer;
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
    public int lastIndex;
    public Effect hintEffect;
    public Element hintText;

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

        hintEffect = screen.findElementByName("minimap").getEffects(EffectEventId.onCustom, de.lessvoid.nifty.effects.impl.Hint.class).get(0);
        hintText = screen.findElementByName("hint-text");
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
        unitImage[number].startEffect(EffectEventId.onCustom, null, "selected");
        selectedUnits.add(selectedFrom.units.get(number));
        game.playSound("world_select_unit");

        game.doScript("onUnitSelected()");

    }

    public void drawReachableArea() {
        if (!selectedUnits.isEmpty()) {
            int x = selectedFrom.posX;
            int z = selectedFrom.posZ;
            game.worldMapState.map.drawReachableArea(selectedUnits, x, z);
        }
    }

    public void onUnitClick(String ind, String b) {

        int index = Integer.parseInt(ind);
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


        if (e.units.size() - 1 < index) {
            deselectUnits();
            return;
        }

        // TODO: check why this is not working
        if (!game.worldMapState.ctrlPressed) {
            deselectUnits();
        }


        if (game.worldMapState.shiftPressed) {
            for (int i = Math.min(index, lastIndex); i <= Math.max(index, lastIndex); i++) {
                selectUnit(i);
            }

        } else {
            selectUnit(index);
        }

        drawReachableArea();
        lastIndex = index;
    }

    public void onMinimapClick() {
        Vector2f m = game.getInputManager().getCursorPosition();
        Tile temp = game.worldMapState.map.minimap.screenToMinimapJME((int) m.x, (int) m.y);
        temp = game.worldMapState.map.ensureInTerrain(temp);
        game.worldMapState.moveCameraTo(temp);
    }

    public void onMinimapHover() {
        Vector2f m = game.getInputManager().getCursorPosition();
        Tile temp = game.worldMapState.map.minimap.screenToMinimapJME((int) m.x, (int) m.y);
        temp.x--;
        temp.z--;
        temp = game.worldMapState.map.ensureInTerrain(temp);
        WorldTile t = game.worldMapState.map.worldTiles[temp.x][temp.z];
        
        //hintText.getRenderer(TextRenderer.class).setText(t.region);

        //screen.findElementByName("#hint-text").getEffectManager().resetSingleEffect(EffectEventId.onHover);

        //hintEffect.getParameters().put("hintText", t.shortInfo());
       // screen.findElementByName("#hint-text");
//        startEffect(EffectEventId.onCustom, null, "hover");
//
//


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
