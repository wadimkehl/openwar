/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package openwar.battle;

import com.jme3.material.Material;
import com.jme3.material.RenderState.BlendMode;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue.Bucket;
import com.jme3.renderer.queue.RenderQueue.ShadowMode;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Cylinder;
import com.jme3.scene.shape.Quad;
import com.jme3.scene.shape.Sphere;
import com.jme3.terrain.geomipmap.TerrainQuad;

/**
 *
 * @author kehl
 */
public class Soldier {

    public enum Status {

        Idle,
        Walk,
        Run,
        Attack_Melee,
        Fight_Melee,
        Attack_Range,
        Fight_Range,
        Fall,
        Dead
    }
    Spatial model, selectionQuad;
    Node node;
    Unit unit;
    Status status;
    Material mat;
    public float hp = 1f;
    Vector2f pos, dir, goal;
    // Cylinder collision shape
    float height = 1.8f;
    float radius = 0.5f;
    TerrainQuad terrain;

    public Soldier(Unit ref) {
        unit = ref;
        status = Status.Idle;
        pos = new Vector2f();
        goal = new Vector2f();

    }

    public void createData() {
        
        terrain = unit.battle.terrain.terrainQuad;

        
        //model = unit.battle.game.getAssetManager().loadModel("models/" + Main.DB.cultures.get(0).armyModel);
        model = (Spatial) new Geometry("", new Cylinder(16,16,radius,height,true));
        mat = new Material(unit.battle.game.getAssetManager(), "Common/MatDefs/Light/Lighting.j3md");
        model.setMaterial(mat);
        model.setShadowMode(ShadowMode.CastAndReceive);
        model.setLocalRotation(new Quaternion().fromAngleAxis(FastMath.HALF_PI, Vector3f.UNIT_X));
        model.setLocalTranslation(0, height*0.5f, 0);

        selectionQuad = (Spatial) new Geometry("", new Quad(1f, 1f));
        Material m = new Material(unit.battle.game.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
        m.setTexture("ColorMap", unit.battle.game.getAssetManager().loadTexture("factions/rebels/icon.png"));
        m.getAdditionalRenderState().setBlendMode(BlendMode.Alpha);
        selectionQuad.setQueueBucket(Bucket.Transparent);
        selectionQuad.setMaterial(m);
        selectionQuad.setLocalRotation(new Quaternion().fromAngleAxis(FastMath.HALF_PI, Vector3f.UNIT_Z));
        selectionQuad.setLocalTranslation(0, 5f, 0);

        node = new Node("soldier");
        node.attachChild(model);


    }
    
    public void select(boolean on)
    {
     if(on)
         mat.setTexture("DiffuseMap", unit.battle.game.getAssetManager().loadTexture("factions/rebels/icon.png"));
     else
         mat.setTexture("DiffuseMap", null);
    }
    public void setPosition(float x, float z) {
        pos.x = x;
        pos.y = z;
        float y = terrain.getHeight(pos);
        node.setLocalTranslation(pos.x, y, pos.y);
    }

    public void setGoal(float x, float z, boolean run) {
        goal.x = x;
        goal.y = z;
        
        if(run)
            status = Status.Run;
        else
            status = Status.Walk;
    }

    public void update(float tpf) {



        switch (status) {

            case Idle:
                // check for different things? enemy engages or sth,
                break;

            case Walk:
                dir = (goal.subtract(pos));
                if (dir.lengthSquared() < 0.005f) {
                    status = Status.Idle;
                }
                dir.normalizeLocal();
                pos.addLocal(dir.multLocal(tpf));
                node.setLocalTranslation(pos.x, terrain.getHeight(pos), pos.y);
                break;

            case Run:
                dir = (goal.subtract(pos));
                if (dir.lengthSquared() < 0.005f) {
                    status = Status.Idle;
                }
                dir.normalizeLocal();
                pos.addLocal(dir.multLocal(tpf * 2f));
                node.setLocalTranslation(pos.x, terrain.getHeight(pos), pos.y);
                break;


        }
    }
}
