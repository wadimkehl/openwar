/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package openwar.battle;

import com.jme3.animation.AnimChannel;
import com.jme3.animation.AnimControl;
import com.jme3.animation.AnimEventListener;
import com.jme3.animation.LoopMode;
import com.jme3.bullet.collision.PhysicsCollisionObject;
import com.jme3.bullet.collision.shapes.CapsuleCollisionShape;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.collision.shapes.SphereCollisionShape;
import com.jme3.bullet.control.CharacterControl;
import com.jme3.bullet.control.GhostControl;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.light.AmbientLight;
import com.jme3.material.Material;
import com.jme3.material.RenderState.BlendMode;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.renderer.queue.RenderQueue.Bucket;
import com.jme3.renderer.queue.RenderQueue.ShadowMode;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.UpdateControl;
import com.jme3.scene.shape.Dome;
import com.jme3.scene.shape.Quad;
import com.jme3.scene.shape.Sphere;
import com.jme3.terrain.geomipmap.TerrainQuad;
import java.util.Random;
import java.util.concurrent.Callable;
import openwar.DB.GenericUnit;
import openwar.Main;

/**
 *
 * @author kehl, hermetic
 */
public class Soldier extends Node implements AnimEventListener{

    public enum Status {

        Idle,
        Stance,
        Reloading,
        Dead
    }

    public enum AnimStatus {

        Idle0, Idle1, Idle2,
        TurnIdle, TurnStance,
        WalkIdle, WalkStance,
    }

    private Camera cam;
 
    private CharacterControl characterControl;
    private AnimChannel animChannel;
    private AnimControl animControl;
 
    private boolean attack;
    private boolean attacking;
    private boolean takehit;
    
    private String idleAnim = "Idle1";
    private String walkAnim = "Walk";
    private String attackAnim = "Attack3";
     private String deadAnim = "Death1";
    private String jumpAnim = "Climb"; //hilarious
    
    Spatial model, selectionQuad, previewQuad;
    Node node;
    Unit unit;
    Status status;
    Material mat;
    public float hp = 1f;
    public Vector2f currPos, currDir, goalPos, goalDir, walkDir, previewPos;
    public Vector3f physPos,physDir, physWalk;
    Quaternion currRot;
    // Cylinder collision shape
    float height = 1.8f;
    float radius = 0.25f;
    float currSpeed = 0;
    TerrainQuad terrain;
    Node cone;
    CollisionShape collShape, rangeShape, meleeShape;
    RigidBodyControl collControl;
    GhostControl rangeControl, meleeControl;

    float fightTimer;
    Soldier enemy;
    SoldierMeleeStats meleeStats;
    SoldierRangeStats rangeStats;
    public boolean inMelee, pos_found;

    public Soldier(Unit ref) {
        unit = ref;
        status = Status.Idle;
        currPos = new Vector2f();
        currDir = new Vector2f();
        goalPos = new Vector2f();
        goalDir = new Vector2f();
        walkDir = new Vector2f();
        previewPos = new Vector2f();
        
        physPos = new Vector3f();
        physDir = new Vector3f();
        physWalk = new Vector3f();

        currRot = new Quaternion();
        inMelee = false;
        meleeStats = new SoldierMeleeStats();

        if (ref.rangeStats != null) {
            rangeStats = new SoldierRangeStats();
        }


   }

    public void createData() {

        terrain = unit.battle.terrain.terrainQuad;

        GenericUnit gen = Main.DB.genUnits.get(unit.refName);

        model = unit.battle.game.getAssetManager().loadModel("models/" + gen.model );
        mat = new Material(unit.battle.game.getAssetManager(), "Common/MatDefs/Light/Lighting.j3md");
        
        mat.setTexture("DiffuseMap", unit.battle.game.getAssetManager().loadTexture("textures/" + gen.diffuse));
        

        //model.setMaterial(mat);
        model.setShadowMode(ShadowMode.CastAndReceive);
        
        Quaternion ql = new Quaternion().fromAngleAxis(FastMath.HALF_PI, Vector3f.UNIT_Y);
        //Quaternion qu = new Quaternion().fromAngleAxis(FastMath.PI, Vector3f.UNIT_Z);
        model.setLocalRotation(ql);
        model.setLocalTranslation(0, -0.05f, 0);


        cone = new Node("");
        Spatial dome = (Spatial) new Geometry("", new Dome(Vector3f.ZERO, 2, 12, 0.2f, false));
        dome.setLocalScale(1, 5, 1);

        cone.attachChild(dome);
        cone.attachChild((Spatial) new Geometry("", new Sphere(12, 12, 0.2f)));
        cone.setMaterial(new Material(unit.battle.game.getAssetManager(), "Common/MatDefs/Light/Lighting.j3md"));
        Quaternion q = new Quaternion().fromAngleAxis(FastMath.HALF_PI, Vector3f.UNIT_Z);
        cone.setLocalRotation(q);
        cone.setLocalTranslation(0, 2, 0);

        selectionQuad = (Spatial) new Geometry("", new Quad(radius * 2.5f, radius * 2.5f));
        Material m = new Material(unit.battle.game.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
        m.setTexture("ColorMap", unit.battle.game.getAssetManager().loadTexture("textures/selection.png"));
        m.getAdditionalRenderState().setBlendMode(BlendMode.Alpha);
        selectionQuad.setQueueBucket(Bucket.Transparent);
        m.getAdditionalRenderState().setDepthWrite(false);
        selectionQuad.setMaterial(m);
        selectionQuad.setLocalRotation(new Quaternion().fromAngleAxis(-FastMath.HALF_PI, Vector3f.UNIT_X));
        selectionQuad.setLocalTranslation(-(radius + 0.1f), 0, (radius + 0.1f));


        previewQuad = (Spatial) new Geometry("", new Quad(radius * 2.5f, radius * 2.5f));
        previewQuad.setQueueBucket(Bucket.Transparent);
        previewQuad.setMaterial(m);
        previewQuad.setLocalRotation(new Quaternion().fromAngleAxis(-FastMath.HALF_PI, Vector3f.UNIT_X));


        node = new Node("soldier");
        node.attachChild(model);
        //node.attachChild(cone);
        
           
        AmbientLight al = new AmbientLight();
        al.setColor(ColorRGBA.White.mult(5f));
        
         
        node.addLight(al);


        collShape = new CapsuleCollisionShape(radius, height);
        

        
        meleeShape = new SphereCollisionShape(unit.meleeStats.distance + 10);
        if (rangeStats != null) {
            rangeShape = new SphereCollisionShape(unit.rangeStats.distance);
        } else {
            rangeShape = new SphereCollisionShape(100);
        }


        collControl = new RigidBodyControl(collShape,1);        
        meleeControl = new GhostControl(meleeShape);
        rangeControl = new GhostControl(rangeShape);

        
        // Group 1: static objects (trees, buildings etc.)
        // Group 2: rigid soldiers A
        // Group 3: team A ghost controls
        // Group 4: rigid soldiers B
        // Group 5: team B ghost controls 
        // Group 6: projectiles
              

        if ("A".equals(unit.owner)) {
            
            collControl.setCollisionGroup(PhysicsCollisionObject.COLLISION_GROUP_02);

            collControl.setCollideWithGroups(
                    PhysicsCollisionObject.COLLISION_GROUP_02 |
                    PhysicsCollisionObject.COLLISION_GROUP_04 |
                    PhysicsCollisionObject.COLLISION_GROUP_05);
            
            meleeControl.setCollisionGroup(PhysicsCollisionObject.COLLISION_GROUP_03);
            meleeControl.setCollideWithGroups(PhysicsCollisionObject.COLLISION_GROUP_04);
                        
            rangeControl.setCollisionGroup(PhysicsCollisionObject.COLLISION_GROUP_03);
            rangeControl.setCollideWithGroups(PhysicsCollisionObject.COLLISION_GROUP_04);
                 
        } 
        else {
            
             collControl.setCollisionGroup(PhysicsCollisionObject.COLLISION_GROUP_04);

             collControl.setCollideWithGroups(
                    PhysicsCollisionObject.COLLISION_GROUP_02 |
                    PhysicsCollisionObject.COLLISION_GROUP_03 |
                    PhysicsCollisionObject.COLLISION_GROUP_04);
             
            meleeControl.setCollisionGroup(PhysicsCollisionObject.COLLISION_GROUP_05);
            meleeControl.setCollideWithGroups(PhysicsCollisionObject.COLLISION_GROUP_02);
                        
            rangeControl.setCollisionGroup(PhysicsCollisionObject.COLLISION_GROUP_05);
            rangeControl.setCollideWithGroups(PhysicsCollisionObject.COLLISION_GROUP_02);
        }


        unit.battle.game.bulletState.getPhysicsSpace().add(collControl);
        unit.battle.game.bulletState.getPhysicsSpace().add(meleeControl);
        node.addControl(collControl);
        node.addControl(meleeControl);
        
        collControl.setGravity(Vector3f.ZERO);


        if (unit.rangeStats != null) {
            unit.battle.game.bulletState.getPhysicsSpace().add(rangeControl);
            node.addControl(rangeControl);
        }

        unit.battle.hashedSoldiers.put(node, this);

 
	animControl = model.getControl(AnimControl.class);
	animControl.addListener(this);
	animChannel = animControl.createChannel();
	animChannel.setAnim(idleAnim);

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
        currRot.fromAngleAxis(FastMath.atan2(dz, dx),Vector3f.UNIT_Y);
        
        physPos.x = currPos.x;
        physPos.y = terrain.getHeight(currPos);
        physPos.z = currPos.y;
        collControl.setPhysicsLocation(physPos);
        
        physDir.x = currDir.x;
        physDir.y = 0;
        physDir.z = currDir.y;
        
        physWalk.x = physWalk.y = physWalk.z = 0f;
      
    }

    public void setGoal(float x, float z, float dx, float dz, boolean run) {
        
        goalPos.x = x;
        goalPos.y = z;
        goalDir.x = -dx;
        goalDir.y = dz;       
        goalDir.normalizeLocal();
    }

    public void setGoal(Vector2f pos, Vector2f dir, boolean run) {
        setGoal(pos.x, pos.y, dir.x, dir.y, run);
    }

    public float turnTo(float dx, float dy, float turnSpeed) {

        //NOTE: Both vectors must be normalized!!!
        float dot = currDir.x * -dx + currDir.y * dy;
        if (dot < 0.98f)
        {
        float currAngle = FastMath.atan2(currDir.y, currDir.x);
        currAngle += (currDir.x * dy - currDir.y * -dx)>0? turnSpeed : -turnSpeed;
        currDir.x = FastMath.cos(currAngle);
        currDir.y = FastMath.sin(currAngle);
        currRot.fromAngleAxis(currAngle, Vector3f.UNIT_Y);
              
        physDir.x = currDir.x;
        physDir.y = 0;
        physDir.z = currDir.y;
        collControl.setPhysicsRotation(currRot);
        }
        return dot;

    }

    public float turnToSoldier(Soldier s, float turnSpeed) {

        float dx = s.currPos.x - currPos.x;
        float dy = s.currPos.y - currPos.y;
        float len = 1.0f / FastMath.sqrt(dx * dx + dy * dy);
        return turnTo(dx*len, dy*len, turnSpeed);
    }

    public void takeMeleeDamage(Soldier from) {

        if (enemy == null)enemy = from;
        else if( enemy.currPos.distanceSquared(currPos)
                > from.currPos.distanceSquared(currPos)) enemy=from;
        

        float damage = from.unit.meleeStats.damage;
        float total = damage / unit.meleeStats.armor;

        System.err.println(this + " receives " + total + " from " + from);

        hp -= total;
        
        takehit=true;   //start anim

        if (hp <= 0) {
            from.enemy = null;
            die();
        }

    }

    public void die() {
        status = Status.Dead;
        System.err.println(this + " died ");
        
        unit.battle.sceneNode.getControl(UpdateControl.class).enqueue(
                new Callable() {

                    @Override
                    public Object call() throws Exception {
                        //model.setMaterial(new Material(unit.battle.game.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md"));
                        //node.detachChild(selectionQuad);
                        //node.detachChild(cone);
                        node.detachAllChildren();
                        unit.battle.game.bulletState.getPhysicsSpace().remove(collControl);
                        unit.battle.game.bulletState.getPhysicsSpace().remove(meleeControl);
                        if (rangeStats != null) {
                            unit.battle.game.bulletState.getPhysicsSpace().remove(rangeControl);
                        }

                        unit.battle.hashedSoldiers.remove(model);
                        return null;
                    }
                });


    }

   

    public void update(float tpf) {

        System.out.println("status: "+status);

        if(status == Status.Dead) return;
        if (unit.previewFormation) {
            previewQuad.setLocalTranslation(previewPos.x - (radius + 0.1f),
                    terrain.getHeight(previewPos) + 0.1f, previewPos.y + (radius + 0.1f));

        }
        
        physPos = collControl.getPhysicsLocation();
        currPos.x = physPos.x;
        currPos.y = physPos.z;
        physPos.y = terrain.getHeight(currPos);
        collControl.setPhysicsLocation(physPos);
        collControl.setPhysicsRotation(currRot);
        
        float enemy_dist=0;
        float goal_dist=0;
        // Update melee and enemy state
        if(enemy != null)
        {
            if (enemy.status == Status.Dead) enemy = unit.findNextTarget(this);                      
            else
            {
                enemy_dist = enemy.currPos.subtract(currPos).lengthSquared();
                if(enemy_dist <= unit.meleeStats.sqDist)
                {
                    enemy_dist = FastMath.sqrt(enemy_dist);
                    inMelee = true;
                    if (unit.enemy == null) 
                    unit.attackUnit(enemy.unit,unit.run);                
                }
                else inMelee = false;
            }          
        }       
        if(enemy == null)
        {
            inMelee = false;
        }
        
                
        

        // If unit is to attack somebody
        if(unit.enemy != null && enemy != null)
        {          
            turnToSoldier(enemy, 0.03f);

            if (!inMelee)
            {

                walkDir = enemy.currPos.subtract(currPos);        
                enemy_dist = walkDir.length();
                walkDir.divideLocal(enemy_dist + 0.01f);              
                turnToSoldier(enemy, 0.03f);
                currSpeed += 0.01f;
            }
            
            else
            {
                // Find correct melee distance to enemy
                float dist = enemy_dist - unit.meleeStats.distance;
                currSpeed = dist;
                
            }
            
        }
        
        // No attack order given
        else
        {
            
            walkDir = goalPos.subtract(currPos);
            goal_dist = walkDir.length();
            walkDir.divideLocal(goal_dist + 0.01f);

            // If no enemy is near
            if(enemy == null)
            {                          
                

                if(goal_dist < 0.25f)
                {
                   turnTo(goalDir.x, goalDir.y, 0.03f);
                   currSpeed = goal_dist;
                }
                
                else if  (turnTo(walkDir.x, walkDir.y, 0.03f) > 0.9)
                {
                    
                    currSpeed += 0.01f;
                }
                else
                    currSpeed *= 0.1f;

               
            }
            else if(currSpeed < 0.1)
            {
                  turnToSoldier(enemy, 0.03f);
            }
        }
             

        switch (status) {

            case Idle:

                if (enemy != null) {

                    if (enemy_dist < unit.meleeStats.distance + 10 && currSpeed < 1) {
                        fightTimer += tpf;
                        if (fightTimer > unit.meleeStats.timeToStance) {
                            status = Status.Stance;
                            fightTimer = 0;
                        }
                    }
                                   
                } else fightTimer = 0;
                
                break;


            case Stance:

                if (enemy == null) {
                    if (fightTimer > unit.meleeStats.timeToStance) {
                        status = Status.Idle;
                        fightTimer = 0;
                    }
                    break;
                }

               
                    if (enemy_dist <= unit.meleeStats.distance) {
                        fightTimer += tpf;
                        if (fightTimer > unit.meleeStats.timeToHit) {
                            enemy.takeMeleeDamage(this);
                            fightTimer = 0;
                            attack=true;    //play attack-animation
                        } 
                    }
                    else fightTimer = 0;

                break;


        }

        currSpeed = unit.battle.ensureMinMax(currSpeed, -0.5f, unit.run ? 2f : 1f);
        physWalk.x = walkDir.x*currSpeed;
        physWalk.z = walkDir.y*currSpeed;
        collControl.setLinearVelocity(physWalk);
        
 	handleAnimations();
       

    }
     private void handleAnimations()
    {
        //System.out.println("attack: "+attack);
	if(attacking)
	{
	    //waiting for attack animation to finish
	}
	else if(attack)
	{
 
	    animChannel.setAnim(attackAnim,.3f);
	    animChannel.setLoopMode(LoopMode.DontLoop);
	    attack = false;
	    attacking = true;
	}
	/*else if(characterControl.onGround())
	{*/
        else if(takehit){
 		if(!animChannel.getAnimationName().equals(deadAnim))
		{
		    animChannel.setAnim(deadAnim,.3f);
		    animChannel.setLoopMode(LoopMode.Cycle);
                    takehit=false;
		}
           
        }
        else    if(currSpeed>0.1f)
	    {
		if(!animChannel.getAnimationName().equals(walkAnim))
		{
		    animChannel.setAnim(walkAnim,.3f);
		    animChannel.setLoopMode(LoopMode.Loop);
		}
	    }
	    else
	    {
		if(!animChannel.getAnimationName().equals(idleAnim))
		{
		    animChannel.setAnim(idleAnim,.3f);
		    animChannel.setLoopMode(LoopMode.Cycle);
		}
	    }
	//}
    }
 
   
    public void onAnimCycleDone(AnimControl control, AnimChannel channel, String animName)
    {
	if(channel == animChannel && attacking && animName.equals(attackAnim))
	{
	    attacking = false;
	}
	if(channel == animChannel && !attacking && animName.equals(idleAnim))
	{
            Random rand = new Random();
            int r=rand.nextInt(4);
            if(r>3)idleAnim = "Idle3";
                else if(r>2)idleAnim = "Idle2";
                    else if(r>1)idleAnim = "Idle1";
        }
    }
 
    public void onAnimChange(AnimControl control, AnimChannel channel, String animName)
    {
    }
  
}

