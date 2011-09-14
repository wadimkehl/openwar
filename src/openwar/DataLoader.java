/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package openwar;

/**
 *
 * @author kehl
 */
import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.asset.AssetManager;
import java.io.File;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import openwar.DB.Building;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class DataLoader {

    AssetManager assets;
    Main app;
    private static final Logger logger = Logger.getLogger(DataLoader.class.getName());

    public DataLoader(Main appl, AssetManager man) {
        app = appl;
        assets = man;
    }

    private void loadData(String folder) {

        try {
            // go into meta folder
            File f = new File(app.locatorRoot + folder);
            if (!f.isDirectory()) {
                logger.log(Level.SEVERE, "Can't find " + folder + " directory...");
                throw new Exception();
            }

            // run through each folder
            for (File u : f.listFiles()) {
                if (!f.isDirectory()) {
                    logger.log(Level.WARNING, "Entity unloadable in " + f.getName());
                    continue;
                }
                // search props.xml
                File props = null;
                for (File l : u.listFiles()) {
                    if ("props.xml".equals(l.getName())) {
                        props = l;
                    }
                }
                if (props == null) {
                    logger.log(Level.WARNING, "Can't find props.xml in " + u.getName());
                    continue;
                }

                // open props file and load everything into the database
                DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                DocumentBuilder db = dbf.newDocumentBuilder();
                Document dom = db.parse(props.getCanonicalPath());
                Element root = dom.getDocumentElement();

                if (folder == "units") {
                    loadUnit(root);
                } else if (folder == "buildings") {
                    loadBuilding(root);
                }
            }
        } catch (Exception E) {
            logger.log(Level.SEVERE, "Error while reading data...");
        }
    }

    private void loadUnit(Element root) {
        openwar.DB.Unit entity = new openwar.DB.Unit();
        try {
            Element unit = (Element) root.getElementsByTagName("unit").item(0);
            Element stats = (Element) root.getElementsByTagName("stats").item(0);
            entity.name = unit.getAttribute("name");
            entity.refName = unit.getAttribute("refname");
            entity.maxCount = Integer.parseInt(unit.getAttribute("maxcount"));
            entity.maxMovePoints = Integer.parseInt(unit.getAttribute("maxmovepoints"));
            String image = "units" + File.separator + entity.refName + File.separator + unit.getAttribute("image");
            entity.image = assets.loadTexture(image);
            app.DB.units.put(entity.refName, entity);
            logger.log(Level.INFO, "Unit loaded: " + entity.refName);
        } catch (Exception E) {
            logger.log(Level.INFO, "Unit CANNOT be loaded: " + entity.refName);
        }
    }

    private void loadBuilding(Element root) {
        Building entity = new Building();
        try {
            Element building = (Element) root.getElementsByTagName("building").item(0);
            Element levels = (Element) root.getElementsByTagName("levels").item(0);
            entity.name = building.getAttribute("name");
            entity.refName = building.getAttribute("refname");
            entity.maxLevel = Integer.parseInt(building.getAttribute("maxlevel"));

            for (int i = 0; i < entity.maxLevel; i++) {
                Element l = (Element) levels.getElementsByTagName("level").item(i);
                String s = "buildings" + File.separator + entity.refName
                        + File.separator + l.getAttribute("image");
                entity.addLevel(Integer.parseInt(l.getAttribute("level")),
                        l.getAttribute("name"), l.getAttribute("refname"),
                        Integer.parseInt(l.getAttribute("cost")),
                        Integer.parseInt(l.getAttribute("turns")),
                        assets.loadTexture(s));
                app.DB.buildings.put(entity.refName, entity);

            }
            logger.log(Level.INFO, "Building loaded: " + entity.refName);
        } catch (Exception E) {
            logger.log(Level.INFO, "Building CANNOT be loaded: " + entity.refName);
        }
    }

    public boolean loadAll() {

        try {
            loadData("factions");
            loadData("units");
            loadData("buildings");
        } catch (Exception E) {
            return false;
        }


        return true;
    }
}
