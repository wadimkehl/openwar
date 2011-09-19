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
import java.nio.ByteBuffer;
import openwar.Main;

/**
 *
 * @author kehl
 */
public class WorldMinimap {

    public Texture2D minimapImage;
    public Element minimapElement;
    public Vector3f minimapColor = new Vector3f(255, 50, 50);
    public WorldMap map;

    public WorldMinimap(WorldMap m, String niftyid) {
        map = m;
        minimapElement = map.game.nifty.getCurrentScreen().findElementByName(niftyid);

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
                int base = (map.ensureInTerrainZ(y) * map.width + map.ensureInTerrainX(x)) * 3;
                data.put(base, (byte) (((int) minimapColor.x) & 0xff));
                data.put(base + 1, (byte) (((int) minimapColor.y) & 0xff));
                data.put(base + 2, (byte) (((int) minimapColor.z) & 0xff));
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
                int base = (map.ensureInTerrainZ(y) * map.width + map.ensureInTerrainX(x)) * 3;
                data.put(base, (byte) (((int) minimapColor.x) & 0xff));
                data.put(base + 1, (byte) (((int) minimapColor.y) & 0xff));
                data.put(base + 2, (byte) (((int) minimapColor.z) & 0xff));
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

        ByteBuffer data = ByteBuffer.allocateDirect(map.height * map.width * 3);
        for (int j = 0; j < map.height; j++) {
            for (int i = 0; i < map.width; i++) {

                Vector3f col = Main.DB.genTiles.get(map.worldTiles[i][map.height - 1 - j].groundType).color;
                int r = (int) col.x;
                int g = (int) col.y;
                int b = (int) col.z;
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
        Vector2f lu = new Vector2f((loc.x + t * t1.x), (map.height - 1 - (loc.z + t * t1.z)));

        t0 = map.game.getCamera().getWorldCoordinates(new Vector2f(right, map.game.getCamera().getHeight()), 0f);
        t1 = map.game.getCamera().getWorldCoordinates(new Vector2f(right, map.game.getCamera().getHeight()), 1f);
        t1.subtractLocal(t0).normalizeLocal();
        t = -loc.y / t1.y;
        Vector2f ru = new Vector2f((loc.x - t * t1.x), (map.height - 1 - (loc.z + t * t1.z)));

        t0 = map.game.getCamera().getWorldCoordinates(new Vector2f(left, map.game.getCamera().getHeight() * 0.2f), 0f);
        t1 = map.game.getCamera().getWorldCoordinates(new Vector2f(left, map.game.getCamera().getHeight() * 0.2f), 1f);
        t1.subtractLocal(t0).normalizeLocal();
        t = -loc.y / t1.y;
        Vector2f lb = new Vector2f((loc.x + t * t1.x), (map.height - 1 - (loc.z + t * t1.z)));

        t0 = map.game.getCamera().getWorldCoordinates(new Vector2f(right, map.game.getCamera().getHeight() * 0.2f), 0f);
        t1 = map.game.getCamera().getWorldCoordinates(new Vector2f(right, map.game.getCamera().getHeight() * 0.2f), 1f);
        t1.subtractLocal(t0).normalizeLocal();
        t = -loc.y / t1.y;
        Vector2f rb = new Vector2f((loc.x - t * t1.x), (map.height - 1 - (loc.z + t * t1.z)));

        drawMinimapLine(data, lu, ru);
        drawMinimapLine(data, lu, lb);
        drawMinimapLine(data, ru, rb);
        drawMinimapLine(data, lb, rb);

        minimapImage = new Texture2D(new Image(Image.Format.RGB8, map.width, map.height, data));
        minimapElement.getRenderer(ImageRenderer.class).setImage(
                new NiftyImage(map.game.nifty.getRenderEngine(), new RenderImageJme(minimapImage)));

    }
}
