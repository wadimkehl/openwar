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

    public Terrain terrain;
    public ArrayList<Climate> climates;

    public Map() {
        terrain = new Terrain();
        climates = new ArrayList<Climate>();
    }

    public void addClimate(String n, String r, Vector3f c) {
        Climate clim = new Climate();
        clim.name = n;
        clim.refName = r;
        clim.color = c;
        climates.add(clim);
    }

   
}
