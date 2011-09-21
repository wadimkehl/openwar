/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package openwar.DB;

import com.jme3.scene.Spatial;
import java.util.ArrayList;

/**
 *
 * @author kehl
 */
public class Settlement {

    public String name;
    public int level, population, posX, posZ;
    public ArrayList<Building> buildings;
    public ArrayList<Unit> units;
    public Spatial model;

    public Settlement() {
        buildings = new ArrayList<Building>();
        units = new ArrayList<Unit>();

    }
}
