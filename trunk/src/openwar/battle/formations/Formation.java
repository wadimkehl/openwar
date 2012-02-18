/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package openwar.battle.formations;

import openwar.battle.Soldier;
import openwar.battle.Unit;

/**
 *
 * @author kehl
 */
public abstract class Formation {
    
    
    public Unit u;
    public boolean sparseFormation;
    
    public abstract void doFormation(boolean run, boolean warp);
    
    
    
}
