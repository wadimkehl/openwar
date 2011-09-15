/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package openwar.DB;

import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.texture.Texture;
import java.util.ArrayList;

/**
 *
 * @author kehl
 */
public class Faction {

    public String name;
    public String refName;
    public Vector3f color;
    public ArrayList<String> namesMale, namesFemale;
    public Texture banner, flag, icon;

    public Faction() {
        namesMale = new ArrayList < String > ();
        namesFemale = new ArrayList < String > ();

    }
}
