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
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.BillboardControl;
import com.jme3.scene.control.UpdateControl;
import com.jme3.scene.shape.Dome;
import com.jme3.scene.shape.Quad;
import com.jme3.scene.shape.Sphere;
import java.util.ArrayList;
import java.util.concurrent.Callable;
import openwar.Main;
import openwar.battle.formations.BoxFormation;
import openwar.battle.formations.CircleFormation;
import openwar.battle.formations.Formation;

/**
 *
 * @author kehl
 */
public class Unit {

    public enum Status {

        Idle,
        Moving,
        Fighting,
        Routing
    };
    public String refName;
    public String owner;
    public UnitMeleeStats meleeStats;
    public UnitRangeStats rangeStats;
    public boolean selected, run, invertFormation, previewFormation;
    public float morale = 100f, stamina = 100f;
    public ArrayList<Soldier> soldiers;
    public BattleAppState battle;
    public Vector2f currPos, goalPos, goalDir, currDir, oldGoalDir;
    public Status status;
    public Formation formation;
    public Unit enemy;
    public Node cone;
    public Spatial banner;
    public float banner_offset=0;

    public Unit(BattleAppState b, openwar.DB.Unit U, String player) {

        battle = b;
        status = Unit.Status.Idle;
        owner = player;
        refName = U.refName;


        currPos = new Vector2f(0, 0);
        goalPos = new Vector2f(0, 0);

        currDir = new Vector2f(0, 1);
        goalDir = new Vector2f(0, 1);
        oldGoalDir = new Vector2f(0, 1);



        if (Main.DB.genUnits.get(U.refName).melee_stats != null) {
            meleeStats = new UnitMeleeStats(Main.DB.genUnits.get(U.refName).melee_stats);
        }


        if (Main.DB.genUnits.get(U.refName).range_stats != null) {
            rangeStats = new UnitRangeStats(Main.DB.genUnits.get(U.refName).range_stats);
        }



        soldiers = new ArrayList<Soldier>();
        for (int i = 0; i < U.count; i++) {
            soldiers.add(new Soldier(this));
        }



        formation = new BoxFormation(this);

    }

    public void createData() {

        cone = new Node("");
        Spatial dome = (Spatial) new Geometry("", new Dome(Vector3f.ZERO, 2, 12, 1, false));
        dome.setLocalScale(1, 5, 1);

        cone.attachChild(dome);
        cone.attachChild((Spatial) new Geometry("", new Sphere(12, 12, 1)));
        cone.setMaterial(new Material(battle.game.getAssetManager(), "Common/MatDefs/Light/Lighting.j3md"));
        Quaternion q = new Quaternion().fromAngleAxis(FastMath.HALF_PI, Vector3f.UNIT_Z);
        Quaternion l = new Quaternion().fromAngleAxis(FastMath.atan2(currDir.y, currDir.x), Vector3f.UNIT_Y);
        cone.setLocalRotation(l.multLocal(q));
        for (Soldier s : soldiers) {
            s.createData();
            battle.sceneNode.attachChild(s.node);
        }
        
        banner = (Spatial) new Geometry("", new Quad(2f, 4f));
        Material mat = new Material(battle.game.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
        mat.getAdditionalRenderState().setBlendMode(BlendMode.Alpha);
        mat.setTexture("ColorMap", Main.DB.genFactions.get(owner).banner);
        banner.setQueueBucket(Bucket.Transparent);
        banner.setMaterial(mat);
        banner.addControl(new BillboardControl());
        battle.sceneNode.attachChild(banner);

    }

    public Soldier findNextTarget(Soldier seeker) {
        Soldier s = null;

        if (enemy != null) {
            float min = 100000;
            for (Soldier t : enemy.soldiers) {
                float l = t.currPos.distanceSquared(currPos);
                if (l < min) {
                    min = l;
                    s = t;
                }
            }
        }
        return s;

    }

    public void update(float tpf) {


        currPos.x = currPos.y = currDir.x = currDir.y = 0;

        boolean allIdle = true;
        boolean moving = false;
        boolean fighting = false;
        for (final Soldier s : soldiers) {
            s.update(tpf);
            allIdle &= (s.status == Soldier.Status.Idle);
            // moving |= (s.status == Soldier.Status.Move);
            //fighting |= (s.status == Soldier.Status.Fighting);

            currPos.addLocal(s.currPos);
            currDir.addLocal(s.currDir);

            // Remove soldier from list if dead
            // TODO: and update battle statistics (if we have one :D )
            if (s.status == Soldier.Status.Dead) {
                battle.sceneNode.getControl(UpdateControl.class).enqueue(
                        new Callable() {

                            @Override
                            public Object call() throws Exception {
                                soldiers.remove(s);
                                return null;
                            }
                        });
            }
        }


        currPos.divideLocal(soldiers.size());
        currDir.normalizeLocal();




        switch (status) {

            case Idle:
                stamina += tpf * 0.01f;
                break;

            case Moving:
                stamina -= run ? tpf * 0.5f : tpf * 0.1f;               
                if(enemy!= null) setGoalUnit(enemy);
                break;


        }


        if (fighting && status != Status.Fighting) {
            status = Status.Fighting;
            System.out.println("Unit is fighting");
        }

        if (allIdle && status != Status.Idle) {
            status = Unit.Status.Idle;
            System.out.println("Unit is idle");
        }

        stamina = battle.ensureMinMax(0f, 100f, stamina);

        
        banner.setLocalTranslation(currPos.x, battle.terrain.terrainQuad.getHeight(currPos) + 3, currPos.y);


        if (!selected) {
            return;
        }

        banner_offset += 2*tpf;
        float val = 0.5f*FastMath.sin(banner_offset);
        banner.setLocalTranslation(currPos.x, 
                battle.terrain.terrainQuad.getHeight(currPos) + 3 +val , currPos.y);

        cone.setLocalTranslation(currPos.x, battle.terrain.terrainQuad.getHeight(currPos) + 5, currPos.y);
        Quaternion q = new Quaternion().fromAngleAxis(FastMath.HALF_PI, Vector3f.UNIT_Z);
        Quaternion l = new Quaternion().fromAngleAxis(FastMath.atan2(goalDir.y, goalDir.x), Vector3f.UNIT_Y);
        cone.setLocalRotation(l.multLocal(q));


    }
    
    
    
    public void attackUnit(Unit u, boolean run) {

        System.err.println(this + " attacking " + u);
        setGoalUnit(u);
        enemy = u;
        
        float ratio = u.soldiers.size()/(float)soldiers.size();
        float curr=0;
        for (Soldier s : soldiers)
        {
            s.enemy = enemy.soldiers.get((int)curr);
            curr += ratio;
        }
    }
    
    

    public void setPosition(float x, float z, float dx, float dz) {
        currPos.x = x;
        currPos.y = z;
        currDir.x = -dx;
        currDir.y = dz;
        currDir.normalizeLocal();
        goalPos = currPos.clone();
        goalDir = currDir.clone();
        formation.doFormation(false, true, invertFormation);

    }

    public void setFormation(Formation form, boolean warp) {
        formation = form;
        formation.doFormation(run, warp, invertFormation);
    }
    
    public void acceptFormationPreview(Vector3f start, Vector3f end,boolean run)
    {
        
        float dx = start.x - end.x;
        float dy = start.z - end.z;
        goalDir = new Vector2f(dy,dx);
        goalDir.normalizeLocal();
        goalPos = new Vector2f(start.x+end.x,start.z + end.z).multLocal(0.5f);
        togglePreviewFormation(false);
        formation.doExactFormation(run, false,true);

    }

    public void setGoal(float x, float z, float dx, float dz, boolean run) {
        oldGoalDir = goalDir.clone();
        goalDir.x = -dx;
        goalDir.y = dz;
        goalDir.normalizeLocal();
        setGoal(x, z, run);
    }


    public void setGoal(Vector2f pos, boolean run) {
        setGoal(pos.x, pos.y, run);
    }

    public void setGoalUnit(Unit u) {
        float dirX = u.currPos.x - currPos.x;
        float dirY = u.currPos.y - currPos.y;
        setGoal(u.currPos.x, u.currPos.y, dirX, dirY, run);
    }

    public void setGoal(float x, float z, boolean run) {
        goalPos.x = x;
        goalPos.y = z;

        float angle = goalDir.smallestAngleBetween(oldGoalDir);
        invertFormation ^=  angle > FastMath.HALF_PI;
        System.err.println(angle*FastMath.RAD_TO_DEG + " " + invertFormation);

        formation.doExactFormation(run, false,false);
        this.run = run;
    }

    public void toggleSelection(boolean select) {
        if (selected == select) {
            return;
        }

        selected = select;


        if (selected) {
            battle.sceneNode.attachChild(cone);

        } else {
            battle.sceneNode.detachChild(cone);
            banner_offset=0;

        }

        for (Soldier s : soldiers) {
            s.select(select);


        }
    }

    public void togglePreviewFormation(boolean select) {
        previewFormation = select;

        if (previewFormation) {
            for (Soldier s : soldiers) {
                battle.sceneNode.attachChild(s.previewQuad);
            }
        } else {
            for (Soldier s : soldiers) {
                battle.sceneNode.detachChild(s.previewQuad);
            }
        }

    }

    public void previewFormation(Vector3f start, Vector3f end) {
        formation.previewFormation(start.x, start.z, end.x, end.z);
    }
}
