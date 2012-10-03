/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package openwar.battle;

/**
 *
 * @author kehl
 */
public class SoldierRangeStats {
    
        
    public enum Status
    {
        Idle,
        SettingUp,
        Aiming,
        Shooting,
        UnAiming,
        Reloading,
        SettingDown
    }   
    Status status;
    
    float timer;

    
        
    public SoldierRangeStats()
    {

        timer=0;
        status = Status.Idle;
    }
    
}
