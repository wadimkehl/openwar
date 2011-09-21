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
    public int count, exp, att_bonus, def_bonus, currMovePoints;

    public Unit(String r) {
        
        refName = r;
        resetMovePoints();
    }

    Unit() {
    }

    public void resetMovePoints() {
        
        currMovePoints = Main.DB.genUnits.get(refName).maxMovePoints;
    }
}
