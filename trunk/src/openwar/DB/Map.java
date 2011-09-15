/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package openwar.DB;

import com.jme3.math.Vector3f;
import java.util.ArrayList;

/**
 *
 * @author kehl
 */
public class Map {

    public class Terrain {

        public class Heightmap {

            public float factor0, factor1, offset;

            public Heightmap() {
            }
        }

        public class Sun {

            public Vector3f color, direction;

            public Sun() {
            }
        }
        public Heightmap heightmap;
        public Sun sun;

        public Terrain() {
            heightmap = new Heightmap();
            sun = new Sun();
        }
    }

    public class Climate {

        public String name;
        public String refName;
        public Vector3f color;

        public Climate() {
        }
    }

    public class Region {

        public class Settlement {

            public class Building {

                public String refName;
                public int level;

                public Building() {
                }
            }

            public class Unit {

                public String refName;
                public int count, exp, att_bonus, def_bonus;

                public Unit() {
                }
            }
            
            public String name;
            public int level, population, posX, posZ;
            public boolean capital;
            public ArrayList<Building> buildings;
            public ArrayList<Unit> units;

            public Settlement() {
                buildings = new ArrayList<Building>();
                units = new ArrayList<Unit>();

            }
        }
        public String name;
        public String refName;
        public Settlement settlement;
        public String owner;
        public Vector3f color;

        public Region() {
            settlement = new Settlement();
        }

        public void AddBuilding(String refname, int level) {
        }

        public void AddUnit(String refname, int count, int exp, int att_bonus, int def_bonus) {
        }
    }
    public Terrain terrain;
    public ArrayList<Climate> climates;
    public ArrayList<Region> regions;

    public Map() {
        terrain = new Terrain();
        climates = new ArrayList<Climate>();
        regions = new ArrayList<Region>();

    }

    public void addClimate(String n, String r, Vector3f c) {
        Climate clim = new Climate();
        clim.name = n;
        clim.refName = r;
        clim.color = c;
        climates.add(clim);
    }

    public void addRegion(String n, String r, Vector3f c, String o) {
        Region reg = new Region();
        reg.name = n;
        reg.refName = r;
        reg.color = c;
        reg.owner = o;
        regions.add(reg);
    }
    
    public void addSettlement(String r, String n, int x, int z, int p, boolean c)
    {
        Region.Settlement s = new Region.Settlement();
    }
}
