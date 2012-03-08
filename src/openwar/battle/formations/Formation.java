/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package openwar.battle.formations;

import openwar.battle.Unit;

/**
 *
 * @author kehl
 */
public abstract class Formation {
    
    
    public Unit u;
    public boolean sparseFormation;
    
 
    
    
    public abstract void previewFormation(float lx, float ly, float rx, float ry, boolean accept);
    public abstract void doFormation(boolean run, boolean warp,boolean invert);
    public abstract float getWidth();
    public abstract float getDepth();
    
    
    
}