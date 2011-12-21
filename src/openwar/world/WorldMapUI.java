/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package openwar.world;

import com.jme3.math.Vector2f;
import com.jme3.niftygui.RenderImageJme;
import com.jme3.texture.Image;
import com.jme3.texture.Texture2D;
import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.effects.Effect;
import de.lessvoid.nifty.effects.EffectEventId;
import de.lessvoid.nifty.effects.EffectProperties;
import de.lessvoid.nifty.effects.Falloff;
import de.lessvoid.nifty.elements.Element;
import de.lessvoid.nifty.elements.render.ImageRenderer;
import de.lessvoid.nifty.elements.render.TextRenderer;
import de.lessvoid.nifty.render.NiftyImage;
import de.lessvoid.nifty.screen.Screen;
import de.lessvoid.nifty.screen.ScreenController;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;
import openwar.DB.Building;
import openwar.DB.GenericBuilding;
import openwar.DB.GenericBuilding.Level;
import openwar.DB.Settlement;
import openwar.DB.Settlement.Construction;
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
    public Element buildingImage[] = new Element[12];
    public Element constructionImage[] = new Element[12];
    public Element constructionListImage[] = new Element[6];
    public Element constructionListBarImage;
    public ArrayList<Unit> selectedUnits = new ArrayList<Unit>();
    public WorldEntity selectedFrom = null;
    public Settlement selectedSettlement;
    public int lastIndex;
    de.lessvoid.nifty.effects.impl.Hint h = new de.lessvoid.nifty.effects.impl.Hint();
    Properties p = new Properties();
    EffectProperties prop = new EffectProperties(p);
    boolean hintShown;

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
        for (int i = 0; i < 12; i++) {
            buildingImage[i] = nifty.getCurrentScreen().findElementByName("building" + i);
        }
        for (int i = 0; i < 12; i++) {
            constructionImage[i] = nifty.getCurrentScreen().findElementByName("construction" + i);
        }
        for (int i = 0; i < 6; i++) {
            constructionListImage[i] = nifty.getCurrentScreen().findElementByName("constructionList" + i);
        }
        constructionListBarImage = nifty.getCurrentScreen().findElementByName("constructionList0Bar");

    }

    @Override
    public void onEndScreen() {
    }

    public void onClick(String s, String b) {
        game.doScript("onWorldMapUIClicked('" + s + "'," + b + ")");


    }
    
    public void refreshSettlementLayer()
    {
          for (int i = 0; i < 12; i++) {
            setConstructionImage(i, null);
        }
        for (int i = 0; i < 6; i++) {
            setConstructionListImage(i, null);
        }
        
        Settlement s = selectedSettlement;
        
        if(s == null) return;

        int i = 0;
        for (Construction c : s.constructionPool.values()) {
            GenericBuilding gb = Main.DB.genBuildings.get(c.refName);
            Level l = gb.levels.get(c.level);
            setConstructionImage(i, l.desc.card);
            i++;
        }
        i = 0;
        for (Construction c : s.constructions) {
            GenericBuilding gb = Main.DB.genBuildings.get(c.refName);
            Level l = gb.levels.get(c.level);
            setConstructionListImage(i, l.desc.card);
            i++;
        }

        if (!s.constructions.isEmpty()) {

            int h = s.constructions.get(0).nrTurns;
            int l = s.constructions.get(0).currentTurn;
            ByteBuffer buf = ByteBuffer.allocateDirect(h * 4);

            for (int p = 0; p < h; p++) {

                buf.put((byte) (0));
                buf.put((byte) ((0xff & 255)));
                buf.put((byte) (0));
                if (p > l) {
                    buf.put((byte) (0));

                } else {
                    buf.put((byte) ((0xff & 200)));
                }

            }
            setConstructionListBarImage(0, new Texture2D(new Image(Image.Format.RGBA8, 1, h, buf)));

        } else {
            setConstructionListBarImage(0, null);
        }



    }

    public void deselectAll() {

        deselectUnits();
        for (int i = 0; i < 20; i++) {
            setUnitImage(i, null);
        }
        for (int i = 0; i < 12; i++) {
            setBuildingImage(i, null);
        }
        for (int i = 0; i < 12; i++) {
            setConstructionImage(i, null);
        }
        for (int i = 0; i < 6; i++) {
            setConstructionListImage(i, null);
        }
        game.showUIElement("settlement_layer", false);
        selectedSettlement = null;

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

    public void switchToUnitsLayer(ArrayList<Unit> list) {
        game.showUIElement("front_building_layer", false);
        game.showUIElement("front_unit_layer", true);


        for (int i = 0; i < list.size(); i++) {
            game.worldMapState.uiController.setBuildingImage(i, null);
        }

        for (int i = 0; i < list.size(); i++) {
            game.worldMapState.uiController.setUnitImage(i, Main.DB.genUnits.get(list.get(i).refName).desc.card);
        }
    }

    public void switchToBuildingsLayer(HashMap<String, Building> list) {
        game.showUIElement("front_building_layer", true);
        game.showUIElement("front_unit_layer", false);

        for (int i = 0; i < list.size(); i++) {
            game.worldMapState.uiController.setUnitImage(i, null);
        }

        int i = 0;
        for (Building b : list.values()) {
            GenericBuilding gb = Main.DB.genBuildings.get(b.refName);
            Level l = gb.levels.get(b.level);
            game.worldMapState.uiController.setBuildingImage(i, l.desc.card);
            i++;
        }
    }

    public void selectSettlement(Settlement s) {
        selectedSettlement = s;
        refreshSettlementLayer();
      

    }

    public void onBuildingClick(String ind, String b) {

        int index = Integer.parseInt(ind);
        int button = Integer.parseInt(b);
        if (button < 1) {
            return;
        }
    }

    public void onConstructionClick(String ind, String b) {

        int index = Integer.parseInt(ind);
        int button = Integer.parseInt(b);

        int i = 0;
        for (Construction c : selectedSettlement.constructionPool.values()) {

            if (i == index) {
                if (button == 0) {
                    selectedSettlement.startConstruction(c);
                }
                break;
            }

            i++;
        }

        selectSettlement(selectedSettlement);

    }

    public void onConstructionListClick(String ind, String b) {

        int index = Integer.parseInt(ind);
        int button = Integer.parseInt(b);

        int i = 0;
        for (Construction c : selectedSettlement.constructions) {

            if (i == index) {
                if (button == 0) {
                    selectedSettlement.abortConstruction(c);
                }
                break;
            }

            i++;
        }

        selectSettlement(selectedSettlement);
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

    public void onMinimapLayerHover() {

        if (hintShown) {
            h.deactivate();
            hintShown = false;
        }
    }

    public void onMinimapHover() {

        if (hintShown) {
            h.deactivate();
            hintShown = false;
        }

        Vector2f m = game.getInputManager().getCursorPosition();
        Tile temp = game.worldMapState.map.minimap.screenToMinimapJME((int) m.x, (int) m.y);
        temp = game.worldMapState.map.ensureInTerrain(temp);
        WorldTile t = game.worldMapState.map.worldTiles[temp.x][temp.z];


        prop.setProperty("hintText", t.shortInfo());
        h.activate(nifty, screen.findElementByName("minimap"), prop);
        h.execute(screen.findElementByName("minimap"), 10, null, nifty.getRenderEngine());
        hintShown = true;


    }

    public void setUnitImage(int number, Texture2D t) {
        if (t == null) {
            unitImage[number].getRenderer(ImageRenderer.class).setImage(null);
        } else {
            unitImage[number].getRenderer(ImageRenderer.class).setImage(
                    new NiftyImage(nifty.getRenderEngine(), new RenderImageJme(t)));
        }

    }

    public void setBuildingImage(int number, Texture2D t) {
        if (t == null) {
            buildingImage[number].getRenderer(ImageRenderer.class).setImage(null);
        } else {
            buildingImage[number].getRenderer(ImageRenderer.class).setImage(
                    new NiftyImage(nifty.getRenderEngine(), new RenderImageJme(t)));
        }

    }

    public void setConstructionImage(int number, Texture2D t) {
        if (t == null) {
            constructionImage[number].getRenderer(ImageRenderer.class).setImage(null);
        } else {
            constructionImage[number].getRenderer(ImageRenderer.class).setImage(
                    new NiftyImage(nifty.getRenderEngine(), new RenderImageJme(t)));
        }

    }

    public void setConstructionListImage(int number, Texture2D t) {
        if (t == null) {
            constructionListImage[number].getRenderer(ImageRenderer.class).setImage(null);
        } else {
            constructionListImage[number].getRenderer(ImageRenderer.class).setImage(
                    new NiftyImage(nifty.getRenderEngine(), new RenderImageJme(t)));
        }

    }

    public void setConstructionListBarImage(int number, Texture2D t) {
        if (t == null) {
            constructionListBarImage.getRenderer(ImageRenderer.class).setImage(null);
        } else {
            constructionListBarImage.getRenderer(ImageRenderer.class).setImage(
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

    public void setText(String id, String t) {
        Element l = nifty.getCurrentScreen().findElementByName(id);
        l.getRenderer(TextRenderer.class).setText(t);
    }
}
