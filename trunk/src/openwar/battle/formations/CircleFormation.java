/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package openwar.battle.formations;

import com.jme3.math.FastMath;
import openwar.battle.Soldier;
import openwar.battle.Unit;

/**
 *
 * @author kehl
 */
public class CircleFormation extends Formation {

    public CircleFormation(Unit unit) {
        u = unit;
    }

    @Override
    public void doFormation(boolean run, boolean warp) {

        float number = u.soldiers.size();
        float dist = sparseFormation ? 3f : 1.5f;

        float r = (number * dist) / FastMath.TWO_PI;
        float rad = 0;

        for (int i = 0; i < number; i++) {

            Soldier s = u.soldiers.get(i);


            float cos = FastMath.cos(rad);
            float sin = FastMath.sin(rad);


            float x = u.goalPos.x - cos * r;
            float z = u.goalPos.y + sin * r;

            rad += dist / r;


            if (warp) {
                s.setPosition(x, z, cos, sin);
            } else {
                s.setGoal(x, z, cos, sin, run);
            }
        }
    }
}
