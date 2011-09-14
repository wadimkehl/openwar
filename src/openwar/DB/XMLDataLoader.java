/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package openwar.DB;

/**
 *
 * @author kehl
 */
import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.asset.AssetManager;
import com.jme3.math.ColorRGBA;
import java.io.File;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import openwar.DB.Building;
import openwar.DB.Faction;
import openwar.Main;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class XMLDataLoader {

    AssetManager assets;
    Main app;
    private static final Logger logger = Logger.getLogger(XMLDataLoader.class.getName());

    public XMLDataLoader(Main appl, AssetManager man) {
        app = appl;
        assets = man;
    }

    private void loadData(String folder) {

        try {
            // go into meta folder
            File f = new File(app.locatorRoot + folder);
            if (!f.isDirectory()) {
                logger.log(Level.SEVERE, "Cannot find {0} directory...", folder);
                throw new Exception();
            }

            // run through each folder
            for (File u : f.listFiles()) {
                if (!f.isDirectory()) {
                    logger.log(Level.WARNING, "Entity unloadable in {0}", f.getName());
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
                    logger.log(Level.WARNING, "Cannot find props.xml in {0}", u.getName());
                    continue;
                }

                // open props file and load everything into the database
                DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                DocumentBuilder db = dbf.newDocumentBuilder();
                Document dom = db.parse(props.getCanonicalPath());
                Element root = dom.getDocumentElement();

                if ("units".equals(folder)) {
                    loadUnit(root);
                } else if ("buildings".equals(folder)) {
                    loadBuilding(root);
                } else if ("factions".equals(folder)) {
                    loadFaction(root);
                }
            }
        } catch (Exception E) {
            logger.log(Level.WARNING, "Error while reading data...");
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
            logger.log(Level.WARNING, "Unit loaded: {0}", entity.refName);
        } catch (Exception E) {
            logger.log(Level.WARNING, "Unit CANNOT be loaded: {0}", entity.refName);
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
                String s = "buildings" + File.separator + entity.refName + File.separator;
                entity.addLevel(Integer.parseInt(l.getAttribute("level")),
                        l.getAttribute("name"), l.getAttribute("refname"),
                        Integer.parseInt(l.getAttribute("cost")),
                        Integer.parseInt(l.getAttribute("turns")),
                        assets.loadTexture(s + l.getAttribute("image")),
                        null);
                if (!"".equals(l.getAttribute("model"))) {
                    entity.levels.get(i).model = assets.loadModel(s + l.getAttribute("model"));
                }
                app.DB.buildings.put(entity.refName, entity);

            }
            logger.log(Level.WARNING, "Building loaded: {0}", entity.refName);
        } catch (Exception E) {
            logger.log(Level.WARNING, "Building CANNOT be loaded: {0}", entity.refName);
        }
    }

    private void loadFaction(Element root) {
        Faction entity = new Faction();
        try {
            Element faction = (Element) root.getElementsByTagName("faction").item(0);
            Element images = (Element) root.getElementsByTagName("images").item(0);
            Element names = (Element) root.getElementsByTagName("names").item(0);

            entity.name = faction.getAttribute("name");
            entity.refName = faction.getAttribute("refname");
            Scanner s = new Scanner(faction.getAttribute("color"));
            float r = s.nextFloat();
            float g = s.nextFloat();
            float b = s.nextFloat();
            entity.color = new ColorRGBA(r, g, b, 255);

            entity.banner = assets.loadTexture("factions" + File.separator
                    + entity.refName + File.separator + images.getAttribute("banner"));
            entity.flag = assets.loadTexture("factions" + File.separator
                    + entity.refName + File.separator + images.getAttribute("flag"));
            entity.icon = assets.loadTexture("factions" + File.separator
                    + entity.refName + File.separator + images.getAttribute("icon"));


            NodeList males = names.getElementsByTagName("male");
            NodeList females = names.getElementsByTagName("female");

            for (int i = 0; i < males.getLength(); i++) {
                Element l = (Element) males.item(i);
                entity.namesMale.add(l.getAttribute("name"));
            }
            for (int i = 0; i < females.getLength(); i++) {
                Element l = (Element) females.item(i);
                entity.namesFemale.add(l.getAttribute("name"));
            }
            logger.log(Level.WARNING, "Faction loaded: {0}", entity.refName);
        } catch (Exception E) {
            logger.log(Level.WARNING, "Faction CANNOT be loaded: {0}", entity.refName);
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
