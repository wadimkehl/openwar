/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package openwar.DB;

import java.util.ArrayList;

/**
 *
 * @author kehl
 */
public class Faction {
    
    public String refName;
    public int gold;
    public String capital; // refname of the region of the capital!!!
    
    public ArrayList<Army> armies;
    
    public Faction()
    {
        armies = new ArrayList<Army>();
    }
    
}
