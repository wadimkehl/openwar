/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package openwar.world;

import com.jme3.math.Vector2f;
import com.jme3.niftygui.RenderImageJme;
import com.jme3.texture.Image;
import com.jme3.texture.Texture.MagFilter;
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
import openwar.DB.GenericUnit;
import openwar.DB.Settlement;
import openwar.DB.Settlement.Construction;
import openwar.DB.Settlement.Recruitment;
import openwar.DB.Unit;
import openwar.Main;

/**
 *
 * @author kehl
 */
public class WorldMapUI implements ScreenController {

    public enum LastLayerSelection {

        Units,
        Buildings
    };
    Nifty nifty;
    Screen screen;
    public Main game;
    public HashMap<String, Element> elements = new HashMap<String, Element>();
    public ArrayList<Unit> selectedUnits = new ArrayList<Unit>();
    public WorldEntity selectedFrom = null;
    public Settlement selectedSettlement;
    public int lastIndex;
    de.lessvoid.nifty.effects.impl.Hint h = new de.lessvoid.nifty.effects.impl.Hint();
    Properties p = new Properties();
    EffectProperties prop = new EffectProperties(p);
    boolean hintShown;
    public LastLayerSelection lastSettlementLayerSelection = LastLayerSelection.Units;

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
            elements.put("unit" + i, screen.findElementByName("unit" + i));

        }
        for (int i = 0; i < 12; i++) {
            elements.put("building" + i, screen.findElementByName("building" + i));
            elements.put("construction" + i, screen.findElementByName("construction" + i));

        }

        for (int i = 0; i < 10; i++) {
            elements.put("recruitment" + i, screen.findElementByName("recruitment" + i));
            elements.put("recruitmentList" + i, screen.findElementByName("recruitmentList" + i));
            elements.put("recruitmentListBar" + i, screen.findElementByName("recruitmentListBar" + i));

        }
        for (int i = 0; i < 6; i++) {
            elements.put("constructionList" + i, screen.findElementByName("constructionList" + i));
            elements.put("constructionListBar" + i, screen.findElementByName("constructionListBar" + i));

        }


        elements.put("settlement_name", screen.findElementByName("settlement_name"));


    }

    @Override
    public void onEndScreen() {
    }

    public void onClick(String s, String b) {
        game.doScript("onWorldMapUIClicked('" + s + "'," + b + ")");


    }

    public void refreshSettlementLayer() {
        for (int i = 0; i < 12; i++) {
            setImage("construction" + i, (Texture2D) game.getAssetManager().loadTexture("textures/back.png"));
        }
        for (int i = 0; i < 6; i++) {
            setImage("constructionList" + i, (Texture2D) game.getAssetManager().loadTexture("textures/back.png"));
        }
        for (int i = 0; i < 10; i++) {
            setImage("recruitment" + i, (Texture2D) game.getAssetManager().loadTexture("textures/back.png"));
        }
        for (int i = 0; i < 10; i++) {
            setImage("recruitmentList" + i, (Texture2D) game.getAssetManager().loadTexture("textures/back.png"));
        }

        Settlement s = selectedSettlement;
        if (s == null) {
            return;
        }

        setText("settlement_name", s.name);


        int i = 0;
        for (Construction c : s.constructionPool.values()) {
            GenericBuilding gb = Main.DB.genBuildings.get(c.refName);
            Level l = gb.levels.get(c.level);
            setImage("construction" + i, l.desc.card);
            i++;
        }
        i = 0;
        for (Construction c : s.constructions) {
            GenericBuilding gb = Main.DB.genBuildings.get(c.refName);
            Level l = gb.levels.get(c.level);
            setImage("constructionList" + i, l.desc.card);
            i++;
        }
        i = 0;
        for (String r : s.recruitmentPool.keySet()) {
            GenericUnit gu = Main.DB.genUnits.get(r);
            setImage("recruitment" + i, gu.desc.card);
            i++;
        }
        i = 0;
        for (Recruitment r : s.recruitments) {
            GenericUnit gu = Main.DB.genUnits.get(r.refName);
            setImage("recruitmentList" + i, gu.desc.card);
            i++;
        }

        if (!s.constructions.isEmpty()) {
            int l = s.constructions.get(0).currentTurn;
            int h = s.constructions.get(0).nrTurns;
            Texture2D t = createProgressBar(l, h);
            setImage("constructionListBar0", t);

        } else {
            setImage("constructionListBar0", null);
        }

        if (!s.recruitments.isEmpty()) {
            int l = s.recruitments.get(0).currentTurn;
            int h = Main.DB.genUnits.get(s.recruitments.get(0).refName).turnsToRecruit;
            Texture2D t = createProgressBar(l, h);
            setImage("recruitmentListBar0", t);

        } else {
            setImage("recruitmentListBar0", null);
        }


    }

    public Texture2D createProgressBar(int curr, int max) {
        ByteBuffer buf = ByteBuffer.allocateDirect(max * 4);

        for (int i = max - 1; i >= 0; i--) {

            buf.put((byte) (0));
            buf.put((byte) ((0xff & 255)));
            buf.put((byte) (0));
            if (i >= curr) {
                buf.put((byte) ((0xff & 128)));
            } else {
                buf.put((byte) (0));
            }

        }
        Texture2D t = new Texture2D(new Image(Image.Format.RGBA8, 1, max, buf));
        t.setMagFilter(MagFilter.Nearest);
        return t;

    }

    public void deselectAll() {

        deselectUnits();
        for (int i = 0; i < 20; i++) {
            setImage("unit" + i, null);
        }
        for (int i = 0; i < 12; i++) {
            setImage("building" + i, null);
        }
//        for (int i = 0; i < 12; i++) {
//            setImage("construction" + i, null);
//        }
//        for (int i = 0; i < 6; i++) {
//            setImage("constructionList" + i, null);
//        }
//        for (int i = 0; i < 10; i++) {
//            setImage("recruitment" + i, null);
//        }
//        for (int i = 0; i < 10; i++) {
//            setImage("recruitmentList" + i, null);
//        }
        game.showUIElement("settlement_layer", false);
        selectedSettlement = null;

    }

    public void deselectUnits() {
        selectedUnits.clear();
        for (int i = 0; i < 20; i++) {
            elements.get("unit" + i).stopEffect(EffectEventId.onCustom);
        }


    }

    public void selectUnit(int number) {
        elements.get("unit" + number).startEffect(EffectEventId.onCustom, null, "selected");
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

        lastSettlementLayerSelection = LastLayerSelection.Units;

        for (int i = 0; i < 12; i++) {
            game.worldMapState.uiController.setImage("building" + i, null);
        }

        for (int i = 0; i < list.size(); i++) {
            game.worldMapState.uiController.setImage("unit" + i, Main.DB.genUnits.get(list.get(i).refName).desc.card);
        }
    }

    public void switchToBuildingsLayer() {

        game.showUIElement("front_building_layer", true);
        game.showUIElement("front_unit_layer", false);

        lastSettlementLayerSelection = LastLayerSelection.Buildings;


        for (int i = 0; i < 20; i++) {
            game.worldMapState.uiController.setImage("unit" + i, null);
        }

        if (selectedSettlement == null) {
            return;
        }

        HashMap<String, Building> list = selectedSettlement.buildings;

        int i = 0;
        for (Building b : list.values()) {
            GenericBuilding gb = Main.DB.genBuildings.get(b.refName);
            Level l = gb.levels.get(b.level);
            game.worldMapState.uiController.setImage("building" + i, l.desc.card);
            i++;
        }
    }

    public void selectSettlement(Settlement s) {

        selectedSettlement = s;
        game.showUIElement("settlement_layer", true);


        if (lastSettlementLayerSelection == LastLayerSelection.Units) {
            switchToUnitsLayer(s.units);
        } else {
            switchToBuildingsLayer();
        }

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

        refreshSettlementLayer();
    }

    public void onRecruitmentClick(String ind, String b) {

        int index = Integer.parseInt(ind);
        int button = Integer.parseInt(b);

        int i = 0;
        for (String r : selectedSettlement.recruitmentPool.keySet()) {

            if (i == index) {
                if (button == 0) {

                    selectedSettlement.startRecruitment(r);
                }
                break;
            }

            i++;
        }

        refreshSettlementLayer();

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

        refreshSettlementLayer();
    }

    public void onRecruitmentListClick(String ind, String b) {

        int index = Integer.parseInt(ind);
        int button = Integer.parseInt(b);

        int i = 0;
        for (Recruitment r : selectedSettlement.recruitments) {

            if (i == index) {
                if (button == 0) {
                    selectedSettlement.abortRecruitment(r);
                }
                break;
            }

            i++;
        }

        refreshSettlementLayer();
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

        if (game.worldMapState.shiftPressed) {
            for (int i = Math.min(index, lastIndex); i <= Math.max(index, lastIndex); i++) {
                selectUnit(i);
            }
        } else {

            // TODO: Nifty consumes ctrl, but not alt...
            if (!game.worldMapState.altPressed) {
                deselectUnits();
            }
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


        prop.setProperty("hintText", t.MinimapInfo());
        h.activate(nifty, screen.findElementByName("minimap"), prop);
        h.execute(screen.findElementByName("minimap"), 10, null, nifty.getRenderEngine());
        hintShown = true;


    }

    public void setImage(String id, Texture2D t) {
        Element l = elements.get(id);


        if (t == null) {
            l.getRenderer(ImageRenderer.class).setImage(null);
        } else {
            l.getRenderer(ImageRenderer.class).setImage(
                    new NiftyImage(nifty.getRenderEngine(), new RenderImageJme(t)));
        }
    }

    public void setText(String id, String t) {
        Element l = elements.get(id);
        l.getRenderer(TextRenderer.class).setText(t);
    }
}
