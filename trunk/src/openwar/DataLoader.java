/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package openwar;

/**
 *
 * @author kehl
 */
import com.jme3.asset.AssetManager;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DataLoader {

    AssetManager assets;
    String data;
    private static final Logger logger = Logger.getLogger(DataLoader.class.getName());

    public DataLoader(AssetManager man, String data_folder) {
        assets = man;
        data = data_folder;
    }

    private boolean LoadUnits() {
        try {
            
            // go into meta folder
            File f = new File(data + "/" + "units");
            if (!f.isDirectory()) {
                logger.log(Level.SEVERE, "Can't find units directory...");
                return false;
            }
            
            // run through each folder
            for (File u : f.listFiles()) {
                if (!f.isDirectory()) {
                    logger.log(Level.SEVERE, "Unit unloadable in {0}.", f.getName());
                    continue;
                }

                // search props.xml
                File props=null;
                for (File l : u.listFiles()) {
                    if ("props.xml".equals(l.getName())) {
                        props = l;
                    }
                }
                
                if(props == null) 
                {
                    logger.log(Level.SEVERE, "Can't find props.xml in {0}.", u.getName());
                    continue;
                }

                // open props file and load everything into the game
                
                
            }
        } catch (Exception E) {
            logger.log(Level.SEVERE, "Error while reading units data...");
            return false;
        }


        return true;
    }

    public boolean LoadAll() {
        
        return true;
    }
}
