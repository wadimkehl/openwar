/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package openwar.world;

import openwar.DB.Unit;
import com.jme3.texture.Texture;

/**
 *
 * @author kehl
 */
public class WorldUnit extends Unit{
    
    
    int currMovePoints;    
    
    public WorldUnit(int maxpoints)
    {
       currMovePoints = maxMovePoints;
    }
    
    public void resetMovePoints()
    {
        currMovePoints = maxMovePoints;
    }
    
}
