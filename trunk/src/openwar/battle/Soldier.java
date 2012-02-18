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
    Vector2f currPos, currDir, goalPos, goalDir;
    // Cylinder collision shape
    float height = 1.8f;
    float radius = 0.5f;
    TerrainQuad terrain;

    public Soldier(Unit ref) {
        unit = ref;
        status = Status.Idle;
        currPos = new Vector2f();
        currDir = new Vector2f();
        goalPos = new Vector2f();
        goalDir = new Vector2f();

    }

    public void createData() {

        terrain = unit.battle.terrain.terrainQuad;


        //model = unit.battle.game.getAssetManager().loadModel("models/" + Main.DB.cultures.get(0).armyModel);
        model = (Spatial) new Geometry("", new Cylinder(16, 16, radius, height, true));
        mat = new Material(unit.battle.game.getAssetManager(), "Common/MatDefs/Light/Lighting.j3md");
        model.setMaterial(mat);
        model.setShadowMode(ShadowMode.CastAndReceive);
        model.setLocalRotation(new Quaternion().fromAngleAxis(FastMath.HALF_PI, Vector3f.UNIT_X));
        model.setLocalTranslation(0, height * 0.5f, 0);

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

    public void select(boolean on) {
        if (on) {
            mat.setTexture("DiffuseMap", unit.battle.game.getAssetManager().loadTexture("factions/rebels/icon.png"));
        } else {
            mat.setTexture("DiffuseMap", null);
        }
    }

    
    public void setPosition(Vector2f pos, Vector2f dir) {
        setPosition(pos.x,pos.y,dir.x,dir.y);
    }

    
    public void setPosition(float x, float z, float dx, float dz) {
        currPos.x = x;
        currPos.y = z;
        currDir.x =dx;
        currDir.y = dz;
        goalPos = currPos.clone();
        goalDir = currDir.clone();
        float y = terrain.getHeight(currPos);
        node.setLocalTranslation(currPos.x, y, currPos.y);
        node.setLocalRotation(new Quaternion().fromAngleAxis(FastMath.atan2(dx,dz),
                Vector3f.UNIT_Y));
                    
    }

    public void setGoal(float x, float z, float dx, float dz, boolean run) {
        goalPos.x = x;
        goalPos.y = z;
        goalDir.x = dx;
        goalDir.y = dz;
        
        if (run) {
            status = Status.Run;
        } else {
            status = Status.Walk;
        }
    }

    public void setGoal(Vector2f pos, Vector2f dir, boolean run) {
        setGoal(pos.x,pos.y,dir.x,dir.y,run);
    }

    public void update(float tpf) {



        switch (status) {

            case Idle:

                // When idle, make sure to look into the right direction
                float angle = goalDir.angleBetween(currDir);
                if (FastMath.abs(angle) > 0.1f) {
                    float new_angle = FastMath.atan2(currDir.x, currDir.y);
                    new_angle -= -FastMath.sign(angle) * 0.1f * tpf;
                    node.setLocalRotation(
                            new Quaternion().fromAngleAxis(FastMath.DEG_TO_RAD * new_angle, Vector3f.UNIT_Y));
                    currDir.x = FastMath.cos(new_angle);
                    currDir.y = FastMath.sin(new_angle);

                }


                // check for different things? enemy engages or sth,
                break;

            case Walk:
                currDir = (goalPos.subtract(currPos));
                if (currDir.lengthSquared() < 0.005f) {
                    status = Status.Idle;
                }
                currDir.normalizeLocal();
                currPos.addLocal(currDir.multLocal(tpf));
                node.setLocalTranslation(currPos.x, terrain.getHeight(currPos), currPos.y);
                break;

            case Run:
                currDir = (goalPos.subtract(currPos));
                if (currDir.lengthSquared() < 0.005f) {
                    status = Status.Idle;
                }
                currDir.normalizeLocal();
                currPos.addLocal(currDir.multLocal(tpf * 2f));
                node.setLocalTranslation(currPos.x, terrain.getHeight(currPos), currPos.y);
                break;


        }
    }
}
