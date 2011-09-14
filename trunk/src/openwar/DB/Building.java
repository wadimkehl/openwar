/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package openwar.DB;

import com.jme3.texture.Texture;
import java.util.HashMap;

/**
 *
 * @author kehl
 */
public class Building {

    public class Level {

        int level;
        String name;
        String refName;
        int cost;
        int turns;
        Texture image;

        public Level() {
        }
    };
    public String refName;
    public String name;
    public int maxLevel;
    public HashMap<Integer, Level> levels;

    public Building() {
        levels = new HashMap<Integer, Level>();
    }

    public void addLevel(int l, String n, String r, int c, int t, Texture i) {
        Level lev = new Level();
        lev.level = l;
        lev.name = n;
        lev.refName = r;
        lev.cost = c;
        lev.turns = t;
        lev.image = i;
    }

    
}
