/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package openwar.DB;

import com.jme3.texture.Texture2D;
import java.util.Map;

/**
 *
 * @author kehl
 */
 public class GenericUnit
    {
        public String refName;
        public String name;
        public int maxCount;
        public int maxMovePoints;

        public Map<String,Integer> stats;
        public Description desc;
        
        
    }