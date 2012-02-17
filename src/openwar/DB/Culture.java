/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package openwar.DB;

import java.util.HashMap;

/**
 *
 * @author kehl
 */
public class Culture {

    public String Name;
    public String refName;
    public String armyModel,fleetModel;

    public HashMap<Integer, String> settlementModels;
    public HashMap<Integer, String> dockModels;

    public Culture(String n, String r) {
        Name = n;
        refName = r;
        settlementModels = new HashMap<Integer, String>();
        dockModels = new HashMap<Integer, String>();

    }
}
