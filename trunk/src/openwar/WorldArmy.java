/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package openwar;

import com.jme3.bounding.BoundingBox;
import com.jme3.bullet.collision.shapes.CapsuleCollisionShape;
import com.jme3.bullet.control.CharacterControl;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue.ShadowMode;
import com.jme3.scene.Spatial;

/**
 *
 * @author kehl
 */
public class WorldArmy {

    int posX, posZ;
    int playerNumber;
    int currentMovePoints;
    Spatial model;
    CharacterControl control;

    public WorldArmy() {
    }

    WorldArmy(int x, int z, int player, Spatial m, float y) {
        posX = x;
        posZ = z;
        playerNumber = player;

        model = m;
        model.setShadowMode(ShadowMode.CastAndReceive);

        model.scale(0.3f);

        model.setModelBound(new BoundingBox());
        model.updateModelBound();

        control = new CharacterControl(new CapsuleCollisionShape(1.5f, 0f, 1), 1f);
        model.addControl(control);

        control.setPhysicsLocation(new Vector3f(255-x, y, 255-z));
        lookS();

    }

    public int calculateMovePoints() {

        return 10;
    }

    public void lookN() {
        control.setViewDirection(new Vector3f(0f, 0f, 1f));
    }

    public void lookNW() {
        control.setViewDirection(new Vector3f(-1f, 0f, 1f));
    }

    public void lookNE() {
        control.setViewDirection(new Vector3f(1f, 0f, 1f));
    }

    public void lookS() {
        control.setViewDirection(new Vector3f(0f, 0f, -1f));
    }

    public void lookSW() {
        control.setViewDirection(new Vector3f(-1f, 0f, -1f));
    }

    public void lookSE() {
        control.setViewDirection(new Vector3f(1f, 0f, -1f));
    }

    public void lookW() {
        control.setViewDirection(new Vector3f(-1f, 0f, 0f));
    }

    public void lookE() {
        control.setViewDirection(new Vector3f(1f, 0f, 0f));
    }
}
