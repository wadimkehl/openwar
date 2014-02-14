/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package openwar.battle;

import com.jme3.bullet.collision.PhysicsCollisionObject;
import com.jme3.bullet.collision.shapes.HeightfieldCollisionShape;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.material.Material;
import com.jme3.math.Vector2f;
import com.jme3.renderer.queue.RenderQueue.ShadowMode;
import com.jme3.terrain.geomipmap.TerrainLodControl;
import com.jme3.terrain.geomipmap.TerrainQuad;
import com.jme3.terrain.heightmap.AbstractHeightMap;
import com.jme3.terrain.heightmap.HillHeightMap;
import com.jme3.terrain.heightmap.ImageBasedHeightMap;
import com.jme3.texture.Texture;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author kehl, hermetic
 */
public class Terrain {

    public AbstractHeightMap heightmap;
    public TerrainQuad terrainQuad;
    public BattleAppState battle;
    int size = 512;
    public RigidBodyControl bodyControl;
    public HeightfieldCollisionShape collShape;

    public Terrain(BattleAppState ba) {
        battle = ba;

    }

    public void createData() {
        try {
            /*Random ran = new Random();
            heightmap = new HillHeightMap(size + 1, 1000, 50, 100, ran.nextInt());*/
            Texture heightMapImage = battle.game.getAssetManager().loadTexture("textures/Terrain/mountains512.png");
            heightmap = new ImageBasedHeightMap(heightMapImage.getImage(), 0.25f);
            heightmap.load();

        } catch (Exception ex) {
            Logger.getLogger(Terrain.class.getName()).log(Level.SEVERE, null, ex);
        }


        terrainQuad = new TerrainQuad("terrain", 65, heightmap.getSize()+1, heightmap.getHeightMap());
        Material mat_terrain = new Material(battle.game.getAssetManager(), "Common/MatDefs/Light/Lighting.j3md");
        //mat_terrain.setTexture("DiffuseMap", battle.game.getAssetManager().loadTexture("map/1.tga"));
        // load terrain
        mat_terrain = new Material(battle.game.getAssetManager(),
                "Common/MatDefs/Terrain/TerrainLighting.j3md");
        mat_terrain.setBoolean("useTriPlanarMapping", false);
        mat_terrain.setBoolean("WardIso", true);
        mat_terrain.setTexture("AlphaMap", battle.game.getAssetManager().loadTexture(
                "textures/Terrain/alphamap.png"));
                Texture grass = battle.game.getAssetManager().loadTexture("textures/Terrain/grass.jpg");
        grass.setWrap(Texture.WrapMode.Repeat);
        mat_terrain.setTexture("DiffuseMap", grass);
        mat_terrain.setFloat("DiffuseMap_0_scale", 64);
        Texture dirt = battle.game.getAssetManager().loadTexture("textures/Terrain/dirt.jpg");
        dirt.setWrap(Texture.WrapMode.Repeat);
        mat_terrain.setTexture("DiffuseMap_1", dirt);
        mat_terrain.setFloat("DiffuseMap_1_scale", 16);
        Texture rock = battle.game.getAssetManager().loadTexture("textures/Terrain/road.jpg");
        rock.setWrap(Texture.WrapMode.Repeat);
        mat_terrain.setTexture("DiffuseMap_2", rock);
        mat_terrain.setFloat("DiffuseMap_2_scale", 128);
        Texture normalMap0 = battle.game.getAssetManager().loadTexture("textures/Terrain/grass_normal.jpg");
        normalMap0.setWrap(Texture.WrapMode.Repeat);
        Texture normalMap1 = battle.game.getAssetManager().loadTexture("textures/Terrain/dirt_normal.png");
        normalMap1.setWrap(Texture.WrapMode.Repeat);
        Texture normalMap2 = battle.game.getAssetManager().loadTexture("textures/Terrain/road_normal.png");
        normalMap2.setWrap(Texture.WrapMode.Repeat);
        mat_terrain.setTexture("NormalMap", normalMap0);
        mat_terrain.setTexture("NormalMap_1", normalMap2);
        mat_terrain.setTexture("NormalMap_2", normalMap2);

        
        terrainQuad.setMaterial(mat_terrain);
        terrainQuad.setLocalScale(1f, 0.5f, 1f);

        collShape = new HeightfieldCollisionShape(heightmap.getHeightMap(), terrainQuad.getLocalScale());
        bodyControl = new RigidBodyControl(collShape, 0);
        terrainQuad.addControl(bodyControl);
        battle.game.bulletState.getPhysicsSpace().add(terrainQuad);

        bodyControl.setCollisionGroup(PhysicsCollisionObject.COLLISION_GROUP_01);
        TerrainLodControl control = new TerrainLodControl(terrainQuad, battle.game.getCamera());
        terrainQuad.addControl(control);

        terrainQuad.setShadowMode(ShadowMode.Receive);




    }

    public float getHeight(float x, float z) {
        float y = terrainQuad.getHeight(new Vector2f(x, z));
        return y;
    }
}

