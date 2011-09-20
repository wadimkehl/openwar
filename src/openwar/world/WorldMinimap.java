/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package openwar.world;

import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.niftygui.RenderImageJme;
import com.jme3.texture.Image;
import com.jme3.texture.Texture2D;
import de.lessvoid.nifty.elements.Element;
import de.lessvoid.nifty.elements.render.ImageRenderer;
import de.lessvoid.nifty.render.NiftyImage;
import de.lessvoid.nifty.tools.SizeValue;
import java.nio.ByteBuffer;
import openwar.Main;

/**
 *
 * @author kehl
 */
public class WorldMinimap {

    public Texture2D minimapImage;
    public Element minimapElement;
    public Vector3f minimapCameraColor = new Vector3f(255, 50, 50);
    public Vector3f minimapNoOwnerColor = new Vector3f(50, 50, 255);
    public WorldMap map;
    public int mapHeight, mapWidth, imageHeight, imageWidth, panelHeight, panelWidth;
    public float imageRatio, panelRatio, mapRatio, disparity;
    public int imageX, imageY;
    public float[][] borderMap;

    public WorldMinimap(WorldMap m) {
        map = m;
        minimapElement = map.game.nifty.getCurrentScreen().findElementByName("minimap");
        Element panel = map.game.nifty.getCurrentScreen().findElementByName("minimap_panel");

        panelWidth = panel.getWidth();
        panelHeight = panel.getHeight();
        imageHeight = minimapElement.getHeight();
        imageWidth = minimapElement.getWidth();
        mapHeight = map.height;
        mapWidth = map.width;

        mapRatio = (float) mapHeight / (float) mapWidth;
        imageRatio = (float) imageHeight / (float) imageWidth;
        panelRatio = (float) panelHeight / (float) panelWidth;
        float ratio = mapRatio * panelRatio;
        minimapElement.setWidth((int) (ratio * (float) minimapElement.getWidth()));
        imageX = minimapElement.getX();
        imageY = map.game.getCamera().getHeight() - minimapElement.getY();

//        int newWidth = (int)(ratio * (float) minimapElement.getWidth());
//        minimapElement.setConstraintWidth(new SizeValue(new Integer(newWidth).toString()));
//        minimapElement.setConstraintX(new SizeValue(new Integer(200).toString()));
//          minimapElement.setConstraintY(new SizeValue(new Integer(200).toString()));    
//        panel.layoutElements();
//        
        disparity = (float) mapHeight / (float) imageHeight;

        borderMap = new float[mapWidth][mapHeight];
        String lastReg = map.worldTiles[0][0].region;
        for (int j = 0; j < mapHeight; j++) {
            for (int i = 0; i < mapWidth; i++) {
                borderMap[i][j] = 1f;
                if (!lastReg.equals(map.worldTiles[i][j].region)) {
                    lastReg = map.worldTiles[i][j].region;
                    borderMap[i][j] = 0f;
                }
            }
            if (j < mapHeight - 1) {
                lastReg = map.worldTiles[0][j].region;
            }
        }
        lastReg = map.worldTiles[0][0].region;
        for (int i = 0; i < mapWidth; i++) {
            for (int j = 0; j < mapHeight; j++) {
                if (!lastReg.equals(map.worldTiles[i][j].region)) {
                    lastReg = map.worldTiles[i][j].region;
                    borderMap[i][j] = 0f;
                }
            }
            if (i < mapWidth - 1) {
                lastReg = map.worldTiles[i][0].region;
            }
        }

    }

    public Vector2f screenToMinimap(Vector2f p) {
        return p.subtract((float) imageX, (float) (map.game.getCamera().getHeight() - imageY)).multLocal(disparity);
    }

    void drawMinimapLine(ByteBuffer data, Vector2f p, Vector2f q) {
        int x = (int) p.x, y = (int) p.y;
        int xQ = (int) q.x, yQ = (int) q.y;
        int D = 0, HX = xQ - x, HY = yQ - y, c, M, xInc = 1, yInc = 1;
        if (HX < 0) {
            xInc = -1;
            HX = -HX;
        }
        if (HY < 0) {
            yInc = -1;
            HY = -HY;
        }



        if (HY <= HX) {
            c = 2 * HX;
            M = 2 * HY;
            for (;;) {
                int base = (map.ensureInTerrainZ(y) * mapHeight + map.ensureInTerrainX(x)) * 3;
                data.put(base, (byte) (((int) minimapCameraColor.x) & 0xff));
                data.put(base + 1, (byte) (((int) minimapCameraColor.y) & 0xff));
                data.put(base + 2, (byte) (((int) minimapCameraColor.z) & 0xff));
                if (x == xQ) {
                    break;
                }
                x += xInc;
                D += M;
                if (D > HX) {
                    y += yInc;
                    D -= c;
                }
            }
        } else {
            c = 2 * HY;
            M = 2 * HX;
            for (;;) {
                int base = (map.ensureInTerrainZ(y) * mapHeight + map.ensureInTerrainX(x)) * 3;
                data.put(base, (byte) (((int) minimapCameraColor.x) & 0xff));
                data.put(base + 1, (byte) (((int) minimapCameraColor.y) & 0xff));
                data.put(base + 2, (byte) (((int) minimapCameraColor.z) & 0xff));
                if (y == yQ) {
                    break;
                }
                y += yInc;
                D += M;
                if (D > HY) {
                    x += xInc;
                    D -= c;
                }
            }
        }
    }

    public void update() {

        ByteBuffer data = ByteBuffer.allocateDirect(mapHeight * mapWidth * 3);
        for (int j = 0; j < mapHeight; j++) {
            for (int i = 0; i < mapWidth; i++) {
                Vector3f col = minimapNoOwnerColor;
                String owner = Main.DB.hashedRegions.get(map.worldTiles[i][mapHeight - 1 - j].region).owner;
                if (!"".equals(owner)) {
                    col = Main.DB.genFactions.get(owner).color;
                }
                float border = borderMap[i][mapHeight - 1 - j];
                int r = (int) (col.x * border);
                int g = (int) (col.y * border);
                int b = (int) (col.z * border);
                data.put((byte) (r & 0xff));
                data.put((byte) (g & 0xff));
                data.put((byte) (b & 0xff));
            }
        }

        Vector3f loc = map.game.getCamera().getLocation();

        float left = map.game.getCamera().getFrustumLeft();
        float right = map.game.getCamera().getFrustumRight();
        float t;

        Vector3f t0 = map.game.getCamera().getWorldCoordinates(new Vector2f(left, map.game.getCamera().getHeight()), 0f);
        Vector3f t1 = map.game.getCamera().getWorldCoordinates(new Vector2f(left, map.game.getCamera().getHeight()), 1f);
        t1.subtractLocal(t0).normalizeLocal();
        t = -loc.y / t1.y;
        Vector2f lu = new Vector2f((loc.x + t * t1.x), (mapHeight - 1 - (loc.z + t * t1.z)));

        t0 = map.game.getCamera().getWorldCoordinates(new Vector2f(right, map.game.getCamera().getHeight()), 0f);
        t1 = map.game.getCamera().getWorldCoordinates(new Vector2f(right, map.game.getCamera().getHeight()), 1f);
        t1.subtractLocal(t0).normalizeLocal();
        t = -loc.y / t1.y;
        Vector2f ru = new Vector2f((loc.x - t * t1.x), (mapHeight - 1 - (loc.z + t * t1.z)));

        t0 = map.game.getCamera().getWorldCoordinates(new Vector2f(left, map.game.getCamera().getHeight() * 0.2f), 0f);
        t1 = map.game.getCamera().getWorldCoordinates(new Vector2f(left, map.game.getCamera().getHeight() * 0.2f), 1f);
        t1.subtractLocal(t0).normalizeLocal();
        t = -loc.y / t1.y;
        Vector2f lb = new Vector2f((loc.x + t * t1.x), (mapHeight - 1 - (loc.z + t * t1.z)));

        t0 = map.game.getCamera().getWorldCoordinates(new Vector2f(right, map.game.getCamera().getHeight() * 0.2f), 0f);
        t1 = map.game.getCamera().getWorldCoordinates(new Vector2f(right, map.game.getCamera().getHeight() * 0.2f), 1f);
        t1.subtractLocal(t0).normalizeLocal();
        t = -loc.y / t1.y;
        Vector2f rb = new Vector2f((loc.x - t * t1.x), (mapHeight - 1 - (loc.z + t * t1.z)));

        drawMinimapLine(data, lu, ru);
        drawMinimapLine(data, lu, lb);
        drawMinimapLine(data, ru, rb);
        drawMinimapLine(data, lb, rb);

        minimapImage = new Texture2D(new Image(Image.Format.RGB8, mapWidth, mapHeight, data));
        minimapElement.getRenderer(ImageRenderer.class).setImage(
                new NiftyImage(map.game.nifty.getRenderEngine(), new RenderImageJme(minimapImage)));

    }
}
