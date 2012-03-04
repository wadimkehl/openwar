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
public class BoxFormation extends Formation {

    float nrPerRow;

    public BoxFormation(Unit unit, int soldierPerRow) {
        u = unit;
        nrPerRow = soldierPerRow;
    }

    public BoxFormation(Unit unit) {
        this(unit, 15);
    }

    @Override
    public float getWidth() {
        float dist = sparseFormation ? 3f : 1.5f;
        return nrPerRow * dist; // nrPerRow - 1 ?
    }

    @Override
    public float getDepth() {
        float number = u.soldiers.size();
        float dist = sparseFormation ? 3f : 1.5f;
        return (FastMath.ceil((number) / (nrPerRow)) - 1) * dist;

    }

    @Override
    public void doFormation(boolean run, boolean warp, boolean invert) {

        float currNr = nrPerRow;
        float currRow = 0;
        float currCol = 0;
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

        int start = invert ? u.soldiers.size() - 1 : 0;
        int end = invert ? -1 : u.soldiers.size();
        int increment = invert ? -1 : 1;
        for (int i = start; i != end; i += increment) {

            Soldier s = u.soldiers.get(i);
            float x = u.goalPos.x - (currRow - currNr * 0.5f) * dist * cos;
            float y = u.goalPos.y + (currRow - currNr * 0.5f) * dist * sin;
            x += (currCol) * dist * sin;
            y += (currCol) * dist * cos;

            if (warp) {
                s.setPosition(x, y, u.goalDir.x, u.goalDir.y);
            } else {
                s.setGoal(x, y, u.goalDir.x, u.goalDir.y, run);
            }

            currRow++;
            number--;
            if (currRow >= nrPerRow) {
                currRow = 0;
                currCol++;
                if (number < nrPerRow) {
                    currNr = number;
                }

            }


        }

    }

    @Override
    public void previewFormation(float lx, float ly, float rx, float ry, boolean accept) {

        Vector2f diff = new Vector2f(rx - lx, ry - ly);
        float width = diff.length();
        diff.normalizeLocal();
        float angle = FastMath.atan2(diff.y, diff.x);
        float cos = FastMath.cos(angle);
        float sin = FastMath.sin(angle);
        float dist = sparseFormation ? 3f : 1.5f;
        float centerx = lx + (rx - lx) / 2f;
        float centery = ly + (ry - ly) / 2f;

        nrPerRow = width / dist;
        float currNr = nrPerRow;
        float currRow = 0;
        float currCol = 0;
        float number = u.soldiers.size();


        if (accept) {
            u.goalPos.x = centerx;
            u.goalPos.y = centery;
            u.goalDir.x = -diff.y;
            u.goalDir.y = -diff.x;
        }

        for (int i = 0; i < u.soldiers.size(); i++) {

            Soldier s = u.soldiers.get(i);
            float x = centerx + (currRow - currNr * 0.5f) * dist * cos;
            float y = centery + (currRow - currNr * 0.5f) * dist * sin;
            x -= (currCol) * dist * sin;
            y += (currCol) * dist * cos;

            s.previewPos.x = x;
            s.previewPos.y = y;

            currRow++;
            number--;
            if (currRow >= nrPerRow) {
                currRow = 0;
                currCol++;
                if (number < nrPerRow) {
                    currNr = number;
                }
            }

            if (accept) {
                s.setGoal(s.previewPos.x, s.previewPos.y, u.goalDir.x, u.goalDir.y, u.run);

            }
        }

    }
}
