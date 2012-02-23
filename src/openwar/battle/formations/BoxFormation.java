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
public class BoxFormation extends Formation {

    float nrPerRow;

    public BoxFormation(Unit unit, int soldierPerRow) {
        u = unit;
        nrPerRow = soldierPerRow;
    }

    @Override
    public void doFormation(boolean run, boolean warp, boolean invert) {


        float number = u.soldiers.size();
        float dist = sparseFormation ? 3f : 1.5f;
        float angle = FastMath.atan2(u.goalDir.y, u.goalDir.x);
        if (angle < 0) {
            angle += FastMath.TWO_PI;
        }
        angle += FastMath.HALF_PI;
        float cos = FastMath.cos(angle);
        float sin = FastMath.sin(angle);

        nrPerRow = number < nrPerRow ? number : nrPerRow;

        float nrCol = FastMath.ceil((number) / (nrPerRow));
        float currRow = 0;
        float currCol = 0;
        
        if (invert) {
            for (int i = 0; i < u.soldiers.size(); i++) {

                Soldier s = u.soldiers.get(i);
                float x = u.goalPos.x - (currRow - nrPerRow * 0.5f) * dist * cos;
                float z = u.goalPos.y + (currRow - nrPerRow * 0.5f) * dist * sin;

                x += (currCol) * dist * sin;
                z += (currCol) * dist * cos;

                currRow++;
                if (currRow == nrPerRow) {
                    currRow = 0;
                    currCol++;
                }

                if (warp) {
                    s.setPosition(x, z, u.goalDir.x, u.goalDir.y);
                } else {
                    s.setGoal(x, z, u.goalDir.x, u.goalDir.y, run);
                }

            }
        }
        else
        {
            for (int i = u.soldiers.size()-1; i >= 0; i--) {

                Soldier s = u.soldiers.get(i);
                float x = u.goalPos.x - (currRow - nrPerRow * 0.5f) * dist * cos;
                float z = u.goalPos.y + (currRow - nrPerRow * 0.5f) * dist * sin;

                x += (currCol) * dist * sin;
                z += (currCol) * dist * cos;

                currRow++;
                if (currRow == nrPerRow) {
                    currRow = 0;
                    currCol++;
                }

                if (warp) {
                    s.setPosition(x, z, u.goalDir.x, u.goalDir.y);
                } else {
                    s.setGoal(x, z, u.goalDir.x, u.goalDir.y, run);
                }

            }
        }
    }
}
