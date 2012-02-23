/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package openwar.battle.formations;

import com.jme3.math.FastMath;
import com.jme3.math.Vector2f;
import openwar.battle.Soldier;
import openwar.battle.Unit;

/**
 *
 * @author kehl
 */
public class LineFormation extends Formation {

    public LineFormation(Unit unit) {
        u = unit;
    }

    @Override
    public void doFormation(boolean run, boolean warp,boolean invert) {

        float number = u.soldiers.size();
        float dist = sparseFormation ? 3f : 1.5f;
        float angle = FastMath.atan2(u.goalDir.y, u.goalDir.x);
        if (angle<0) angle+=FastMath.TWO_PI;
        angle+=FastMath.HALF_PI;
        float cos = FastMath.cos(angle);
        float sin = FastMath.sin(angle);

        for (int i = 0; i < number; i++) {

            Soldier s = u.soldiers.get(i);
            
            float x = u.goalPos.x - (i - number*0.5f) * dist*cos;
            float z = u.goalPos.y + (i - number*0.5f) * dist*sin;

            if (warp) {
                s.setPosition(x, z, u.goalDir.x, u.goalDir.y);
            } else {
                s.setGoal(x,z, u.goalDir.x,u.goalDir.y, run);
            }
        }
    }
}
