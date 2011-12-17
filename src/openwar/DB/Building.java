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
    public int currentConstructionTurn;
    
    public Building()
    {}
    
    public Building(String ref, int l, int turn)
    {
        refName = ref;
        level = l;
        currentConstructionTurn = turn;
    }
    
}
