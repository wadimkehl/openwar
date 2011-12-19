/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package openwar.DB;

import com.jme3.scene.Spatial;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture2D;
import java.util.HashMap;
import java.util.Map;

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
        public HashMap<String, String> requires;
        public HashMap<String, String> provides;
        public Description desc;

        public Level() {
            requires = new HashMap<String, String>();
            provides = new HashMap<String, String>();

        }
    };
    public String refName;
    public String name;
    public int maxLevel;
    public HashMap<Integer, Level> levels;
    public HashMap<String, String> requires;

    public GenericBuilding() {
        levels = new HashMap<Integer, Level>();
        requires = new HashMap<String, String>();

    }

    public void addLevel(int l, String n, String r, int c, int t, Description d) {
        Level lev = new Level();
        lev.level = l;
        lev.name = n;
        lev.refName = r;
        lev.cost = c;
        lev.turns = t;
        lev.desc = d;
        levels.put(l, lev);
    }
}
