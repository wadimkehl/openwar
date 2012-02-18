/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package openwar.battle;

import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.scene.control.UpdateControl;
import java.util.ArrayList;
import java.util.concurrent.Callable;
import openwar.battle.formations.Formation;
import openwar.battle.formations.LineFormation;

/**
 *
 * @author kehl
 */
public class Unit {

    public enum Status {

        Idle,
        Walk,
        Run,
        Attack,
        Fight,
        Retreat,
        Rout
    };
    public String refName;
    public String owner;
    public int exp, att, def;
    public boolean selected;
    public float morale = 100f, stamina = 100f;
    public ArrayList<Soldier> soldiers;
    public BattleAppState battle;
    public Vector2f currPos, goalPos, goalDir, currDir;
    public Status status;
    public Formation formation;

    public Unit(BattleAppState b, openwar.DB.Unit U, String player) {

        battle = b;
        status = Unit.Status.Idle;
        owner = player;
        exp = U.exp;
        att = U.att;
        def = U.def;

        currPos = new Vector2f(0, 0);
        goalPos = new Vector2f(0, 0);

        currDir = new Vector2f(0, 1);
        goalDir = new Vector2f(0, 1);

        soldiers = new ArrayList<Soldier>();
        for (int i = 0; i < U.count; i++) {
            soldiers.add(new Soldier(this));
        }

        formation = new LineFormation(this);

    }

    public void createData() {


        for (Soldier s : soldiers) {
            s.createData();
            battle.sceneNode.attachChild(s.node);
        }

    }

    public void update(float tpf) {


        currPos.x=currPos.y=currDir.x=currDir.y=0;

        boolean allIdle = true;
        for (final Soldier s : soldiers) {
            s.update(tpf);
            allIdle &= (s.status == Soldier.Status.Idle);
            
            currPos.addLocal(s.currPos);
            currDir.addLocal(s.goalDir);

            // Remove soldier from list if dead
            // TODO: and update battle statistics (if we have one :D )
            if (s.status == Soldier.Status.Dead) {
                battle.sceneNode.getControl(UpdateControl.class).enqueue(
                        new Callable() {

                            @Override
                            public Object call() throws Exception {
                                s.node.detachChild(s.selectionQuad);
                                soldiers.remove(s);
                                return null;
                            }
                        });
            }
        }

        
        currPos.divideLocal(soldiers.size());
        currDir.divideLocal(soldiers.size());


        switch (status) {

            case Idle:
                stamina += tpf * 0.01f;
                break;

            case Walk:
                stamina -= tpf * 0.1f;
                if (allIdle) {
                    status = Unit.Status.Idle;
                    System.out.println("Unit is idle");
                }
                break;

            case Run:
                stamina -= tpf * 0.5f;
                if (allIdle) {
                    status = Unit.Status.Idle;
                    System.out.println("Unit is idle");

                }
                break;


        }

        stamina = battle.ensureMinMax(0f, 100f, stamina);


    }

    public void setPosition(float x, float z, float dx, float dz) {
        currPos.x = x;
        currPos.y = z;
        currDir.x = dx;
        currDir.y = dz;
        goalPos = currPos.clone();
        goalDir = currDir.clone();
        formation.doFormation(false, true);

    }
    

    public void setGoal(float x, float z, float dx, float dz, boolean run) {
        goalDir.x = dx;
        goalDir.y = dz;
        setGoal(x, z, run);

    }

    public void setGoal(float x, float z, boolean run) {
        goalPos.x = x;
        goalPos.y = z;

        if (run) {
            status = Status.Run;
            System.out.println("Unit is running");

        } else {
            status = Status.Walk;
            System.out.println("Unit is walking");

        }

        formation.doFormation(run, false);
    }

    public void toggleSelection(boolean select) {
        if (selected == select) {
            return;
        }

        selected = select;
        for (Soldier s : soldiers) {
            s.select(select);


        }
    }
}
