/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package openwar.DB;

/**
 *
 * @author kehl
 */
public class Building {
    
    public String refName;
    public int level;
    
    public Building()
    {}
    
    public Building(String ref, int l)
    {
        refName = ref;
        level = l;
    }
    
}
