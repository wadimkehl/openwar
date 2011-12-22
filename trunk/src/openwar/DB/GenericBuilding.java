/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package openwar.DB;

import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author kehl
 */
public class GenericBuilding {

    public class GenericRecruitmentStats {

        public String refName;
        public int maxUnits, turnsTillNextUnit;

        public GenericRecruitmentStats() {
        }
    }

    public class Level {

        public int level;
        public String name;
        public String refName;
        public int cost;
        public int turns;
        public HashMap<String, String> requires;
        public HashMap<String, ArrayList<String>> provides;
        public HashMap<String, GenericRecruitmentStats> genRecStats;
        public Description desc;

        public Level() {
            requires = new HashMap<String, String>();
            provides = new HashMap<String, ArrayList<String>>();
            genRecStats = new HashMap<String, GenericRecruitmentStats>();

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

    public void createRecruitmentStats() {
        for (Level l : levels.values()) {

            for (String s : l.provides.keySet()) {
                if (s.equals("unit")) {
                    ArrayList<String> list = l.provides.get(s);
                    for (String unit : list) {
                        String[] tokens = unit.split(" ");
                        GenericRecruitmentStats rec = new GenericRecruitmentStats();
                        rec.refName = tokens[0];
                        rec.maxUnits = Integer.parseInt(tokens[1]);
                        rec.turnsTillNextUnit = Integer.parseInt(tokens[2]);
                        l.genRecStats.put(rec.refName, rec);
                    }
                }

            }

        }
    }
}
