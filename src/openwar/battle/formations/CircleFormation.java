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
public class CircleFormation extends Formation {

    public CircleFormation(Unit unit) {
        u = unit;
    }

    @Override
    public void doFormation(boolean run, boolean warp, boolean invert) {

        float number = u.soldiers.size();
        float dist = sparseFormation ? 3f : 1.5f;
        float r = (number * dist) / FastMath.TWO_PI;
        float rad = FastMath.HALF_PI;

        for (int i = 0; i < number; i++) {

            Soldier s = u.soldiers.get(i);


            float cos = FastMath.cos(rad);
            float sin = FastMath.sin(rad);


            float x = u.goalPos.x - cos * r;
            float z = u.goalPos.y + sin * r;

//            if (invert) {
//                rad -= dist / r;
//            } else {
            rad += dist / r;
//            }


            if (warp) {
                s.setPosition(x, z, cos, sin);
            } else {
                s.setGoal(x, z, cos, sin, run);
            }
        }
    }

    @Override
    public float getWidth() {

        float number = u.soldiers.size();
        float dist = sparseFormation ? 3f : 1.5f;
        return (number * dist) / FastMath.PI;
    }

    @Override
    public float getDepth() {
        return getWidth();
    }

    @Override
    public void previewFormation(float lx, float ly, float rx, float ry, boolean accept) {
        float dist = sparseFormation ? 3f : 1.5f;
        float number = u.soldiers.size();
        Vector2f diff = new Vector2f(rx - lx, ry - ly);
        float r = Math.max((number * dist) / FastMath.TWO_PI, diff.length() * 0.5f);
        diff.normalizeLocal();
        float centerx = lx + (rx - lx) / 2f;
        float centery = ly + (ry - ly) / 2f;

        float rad = FastMath.HALF_PI;
        float increment = FastMath.TWO_PI / number;


        if (accept) {
            u.goalPos.x = centerx;
            u.goalPos.y = centery;
            u.goalDir.x = -diff.y;
            u.goalDir.y = -diff.x;
        }


        for (Soldier s : u.soldiers) {

            float cos = FastMath.cos(rad);
            float sin = FastMath.sin(rad);
            s.previewPos.x = centerx - cos * r;
            s.previewPos.y = centery + sin * r;

            // playing with increment can lead to arcs and barricades
            //rad += dist / r;
            rad += increment;

            if (accept) {
                s.setGoal(s.previewPos.x, s.previewPos.y, cos, sin, u.run);

            }
        }




    }
}
