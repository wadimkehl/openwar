/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package openwar.DB;

import openwar.Main;

/**
 *
 * @author kehl
 */
public class Unit {

    public String refName;
    public int count, exp, att, def, currMovePoints;

    public Unit(String r) {
        
        refName = r;
        resetMovePoints();
    }

    public Unit() {
    }

    public void resetMovePoints() {
        
        currMovePoints = Main.DB.genUnits.get(refName).maxMovePoints;
    }
    
    public float calculateUpkeep()
    {
        float upkeep = Main.DB.genUnits.get(refName).upkeep;        
        float maxCount = Main.DB.genUnits.get(refName).maxCount;
        float fraction = ((float) count)/maxCount;     
        return (upkeep * fraction);
    }
}
