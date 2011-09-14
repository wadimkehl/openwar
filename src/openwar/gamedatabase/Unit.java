/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package openwar.gamedatabase;

import com.jme3.texture.Texture;
import java.util.Map;

/**
 *
 * @author kehl
 */
 public class Unit
    {
        public String refName;
        
        public int maxCount;
        public int maxMovePoints;

        public Map<String,Integer> stats;
        public Texture unitCard;
        
        
    }