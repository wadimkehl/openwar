/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package openwar.DB;

import com.jme3.asset.AssetManager;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.font.Rectangle;
import com.jme3.material.Material;
import com.jme3.material.RenderState.BlendMode;
import com.jme3.math.ColorRGBA;
import com.jme3.renderer.queue.RenderQueue.Bucket;
import com.jme3.scene.Geometry;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.BillboardControl;
import com.jme3.scene.shape.Quad;
import java.util.ArrayList;
import openwar.Main;

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
    public Geometry billBoard;

    public Settlement() {
        buildings = new ArrayList<Building>();
        units = new ArrayList<Unit>();



    }

    public void createBillBoard(Main game) {



        BitmapFont fnt = game.getAssetManager().loadFont("ui/fonts/palatino.fnt");
        BitmapText label = new BitmapText(fnt, false);
        label.setSize(1f);
        label.setText(name.toUpperCase());
        float width = label.getLineWidth();
        float height = label.getLineHeight();

        label.setColor(ColorRGBA.Black);
        label.setQueueBucket(Bucket.Translucent);

        label.setLocalTranslation(posX - 1 + 0.25f, 5-height/2, posZ + 0.0001f);
        label.addControl(new BillboardControl());
        game.worldMapState.map.scene.attachChild(label);

        billBoard = new Geometry(name + "_billboard", new Quad(width+0.5f, height+0.5f, false));
        Material mat = new Material(game.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
        mat.getAdditionalRenderState().setBlendMode(BlendMode.Alpha);
//        mat.setTexture("ColorMap",game.getAssetManager().loadTexture("Textures/ColoredTex/Monkey.png"));
        mat.setTransparent(true);
        billBoard.setMaterial(mat);
        billBoard.setQueueBucket(Bucket.Transparent);
        billBoard.addControl(new BillboardControl());
        billBoard.setLocalTranslation(posX - 1f, 3, posZ);

        game.worldMapState.map.scene.attachChild(billBoard);



    }

    public void updateBillBoard(Main game) {

        if (billBoard == null) {
            createBillBoard(game);
        }


    }
}
