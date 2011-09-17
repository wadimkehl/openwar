/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package openwar.DB;

/**
 *
 * @author kehl
 */
import com.jme3.asset.AssetManager;
import com.jme3.asset.TextureKey;
import com.jme3.math.Vector3f;
import com.jme3.texture.Image.Format;
import java.io.File;
import java.util.Locale;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import openwar.Main;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class XMLDataLoader {

    AssetManager assets;
    Main app;
    private static final Logger logger = Logger.getLogger(XMLDataLoader.class.getName());

    public XMLDataLoader(Main appl) {
        app = appl;
        assets = appl.getAssetManager();
    }

    private void loadMultiData(String folder) {

        try {
            // go into meta folder
            File f = new File(app.locatorRoot + folder);
            if (!f.isDirectory()) {
                logger.log(Level.SEVERE, "Cannot find {0} directory...", folder);
                throw new Exception();
            }


            // if we load the map, check in meta folder for props.xml
            if ("map".equals(folder)) {
                // search props.xml
                File props = null;
                for (File l : f.listFiles()) {
                    if ("props.xml".equals(l.getName())) {
                        props = l;
                    }
                }
                if (props == null) {
                    logger.log(Level.WARNING, "Cannot find props.xml in {0}", f.getName());
                    return;
                }

                // open props file and load everything into the database
                DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                DocumentBuilder db = dbf.newDocumentBuilder();
                Document dom = db.parse(props.getCanonicalPath());
                loadMap(dom.getDocumentElement());
                return;
            }

            // else run through each subfolder
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
                    logger.log(Level.WARNING, "Cannot find props.xml in {0}", f.getName() + "/" + u.getName());
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
            logger.log(Level.SEVERE, "Error while reading {0} data...", folder);
        }
    }

    private void loadUnit(Element root) {
        GenericUnit entity = new GenericUnit();
        try {
            Element unit = (Element) root.getElementsByTagName("unit").item(0);
            Element stats = (Element) root.getElementsByTagName("stats").item(0);
            entity.name = unit.getAttribute("name");
            entity.refName = unit.getAttribute("refname");
            entity.maxCount = Integer.parseInt(unit.getAttribute("maxcount"));
            entity.maxMovePoints = Integer.parseInt(unit.getAttribute("maxmovepoints"));
            String image = "units" + File.separator + entity.refName + File.separator + unit.getAttribute("image");
            entity.image = assets.loadTexture(image);
            Main.DB.genUnits.put(entity.refName, entity);
            logger.log(Level.WARNING, "*Unit loaded: {0} *", entity.refName);
        } catch (Exception E) {
            logger.log(Level.SEVERE, "Unit CANNOT be loaded: {0}", entity.refName);
        }
    }

    private void loadBuilding(Element root) {
        GenericBuilding entity = new GenericBuilding();
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
                Main.DB.genBuildings.put(entity.refName, entity);

            }
            logger.log(Level.WARNING, "*Building loaded: {0} *", entity.refName);
        } catch (Exception E) {
            logger.log(Level.SEVERE, "Building CANNOT be loaded: {0}", entity.refName);
        }
    }

    private void loadFaction(Element root) {
        GenericFaction entity = new GenericFaction();
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
            entity.color = new Vector3f(r, g, b);

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
            Main.DB.genFactions.put(entity.refName, entity);
            logger.log(Level.WARNING, "*Faction loaded: {0} *", entity.refName);
        } catch (Exception E) {
            logger.log(Level.SEVERE, "Faction CANNOT be loaded: {0}", entity.refName);
        }
    }

    private void loadMap(Element root) {
        try {
            Element textures = (Element) root.getElementsByTagName("textures").item(0);
            Element terrain = (Element) root.getElementsByTagName("terrain").item(0);
            Element climates = (Element) root.getElementsByTagName("climates").item(0);
            Element regions = (Element) root.getElementsByTagName("regions").item(0);


            Main.DB.map.tilesTexturesCount = Integer.parseInt(textures.getAttribute("tiletextures"));
            NodeList tiletexs = textures.getElementsByTagName("tile");
            NodeList basetexs = textures.getElementsByTagName("base");


            for (int i = 0; i < tiletexs.getLength(); i++) {
                Element l = (Element) tiletexs.item(i);
                Main.DB.map.tileTextures.add(Integer.parseInt(l.getAttribute("id")),
                        assets.loadTexture("map" + File.separator + "textures"
                        + File.separator + l.getAttribute("texture")));
                Main.DB.map.tileTextures_scales.add(Float.parseFloat(l.getAttribute("scale")));
            }


            for (int i = 0; i < basetexs.getLength(); i++) {
                Element l = (Element) basetexs.item(i);
                String path = "map" + File.separator + "base"
                        + File.separator + l.getAttribute("texture");
                if ("regions".equals(l.getAttribute("name"))) {
                    Main.DB.map.regionsTex = assets.loadTexture(new TextureKey(path, true));
                } else if ("types".equals(l.getAttribute("name"))) {
                    Main.DB.map.typesTex = assets.loadTexture(new TextureKey(path, true));
                } else if ("climates".equals(l.getAttribute("name"))) {
                    Main.DB.map.climatesTex = assets.loadTexture(new TextureKey(path, true));
                } else if ("heights".equals(l.getAttribute("name"))) {
                    Main.DB.map.heightmapTex = assets.loadTexture(new TextureKey(path, true));

                }
            }

            Main.DB.map.regionsTex.getImage().setFormat(Format.RGB8);
            Main.DB.map.heightmapTex.getImage().setFormat(Format.RGB8);
            Main.DB.map.typesTex.getImage().setFormat(Format.RGB8);
            Main.DB.map.climatesTex.getImage().setFormat(Format.RGB8);



            NodeList t = terrain.getElementsByTagName("tile");
            for (int i = 0; i < t.getLength(); i++) {
                GenericTile tile = new GenericTile();
                Element l = (Element) t.item(i);
                tile.name = l.getAttribute("name");
                tile.type = Integer.parseInt(l.getAttribute("type"));
                Scanner s = new Scanner(l.getAttribute("color"));
                tile.color = new Vector3f(s.nextFloat(), s.nextFloat(), s.nextFloat());
                tile.cost = Integer.parseInt(l.getAttribute("cost"));
                tile.walkable = Boolean.parseBoolean(l.getAttribute("walkable"));
                tile.sailable = Boolean.parseBoolean(l.getAttribute("sailable"));
                tile.textureid = Integer.parseInt(l.getAttribute("textureid"));
                Main.DB.map.tiles.put(tile.type, tile);
            }




            Element hm = (Element) terrain.getElementsByTagName("heightmap").item(0);
            Main.DB.map.terrain.heightmap.factor0 = Float.parseFloat(hm.getAttribute("factor0"));
            Main.DB.map.terrain.heightmap.factor1 = Float.parseFloat(hm.getAttribute("factor1"));
            Main.DB.map.terrain.heightmap.offset = Float.parseFloat(hm.getAttribute("offset"));

            Element sun = (Element) terrain.getElementsByTagName("sun").item(0);
            Scanner s = new Scanner(sun.getAttribute("color"));
            Main.DB.map.terrain.sun.color = new Vector3f(s.nextFloat(), s.nextFloat(), s.nextFloat());
            s = new Scanner(sun.getAttribute("direction"));
            s.useLocale(Locale.ENGLISH);
            Main.DB.map.terrain.sun.direction =
                    new Vector3f(s.nextFloat(), s.nextFloat(), s.nextFloat());

            NodeList c = climates.getElementsByTagName("climate");
            for (int i = 0; i < c.getLength(); i++) {
                Element l = (Element) c.item(i);

                Climate cl = new Climate();
                s = new Scanner(l.getAttribute("color"));
                cl.name = l.getAttribute("name");
                cl.refName = l.getAttribute("refname");
                cl.color = new Vector3f(s.nextFloat(), s.nextFloat(), s.nextFloat());
                Main.DB.climates.add(cl);
                Main.DB.hashedClimates.put(cl.refName, cl);

            }

            c = regions.getElementsByTagName("region");
            for (int i = 0; i < c.getLength(); i++) {
                Element r = (Element) c.item(i);

                Region reg = new Region();
                reg.name = r.getAttribute("name");
                reg.refName = r.getAttribute("refname");
                s = new Scanner(r.getAttribute("color"));
                reg.color = new Vector3f(s.nextFloat(), s.nextFloat(), s.nextFloat());
                reg.owner = r.getAttribute("owner");
                Main.DB.regions.add(reg);
                Main.DB.hashedRegions.put(reg.refName, reg);

                if (r.getElementsByTagName("settlement").getLength() > 0) {
                    Element sett = (Element) r.getElementsByTagName("settlement").item(0);
                    Settlement se = new Settlement();
                    se.name = sett.getAttribute("name");
                    se.posX = Integer.parseInt(sett.getAttribute("posx"));
                    se.posZ = Integer.parseInt(sett.getAttribute("posz"));
                    se.level = Integer.parseInt(sett.getAttribute("level"));
                    se.population = Integer.parseInt(sett.getAttribute("population"));
                    reg.settlement = se;
                    Main.DB.settlements.add(se);
                    Main.DB.hashedSettlements.put(reg.refName, se);
                }



            }

            logger.log(Level.WARNING, "*Map loaded*");
        } catch (Exception E) {
            logger.log(Level.SEVERE, "Map CANNOT be loaded");
        }
    }

    public boolean loadAll() {

        try {
            
            logger.log(Level.WARNING, "Loading factions");
            loadMultiData("factions");
            logger.log(Level.WARNING, "Loading units");
            loadMultiData("units");
            logger.log(Level.WARNING, "Loading buildings");
            loadMultiData("buildings");
            
            logger.log(Level.WARNING, "Loading map");
            loadMultiData("map");

        } catch (Exception E) {
            return false;
        }


        return true;
    }
}
