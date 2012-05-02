/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package openwar.battle;

import com.jme3.bullet.collision.PhysicsCollisionObject;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.collision.shapes.CylinderCollisionShape;
import com.jme3.bullet.collision.shapes.SphereCollisionShape;
import com.jme3.bullet.control.GhostControl;
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
import com.jme3.scene.shape.Dome;
import com.jme3.scene.shape.Quad;
import com.jme3.scene.shape.Sphere;
import com.jme3.terrain.geomipmap.TerrainQuad;
import java.util.ArrayList;

/**
 *
 * @author kehl
 */
public class Soldier {

    public enum Status {

        Idle,
        TurnToMove,
        Move,
        Attack_Melee,
        Fight_Melee,
        Attack_Range,
        Fight_Range,
        MidAir,
        Dead
    }
    
    Spatial model, selectionQuad, previewQuad;
    Node node;
    Unit unit;
    Status status;
    Material mat;
    public float hp = 1f;
    public Vector2f currPos, currDir, goalPos, goalDir, walkDir, previewPos;
    // Cylinder collision shape
    float height = 1.8f;
    float radius = 0.4f;
    TerrainQuad terrain;
    Node cone;
    CollisionShape collShape, rangeShape, meleeShape;
    GhostControl collControl, rangeControl, meleeControl;
    Vector2f collVec;
    boolean collision;
    int loopCounter;

    public Soldier(Unit ref) {
        unit = ref;
        status = Status.Idle;
        currPos = new Vector2f();
        currDir = new Vector2f();
        goalPos = new Vector2f();
        goalDir = new Vector2f();
        walkDir = new Vector2f();
        collVec = new Vector2f();
        previewPos = new Vector2f();


    }

    public void createData() {

        terrain = unit.battle.terrain.terrainQuad;


        //model = unit.battle.game.getAssetManager().loadModel("models/" + Main.DB.cultures.get(0).armyModel);
        model = (Spatial) new Geometry("", new Cylinder(16, 16, radius, height, true));
        mat = new Material(unit.battle.game.getAssetManager(), "Common/MatDefs/Light/Lighting.j3md");
        model.setMaterial(mat);
        model.setShadowMode(ShadowMode.CastAndReceive);
        model.setLocalRotation(new Quaternion().fromAngleAxis(FastMath.HALF_PI, Vector3f.UNIT_X));
        //model.setLocalTranslation(0, height * 0.5f, 0);

        cone = new Node("");
        Spatial dome = (Spatial) new Geometry("", new Dome(Vector3f.ZERO, 2, 12, 0.2f, false));
        dome.setLocalScale(1, 5, 1);

        cone.attachChild(dome);
        cone.attachChild((Spatial) new Geometry("", new Sphere(12, 12, 0.2f)));
        cone.setMaterial(new Material(unit.battle.game.getAssetManager(), "Common/MatDefs/Light/Lighting.j3md"));
        Quaternion q = new Quaternion().fromAngleAxis(FastMath.HALF_PI, Vector3f.UNIT_Z);
        Quaternion l = new Quaternion().fromAngleAxis(FastMath.atan2(currDir.y, currDir.x), Vector3f.UNIT_Y);
        cone.setLocalRotation(l.multLocal(q));
        cone.setLocalTranslation(0, 2, 0);

        selectionQuad = (Spatial) new Geometry("", new Quad(radius*2.5f, radius*2.5f));
        Material m = new Material(unit.battle.game.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
        m.setTexture("ColorMap", unit.battle.game.getAssetManager().loadTexture("textures/selection.png"));
        m.getAdditionalRenderState().setBlendMode(BlendMode.Alpha);
        selectionQuad.setQueueBucket(Bucket.Transparent);
        m.getAdditionalRenderState().setDepthWrite(false);
        selectionQuad.setMaterial(m);
        selectionQuad.setLocalRotation(new Quaternion().fromAngleAxis(-FastMath.HALF_PI, Vector3f.UNIT_X));
        selectionQuad.setLocalTranslation(-(radius+0.1f), -height/2f+0.1f, (radius+0.1f));


        previewQuad = (Spatial) new Geometry("", new Quad(radius*2.5f, radius*2.5f));
        previewQuad.setQueueBucket(Bucket.Transparent);
        previewQuad.setMaterial(m);
        previewQuad.setLocalRotation(new Quaternion().fromAngleAxis(-FastMath.HALF_PI, Vector3f.UNIT_X));

        
        node = new Node("soldier");
        node.attachChild(model);
        node.attachChild(cone);

        collShape = new CylinderCollisionShape(new Vector3f(radius*0.75f, height / 2f, radius*0.75f), 1);
        meleeShape = new SphereCollisionShape(10);
        rangeShape = new SphereCollisionShape(100);

        collControl = new GhostControl(collShape);
        meleeControl =new GhostControl(meleeShape);
        rangeControl = new GhostControl(rangeShape);
                
        
        collControl.setCollideWithGroups(PhysicsCollisionObject.COLLISION_GROUP_01 | 
                                         PhysicsCollisionObject.COLLISION_GROUP_02);
        meleeControl.setCollisionGroup(PhysicsCollisionObject.COLLISION_GROUP_03);
        rangeControl.setCollisionGroup(PhysicsCollisionObject.COLLISION_GROUP_03);
        
        if("A".equals(unit.owner))
        {            
            collControl.setCollisionGroup(PhysicsCollisionObject.COLLISION_GROUP_01);        
            meleeControl.setCollideWithGroups(PhysicsCollisionObject.COLLISION_GROUP_02);
            rangeControl.setCollideWithGroups(PhysicsCollisionObject.COLLISION_GROUP_02);
        }
        else
        {
            collControl.setCollisionGroup(PhysicsCollisionObject.COLLISION_GROUP_02);
            meleeControl.setCollideWithGroups(PhysicsCollisionObject.COLLISION_GROUP_01);
            rangeControl.setCollideWithGroups(PhysicsCollisionObject.COLLISION_GROUP_01);  
        }
     
        
        
        unit.battle.game.bulletState.getPhysicsSpace().add(collControl);    
        unit.battle.game.bulletState.getPhysicsSpace().add(meleeControl);
        node.addControl(collControl);
        node.addControl(meleeControl);
        
        if(unit.rangeStats != null)
        {
           unit.battle.game.bulletState.getPhysicsSpace().add(rangeControl);
           node.addControl(rangeControl);
        }
        
        unit.battle.hashedSoldiers.put(node, this);


    }

    public void select(boolean on) {
        if (on) {
            node.attachChild(selectionQuad);
        } else {
            node.detachChild(selectionQuad);

        }
    }

    public void setPosition(Vector2f pos, Vector2f dir) {
        setPosition(pos.x, pos.y, dir.x, dir.y);
    }

    public void setPosition(float x, float z, float dx, float dz) {
        currPos.x = x;
        currPos.y = z;
        currDir.x = -dx;
        currDir.y = dz;
        currDir.normalizeLocal();
        goalPos = currPos.clone();
        goalDir = currDir.clone();
        node.setLocalTranslation(currPos.x, terrain.getHeight(currPos) + height * 0.5f, currPos.y);
        node.setLocalRotation(new Quaternion().fromAngleAxis(FastMath.atan2(dz, dx),
                Vector3f.UNIT_Y));

    }

    public void setGoal(float x, float z, float dx, float dz, boolean run) {
        goalPos.x = x;
        goalPos.y = z;
        goalDir.x = dx;
        goalDir.y = dz;
        goalDir.normalizeLocal();


        status = Status.Move;

    }

    public void setGoal(Vector2f pos, Vector2f dir, boolean run) {
        setGoal(pos.x, pos.y, dir.x, dir.y, run);
    }

    public boolean turnToGoalDir(float turnSpeed) {


        if (currDir.x * goalDir.x + currDir.y * goalDir.y > 0.99f) {
            return true;
        }



//        float goalAngle = FastMath.atan2(unit.goalDir.y, unit.goalDir.x);
//        if(goalAngle <0) goalAngle += FastMath.TWO_PI;
//        if(currAngle <0) currAngle += FastMath.TWO_PI;

//                

//        float angle = FastMath.acos(dot);
//        if(angle < 0.1f) return true;


        float currAngle = FastMath.atan2(currDir.y, currDir.x);
        float z = currDir.x * goalDir.y - currDir.y * goalDir.x;

        if (z > 0) {
            currAngle += turnSpeed;
        } else {
            currAngle -= turnSpeed;
        }



        currDir.x = FastMath.cos(currAngle);
        currDir.y = FastMath.sin(currAngle);

        node.setLocalRotation(
                new Quaternion().fromAngleAxis(currAngle, Vector3f.UNIT_Y));

        return false;

    }

    public boolean turnToWalkDir(float turnSpeed) {
        if (currDir.x * -walkDir.x + currDir.y * walkDir.y > 0.99f) {
            return true;
        }
        float currAngle = FastMath.atan2(currDir.y, currDir.x);
        float z = currDir.x * walkDir.y - currDir.y * -walkDir.x;

        if (z > 0) {
            currAngle += turnSpeed;
        } else {
            currAngle -= turnSpeed;
        }

        currDir.x = FastMath.cos(currAngle);
        currDir.y = FastMath.sin(currAngle);

        node.setLocalRotation(
                new Quaternion().fromAngleAxis(currAngle, Vector3f.UNIT_Y));

        return false;

    }

    public void update(float tpf) {

        
        loopCounter++;
        if (collision) {
            loopCounter = 0;
            currPos.addLocal(collVec.multLocal(tpf));
            node.setLocalTranslation(currPos.x, terrain.getHeight(currPos) + height * 0.5f, currPos.y);
            collision=false;

        } 
        switch (status) {

            case Idle:

                turnToGoalDir(0.03f);
                if (loopCounter > 100) {
                    loopCounter=0;
                    walkDir = goalPos.subtract(currPos);
                    if (walkDir.lengthSquared() > 0.1f) {
                        status = Status.Move;
                    }
                }
                break;

            case TurnToMove:
                if (turnToWalkDir(0.03f)) {
                    status = Status.Move;
                    break;
                }



            case Move:


                walkDir = goalPos.subtract(currPos);
                if (walkDir.lengthSquared() < 0.01f) {
                    status = Status.Idle;
                }
                walkDir.normalizeLocal();

                if (turnToWalkDir(0) == false) {
                    status = Status.TurnToMove;
                    break;
                }


                if (unit.run) {
                    currPos.addLocal(walkDir.mult(2 * tpf));
                } else {
                    currPos.addLocal(walkDir.mult(tpf));
                }
                node.setLocalTranslation(currPos.x, terrain.getHeight(currPos) + height * 0.5f, currPos.y);

                break;

        }



        if(unit.previewFormation)
        {
           previewQuad.setLocalTranslation(previewPos.x-(radius+0.1f), 
                   terrain.getHeight(previewPos)+0.1f, previewPos.y+(radius+0.1f));

        }

    }
}
