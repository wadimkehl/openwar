/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package openwar.world;

import com.jme3.material.Material;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue.ShadowMode;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;

/**
 *
 * @author kehl
 */
public class WorldDecoration {

    public Vector3f pos;
    public Vector3f rot;
    public Vector3f scale;
    public Spatial model;
    public Node node;
    public WorldMap map;
    public String refName;

    public WorldDecoration() {
        
        node = new Node();
    }

    public void createData(WorldMap m) {

        this.map = m;

        model.setMaterial(new Material(map.game.getAssetManager(), "Common/MatDefs/Light/Lighting.j3md"));
        model.setShadowMode(ShadowMode.CastAndReceive);
        node.setLocalTranslation(pos);
        model.setLocalRotation(new Quaternion().fromAngles(rot.x, rot.y, rot.z));
        model.setLocalScale(scale);
        node.attachChild(model);

        map.scene.attachChild(node);


    }
}
