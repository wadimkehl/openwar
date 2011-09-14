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
public class ArmyUnit extends GameDatabase.Unit{
    
    
    int currMovePoints;    
    
    public ArmyUnit(int maxpoints)
    {
       currMovePoints = maxMovePoints = maxpoints;
    }
    
    public void resetMovePoints()
    {
        currMovePoints = maxMovePoints;
    }
    
}
