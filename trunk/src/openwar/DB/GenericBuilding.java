/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package openwar.DB;

import com.jme3.scene.Spatial;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture2D;
import java.util.HashMap;

/**
 *
 * @author kehl
 */
public class GenericBuilding {

    public class Level {

        public int level;
        public String name;
        public String refName;
        public int cost;
        public int turns;
        public Texture2D card;
        public Texture2D detail;
        public Spatial model;

        public Level() {
        }
    };
    public String refName;
    public String name;
    public int maxLevel;
    public HashMap<Integer, Level> levels;

    public GenericBuilding() {
        levels = new HashMap<Integer, Level>();
    }

    public void addLevel(int l, String n, String r, int c, int t, Texture2D i, Spatial m) {
        Level lev = new Level();
        lev.level = l;
        lev.name = n;
        lev.refName = r;
        lev.cost = c;
        lev.turns = t;
        lev.card = i;
        lev.model = m;
        levels.put(l, lev);
    }
}
