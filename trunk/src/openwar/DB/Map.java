/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package openwar.DB;

import com.jme3.math.Vector3f;
import com.jme3.texture.Texture;
import java.util.ArrayList;
import java.util.HashMap;

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


    public Terrain terrain;
    public HashMap<Integer, GenericTile> tiles;
    public Texture heightmapTex, regionsTex, climatesTex, typesTex;
    public ArrayList<Texture> tileTextures;
    public ArrayList<Float> tileTextures_scales;
    public int tilesTexturesCount;

    public Map() {
        terrain = new Terrain();
        tiles = new HashMap<Integer, GenericTile>();
        tileTextures = new ArrayList<Texture>();
        tileTextures_scales = new ArrayList<Float>();

    }

  
}
