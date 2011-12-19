/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package openwar.DB;

import com.jme3.font.BitmapText;
import com.jme3.material.Material;
import com.jme3.material.RenderState.BlendMode;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue.Bucket;
import com.jme3.renderer.queue.RenderQueue.ShadowMode;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.BillboardControl;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Quad;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import openwar.Main;
import openwar.world.WorldEntity;
import openwar.world.WorldMap;

/**
 *
 * @author kehl
 */
public class Settlement extends WorldEntity {

    public class Statistics {

        int population, total_income, total_growth;
        public double base_growth, total_order;
        public double tax_rate;
        public HashMap<String, Double> growth_modifier;
        public HashMap<String, Double> income_modifier;
        public HashMap<String, Integer> income_adder;
        public HashMap<String, Double> order_modifier;

        public int computeIncome() {
            double factor = tax_rate;

            for (Double d : income_modifier.values()) {
                factor += d;
            }

            total_income = (int) (population * factor);

            for (Integer i : income_adder.values()) {
                total_income += i;
            }

            return total_income;
        }

        public int computeGrowth() {
            double factor = base_growth;

            for (Double d : growth_modifier.values()) {
                factor += d;
            }

            total_growth = (int) (population * factor);
            return total_growth;
        }

        public double computeOrder() {
            total_order = 1;

            for (Double d : order_modifier.values()) {
                total_order += d;
            }

            return total_order;
        }

        public Statistics() {
            growth_modifier = new HashMap<String, Double>();
            income_modifier = new HashMap<String, Double>();
            income_adder = new HashMap<String, Integer>();
            order_modifier = new HashMap<String, Double>();

        }
    };
    public Statistics stats;
    public String name;
    public String region;
    public String culture;
    public int level;
    public ArrayList<Building> buildings;
    public Spatial billBoard;
    public ArrayList<Building> constructionList;
    public ArrayList<Unit> recruitmentList;
    public ArrayList<Building> constructionPool;
    public ArrayList<Unit> recruitmentPool;

    public Settlement() {
        super();
        buildings = new ArrayList<Building>();
        constructionList = new ArrayList<Building>();
        recruitmentList = new ArrayList<Unit>();
        constructionPool = new ArrayList<Building>();
        recruitmentPool = new ArrayList<Unit>();
        stats = new Statistics();

    }

    @Override
    public void createData(WorldMap m) {
        map = m;

        owner = Main.DB.hashedRegions.get(region).owner;

        //Spatial m = Main.DB.genBuildings.get("city").levels.get(level).model.clone();
        model = (Spatial) new Geometry("city", new Box(Vector3f.ZERO, 1.2f, 0.25f, 1.2f));
        Material mat = new Material(map.game.getAssetManager(), "Common/MatDefs/Light/Lighting.j3md");
        model.setMaterial(mat);
        model.setShadowMode(ShadowMode.CastAndReceive);
        model.setLocalTranslation(0f, 0.25f, 0f);
        node.attachChild(model);

        banner = (Spatial) new Geometry("", new Quad(1.5f, 3f));
        banner.setLocalTranslation(-0.5f, 1f, -0.5f);
        mat = new Material(map.game.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
        mat.getAdditionalRenderState().setBlendMode(BlendMode.Alpha);
        mat.setTexture("ColorMap", Main.DB.genFactions.get(owner).banner);
        banner.setQueueBucket(Bucket.Translucent);
        banner.setMaterial(mat);
        node.attachChild(banner);

        createBillBoard();


        node.setLocalTranslation(map.getGLTileCenter(posX, posZ));
        map.scene.attachChild(node);

    }

    public void createBillBoard() {


        BitmapText label = new BitmapText(map.game.getAssetManager().loadFont("ui/fonts/palatino.fnt"), false);
        label.setSize(1f);
        label.setText(name);
        float width = label.getLineWidth();
        float height = label.getLineHeight();

        label.setColor(ColorRGBA.Black);
        label.setQueueBucket(Bucket.Translucent);

        label.setLocalTranslation(-1.25f, 7 - height / 2, 0.0001f);
        label.addControl(new BillboardControl());
        node.attachChild(label);

        billBoard = new Geometry(name + "_billboard", new Quad(width + 0.5f, height + 0.5f, false));
        Material mat = new Material(map.game.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
        mat.getAdditionalRenderState().setBlendMode(BlendMode.Alpha);
//        mat.setTexture("ColorMap",game.getAssetManager().loadTexture("Textures/ColoredTex/Monkey.png"));
        billBoard.setMaterial(mat);
        billBoard.setQueueBucket(Bucket.Transparent);
        billBoard.addControl(new BillboardControl());
        billBoard.setLocalTranslation(-1.5f, 5, 0);

        node.attachChild(billBoard);



    }

    public void calculatePools() {

        constructionPool.clear();
        recruitmentPool.clear();

        for (String s : Main.DB.genBuildings.keySet()) {

            boolean processed = false;
            for (Building b : buildings) {
                if (b.refName.equals(s)) {
                    if (b.level < Main.DB.genBuildings.get(s).maxLevel) {
                        String eval = owner + "','" + region + "','" + s + "'," + b.level + 1;
                        boolean next_level = ((Boolean) map.game.doReturnScript("canBeBuilt('" + eval + ")"));
                        if (next_level) {
                            constructionPool.add(new Building(s, b.level + 1, 0));
                        }
                    }
                    processed = true;
                    break;
                }
            }

            if (!processed) {
                for (Building b : constructionList) {
                    if (b.refName.equals(s)) {
                        processed = true;
                        break;
                    }
                }
            }

            if (!processed) {
                String eval = owner + "','" + region + "','" + s + "',0";
                boolean next_level = ((Boolean) map.game.doReturnScript("canBeBuilt('" + eval + ")"));
                if (next_level) {
                    constructionPool.add(new Building(s, 0, 0));
                }
            }
        }
    }

    @Override
    public void update(float tpf) {
    }

    public Army dispatchArmy(ArrayList<Unit> split) {
        Army a = new Army();
        Main.DB.hashedFactions.get(owner).armies.add(a);
        a.owner = owner;
        a.posX = posX;
        a.posZ = posZ;
        mergeUnitsTo(a, split);
        a.createData(map);
        map.scene.attachChild(a.node);
        return a;
    }
}
