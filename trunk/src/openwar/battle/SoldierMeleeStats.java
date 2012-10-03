/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package openwar.battle;

/**
 *
 * @author kehl
 */
public class SoldierMeleeStats {
    
      
    public enum Status
    {
        Idle,
        Stance,
        Shield,
        Shiltron      
    }   
    
    Status status;

    float timer;
    
    
    public SoldierMeleeStats()
    {

        timer=0;
        status = Status.Idle;
    }
    
    
}
