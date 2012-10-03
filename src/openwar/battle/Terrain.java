/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package openwar.battle;

import com.jme3.bullet.collision.PhysicsCollisionObject;
import com.jme3.bullet.collision.shapes.HeightfieldCollisionShape;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.material.Material;
import com.jme3.renderer.queue.RenderQueue.ShadowMode;
import com.jme3.terrain.geomipmap.TerrainLodControl;
import com.jme3.terrain.geomipmap.TerrainQuad;
import com.jme3.terrain.heightmap.AbstractHeightMap;
import com.jme3.terrain.heightmap.HillHeightMap;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author kehl
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
            Random ran = new Random();
            heightmap = new HillHeightMap(size + 1, 1000, 50, 100, ran.nextInt());
        } catch (Exception ex) {
            Logger.getLogger(Terrain.class.getName()).log(Level.SEVERE, null, ex);
        }


        terrainQuad = new TerrainQuad("terrain", 65, heightmap.getSize(), heightmap.getHeightMap());
        Material mat_terrain = new Material(battle.game.getAssetManager(), "Common/MatDefs/Light/Lighting.j3md");
        mat_terrain.setTexture("DiffuseMap", battle.game.getAssetManager().loadTexture("map/1.tga"));
        terrainQuad.setMaterial(mat_terrain);
        terrainQuad.setLocalScale(1f, 0.25f, 1f);
        
        collShape = new HeightfieldCollisionShape(heightmap.getHeightMap(), terrainQuad.getLocalScale());
        bodyControl = new RigidBodyControl(collShape,0);
        terrainQuad.addControl(bodyControl);                
        battle.game.bulletState.getPhysicsSpace().add(terrainQuad);

        bodyControl.setCollisionGroup(PhysicsCollisionObject.COLLISION_GROUP_01);         
        TerrainLodControl control = new TerrainLodControl(terrainQuad, battle.game.getCamera());
        terrainQuad.addControl(control);

        terrainQuad.setShadowMode(ShadowMode.Receive);

        


    }
}
