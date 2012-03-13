/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package openwar.DB;

import java.util.Map;

/**
 *
 * @author kehl
 */
public class GenericUnit {

    public String refName;
    public String name;
    public int maxCount;
    public int maxMovePoints;
    public int turnsToRecruit;
    public int cost,upkeep;
    public boolean walks, sails, cargo;
    public Map<String, Integer> stats;
    public Description desc;
}