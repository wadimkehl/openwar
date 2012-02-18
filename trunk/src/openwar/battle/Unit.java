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
    public Vector2f pos, goal;
    public Status status;

    public Unit(BattleAppState b, openwar.DB.Unit U, String player) {

        battle = b;
        status = Unit.Status.Idle;
        owner = player;
        exp = U.exp;
        att = U.att;
        def = U.def;

        pos = new Vector2f();
        goal = new Vector2f();

        soldiers = new ArrayList<Soldier>();
        for (int i = 0; i < U.count; i++) {
            soldiers.add(new Soldier(this));
        }

    }

    public void createData() {

        float x = pos.x, z = pos.y;

        for (Soldier s : soldiers) {
            s.createData();
            s.setPosition(x, z);
            battle.sceneNode.attachChild(s.node);

            x += 2f;
        }
    }

    public void update(float tpf) {



        boolean allIdle = true;
        for (final Soldier s : soldiers) {
            s.update(tpf);
            allIdle &= (s.status == Soldier.Status.Idle);


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

    public void setGoal(float x, float z, boolean run) {
        goal.x = x;
        goal.y = z;

        if (run) {
            status = Status.Run;
            System.out.println("Unit is running");

        } else {
            status = Status.Walk;
            System.out.println("Unit is walking");

        }

        // TODO: Soldiers need to get their goals according to formation position
        for (Soldier s : soldiers) {
            s.setGoal(x, z, run);
        }
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
