/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package openwar.battle;

import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;

/**
 *
 * @author kehl
 */
public class Projectile {
    
    Spatial model;
    Node node;
    float hitRadius,initialVelocity;
    boolean consumedOnHit;
    
    Vector3f currVelocity;
    
    
    public Projectile()
    {
        hitRadius = 0.01f;
        initialVelocity = 150f;
        consumedOnHit=true;
    }
    
}
