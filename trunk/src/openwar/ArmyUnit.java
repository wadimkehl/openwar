/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package openwar;

import com.jme3.texture.Texture;

/**
 *
 * @author kehl
 */
public class ArmyUnit {
    
    
    int maxMovePoints;
    int currMovePoints;
    Texture unitCard;
    
    
    public ArmyUnit()
    {
        
    }
    
    public void resetMovePoints()
    {
        currMovePoints = maxMovePoints;
    }
    
}
