/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package openwar.battle;

import com.jme3.app.Application;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.collision.CollisionResult;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.AnalogListener;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.post.FilterPostProcessor;
import com.jme3.post.filters.BloomFilter;
import com.jme3.post.filters.FogFilter;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.renderer.queue.RenderQueue.Bucket;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.UpdateControl;
import com.jme3.scene.shape.Cylinder;
import com.jme3.scene.shape.Quad;
import com.jme3.shadow.PssmShadowRenderer;
import com.jme3.system.NanoTimer;
import com.jme3.terrain.geomipmap.TerrainPatch;
import com.jme3.texture.Image;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture2D;
import com.jme3.util.SkyFactory;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import jme3tools.optimize.GeometryBatchFactory;
import openwar.AudioAppState;
import openwar.DB.Army;
import openwar.Main;
import openwar.world.Tile;

/**
 *
 * @author kehl, hermetic
 */
public class BattleAppState extends AbstractAppState {

    public enum DragMode {

        None,
        Selection,
        Formation
    }
    
    NanoTimer timer = new NanoTimer();
    public Spatial dragStartShape, dragEndShape;
    long lastClickTime;
    public Vector2f cursorPos;
    public Vector3f camLoc, camDir, camLeft, camUp, dragModeStartPoint, dragModeEndPoint;
    public Geometry selectionQuad;
    Quaternion camRot;
    float camMoveSpeed = 80f, camTurnSpeed = 0.7f, camLeftAngle = 0f, camUpAngle = 0.5f;
    public Node sceneNode;
    public boolean shiftPressed, ctrlPressed, altPressed, leftPressed, rightPressed;
    DragMode dragMode = DragMode.None;
    public Main game;
    public Terrain terrain;
    public ArrayList<Unit> teamA, teamB, selectedUnits;
    public Tile tile;
    public BattleUI uiController;
    public HashMap<Spatial, Soldier> hashedSoldiers;
    public HashMap<Spatial, Projectile> hashedProjectiles;
    public BattleCollisionListener collisionListener;
    public Texture selectionTexture;
    
    private AnalogListener analogListener = new AnalogListener() {

        @Override
        public void onAnalog(String name, float value, float tpf) {



            camLoc = game.getCamera().getLocation();
            camDir = game.getCamera().getDirection();
            camLeft = game.getCamera().getLeft();

            if (name.equals("battle_forward")) {
                camLoc.addLocal(tpf * camMoveSpeed * camDir.x, 0, tpf * camMoveSpeed * camDir.z);
            } else if (name.equals("battle_backward")) {
                camLoc.addLocal(tpf * camMoveSpeed * -camDir.x, 0, tpf * camMoveSpeed * -camDir.z);
            } else if (name.equals("battle_strafeleft")) {
                camLoc.addLocal(camLeft.mult(tpf * camMoveSpeed));
            } else if (name.equals("battle_straferight")) {
                camLoc.addLocal(camLeft.mult(-tpf * camMoveSpeed));
            } else if (name.equals("battle_strafeup")) {
                camLoc.addLocal(0, tpf * 10 * value, 0);
            } else if (name.equals("battle_strafedown")) {
                camLoc.addLocal(0, tpf * 10 * -value, 0);
            } else if (name.equals("mouse_moveup") || name.equals("mouse_movedown")
                    || name.equals("mouse_moveleft") || name.equals("mouse_moveright")) {
                if (leftPressed || rightPressed) {

                    CollisionResult r = game.getMousePick(terrain.terrainQuad);
                    if (r == null) {
                        return;
                    }

                    if (dragMode == DragMode.None) {

                        if (rightPressed && !selectedUnits.isEmpty()) {
                            dragMode = DragMode.Formation;
                            selectedUnits.get(0).togglePreviewFormation(true);
                            sceneNode.attachChild(dragStartShape);
                            sceneNode.attachChild(dragEndShape);
                        } else {
                            dragMode = DragMode.Selection;
                            sceneNode.attachChild(selectionQuad);

                        }

                        dragModeStartPoint = r.getContactPoint().clone();
                    }

                }
            }


            game.getCamera().setLocation(camLoc);


            if (dragMode != DragMode.None) {

                CollisionResult r = game.getMousePick(terrain.terrainQuad);
                if (r == null) {
                    return;
                }
                dragModeEndPoint = r.getContactPoint();
                Vector3f start = game.getCamera().getScreenCoordinates(dragModeStartPoint);
                Vector3f end = game.getCamera().getScreenCoordinates(dragModeEndPoint);

                if (dragMode == DragMode.Selection) {
                    selectionQuad.setLocalTranslation(Math.min(start.x, end.x), Math.min(start.y, end.y), 0);
                    selectionQuad.setLocalScale(
                            FastMath.abs(start.x - end.x), FastMath.abs(start.y - end.y), 1);
                } else {
                    dragStartShape.setLocalTranslation(dragModeStartPoint);
                    dragEndShape.setLocalTranslation(dragModeEndPoint);

                    selectedUnits.get(0).previewFormation(dragModeStartPoint, dragModeEndPoint);
                }

            }

        }
    };
    public ActionListener actionListener = new ActionListener() {

        @Override
        public void onAction(String name, boolean pressed, float tpf) {




            if (name.equals("mouse_left")) {
                leftPressed = pressed;

                if (!pressed) {

                    if (dragMode == DragMode.Selection) {
                        dragMode = DragMode.None;
                        sceneNode.detachChild(selectionQuad);

                        selectedUnits.clear();
                        float l, r, u, d;
                        l = Math.min(dragModeStartPoint.x, dragModeEndPoint.x);
                        r = Math.max(dragModeStartPoint.x, dragModeEndPoint.x);
                        d = Math.min(dragModeStartPoint.z, dragModeEndPoint.z);
                        u = Math.max(dragModeStartPoint.z, dragModeEndPoint.z);

                        System.err.println(l + " " + r + " " + u + " " + d);
                        for (Soldier s : hashedSoldiers.values()) {
                            if (s.currPos.x > l && s.currPos.x < r
                                    && s.currPos.y > d && s.currPos.y < u) {
                                if (!selectedUnits.contains(s.unit)) {
                                    selectUnit(s.unit);
                                }
                            }

                        }
                        return;
                    }
                    dragMode = DragMode.None;



                    CollisionResult r = game.getMousePick(sceneNode);
                    if (r != null) {
                        leftMouseClick(r);
                    }
                }
            } else if (name.equals("mouse_right")) {
                rightPressed = pressed;

                if (!pressed) {


                    if (dragMode == DragMode.Formation) {
                        dragMode = DragMode.None;
                        sceneNode.detachChild(dragStartShape);
                        sceneNode.detachChild(dragEndShape);
                        selectedUnits.get(0).acceptFormationPreview(dragModeStartPoint, dragModeEndPoint, true);
                        return;
                    }


                    dragMode = DragMode.None;
                    CollisionResult r = game.getMousePick(sceneNode);

                    if (r != null) {
                        rightMouseClick(r);
                    }
                }

            } else if (name.equals("shift")) {
                shiftPressed = pressed;
            } else if (name.equals("ctrl")) {
                ctrlPressed = pressed;
            } else if (name.equals("alt")) {
                altPressed = pressed;
            }
        }
    };

    public void leftMouseClick(CollisionResult r) {
        Vector3f pt = r.getContactPoint();

        if (r.getGeometry() == null) {
            return;
        }

        if (r.getGeometry() instanceof TerrainPatch) {
            System.err.println(pt);
            deselectAll();
            return;

        }

        Spatial spat = (Spatial) r.getGeometry().getParent().getParent();
        Unit u = getUnitBySoldier(spat);
        if (u != null) {

            // If unit is not fleeing from battle
            if (u.status != Unit.Status.Routing) {
                deselectAll();
                selectUnit(u);
            }



            return;
        }

    }

    public void rightMouseClick(CollisionResult r) {

        Vector3f pt = r.getContactPoint();
        
        
        if (r.getGeometry() instanceof TerrainPatch) {

            if (selectedUnits.isEmpty()) {
                return;
            }

            Vector2f goal = new Vector2f(pt.x, pt.z);
            Vector2f meanPos = new Vector2f();
            Vector2f meanDir = new Vector2f();
            for (Unit u : selectedUnits) {
                meanPos.addLocal(u.currPos);
                meanDir.addLocal(u.currDir);

            }
            meanPos.divideLocal(selectedUnits.size());
            meanDir.normalizeLocal();


            // Check for double click and either walk or run
            long time = timer.getTime();
            boolean run = (time - lastClickTime < 300000000);
            lastClickTime = time;


            for (Unit u : selectedUnits) {
                Vector2f finalPos = goal.add(u.currPos.subtract(meanPos));

                u.enemy = null;
                
                // Check if the unit direction should change
                if (altPressed) {
                    float dx = finalPos.x - u.currPos.x;
                    float dz = finalPos.y - u.currPos.y;
                    u.setGoal(finalPos.x, finalPos.y, dx, dz, ctrlPressed | run);
                } else {
                    u.setGoal(finalPos.x, finalPos.y, ctrlPressed | run);
                }
            }




            return;

        }
        
        Spatial spat = (Spatial) r.getGeometry();
        Unit u = getUnitBySoldier(spat);
        if (u != null) {

                       for (Unit selected : selectedUnits) 
                           selected.attackUnit(u, ctrlPressed);




            return;
        }



    }

    public void deselectAll() {
        for (Unit u : selectedUnits) {
            u.toggleSelection(false);
        }
        selectedUnits.clear();
    }

    public void selectUnit(Unit u) {
        selectedUnits.add(u);
        u.toggleSelection(true);

    }

    public Unit getUnitBySoldier(Spatial sp) {
        for (Unit u : teamA) {
            for (Soldier s : u.soldiers) {
                if (s.model == sp) {
                    return u;
                }
            }
        }

        for (Unit u : teamB) {
            for (Soldier s : u.soldiers) {
                if (s.model == sp) {
                    return u;
                }
            }
        }

        return null;
    }

    public BattleAppState() {
        teamA = new ArrayList<Unit>();
        teamB = new ArrayList<Unit>();
        selectedUnits = new ArrayList<Unit>();
        terrain = new Terrain(this);
        sceneNode = new Node("Battle");
        sceneNode.addControl(new UpdateControl());
        hashedSoldiers = new HashMap<Spatial, Soldier>();
        hashedProjectiles = new HashMap<Spatial,Projectile>();



    }

    public BattleAppState(ArrayList<openwar.DB.Unit> a, ArrayList<openwar.DB.Unit> b) {

        this();

        for (openwar.DB.Unit u : a) {
            teamA.add(new Unit(this, u, "romans"));
        }

        for (openwar.DB.Unit u : b) {
            teamB.add(new Unit(this, u, "greeks"));
        }




    }

    public BattleAppState(ArrayList<Army> a, ArrayList<Army> b, Tile t) {


        this();

        for (Army army : a) {
            for (openwar.DB.Unit u : army.units) {
                teamA.add(new Unit(this, u, army.owner));
            }
        }

        for (Army army : b) {
            for (openwar.DB.Unit u : army.units) {
                teamB.add(new Unit(this, u, army.owner));
            }
        }



    }

    @Override
    public void initialize(AppStateManager stateManager, Application app) {
        game = (Main) app;


        game.getInputManager().addListener(actionListener, "mouse_left");
        game.getInputManager().addListener(actionListener, "mouse_right");
        game.getInputManager().addListener(actionListener, "shift");
        game.getInputManager().addListener(actionListener, "ctrl");
        game.getInputManager().addListener(actionListener, "alt");

        game.getInputManager().addListener(analogListener, "battle_strafeup");
        game.getInputManager().addListener(analogListener, "battle_strafedown");
        game.getInputManager().addListener(analogListener, "battle_strafeleft");
        game.getInputManager().addListener(analogListener, "battle_straferight");
        game.getInputManager().addListener(analogListener, "battle_forward");
        game.getInputManager().addListener(analogListener, "battle_backward");

        game.getInputManager().addListener(analogListener, "mouse_moveleft");
        game.getInputManager().addListener(analogListener, "mouse_moveright");
        game.getInputManager().addListener(analogListener, "mouse_moveup");
        game.getInputManager().addListener(analogListener, "mouse_movedown");


        terrain.createData();
        CreateGrass();
        createSky();
 
        dragStartShape = (Spatial) new Geometry("", new Cylinder(8, 8, 0.25f, 3f, true));
        dragStartShape.setMaterial(new Material(game.getAssetManager(), "Common/MatDefs/Light/Lighting.j3md"));
        dragStartShape.setLocalRotation(new Quaternion().fromAngleAxis(FastMath.HALF_PI, Vector3f.UNIT_X));
        dragEndShape = dragStartShape.clone();
        
        collisionListener = new BattleCollisionListener(this);
        game.bulletState.getPhysicsSpace().addCollisionListener(collisionListener);


        float z = -20;
        for (Unit u : teamA) {
            u.createData();
            u.setPosition(0, z, 0, 1);
            z -= u.formation.getDepth() + 5;

        }

        z = 20;
        for (Unit u : teamB) {
            u.createData();
            u.setPosition(0, z, 0, -1);

            z += u.formation.getDepth() + 5;

        }
        
        /*Spatial sky = SkyFactory.createSky(game.getAssetManager(), "map/9.tga", true);
        sceneNode.attachChild(sky);*/
        
        
        sceneNode.attachChild(terrain.terrainQuad);

        game.rootNode.attachChild(sceneNode);
        camRot = new Quaternion().fromAngles(camUpAngle, camLeftAngle, 0);
        game.getCamera().setRotation(camRot);
        Vector3f start = new Vector3f(0, 30, -100);
        game.getCamera().setLocation(start.addLocal(0, teamA.get(0).soldiers.get(0).node.getLocalTranslation().y, 0));


        dragModeStartPoint = new Vector3f();
        dragModeEndPoint = new Vector3f();

        DirectionalLight dlight = new DirectionalLight();
        Vector3f col = Main.DB.sun_color;
        dlight.setColor(new ColorRGBA(col.x / 255f, col.y / 255f, col.z / 255f, 1));
        dlight.setDirection(Main.DB.sun_direction);
        sceneNode.addLight(dlight);
        PssmShadowRenderer pssm = new PssmShadowRenderer(game.getAssetManager(), 2048, 16);
        pssm.setDirection(Main.DB.sun_direction);
        game.getViewPort().addProcessor(pssm);

 
  
           if(!Main.devMode)
        {
                    
           FilterPostProcessor fpp = new FilterPostProcessor(game.getAssetManager());

            
        FogFilter fog = new FogFilter();
        fog.setFogColor(new ColorRGBA(0.9f, 0.7f, 0.7f, 1.0f));
        fog.setFogDistance(155);
        fog.setFogDensity(1.0f);
        fpp.addFilter(fog);


        BloomFilter bloom = new BloomFilter();
        bloom.setExposurePower(8f);
        bloom.setBloomIntensity(1.5f);
        fpp.addFilter(bloom);
        game.getViewPort().addProcessor(fpp);
        }
        

        selectionQuad = new Geometry("", new Quad(1, 1));
        game.guiNode.attachChild(selectionQuad);
        Material mat = new Material(game.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
        mat.getAdditionalRenderState().setWireframe(true);
        selectionQuad.setMaterial(mat);
        selectionQuad.setQueueBucket(Bucket.Gui);

        game.audioState.setMusicMode(AudioAppState.MusicMode.BattleIdle);
        uiController = new BattleUI();
        uiController.game = game;
        game.nifty.fromXml("ui/battle/ui.xml", "start", uiController);



        initialized = true;



    }

    @Override
    public void update(float tpf) {

        cursorPos = game.getInputManager().getCursorPosition();
        if (cursorPos.x < 3f) {
            camLeftAngle += camTurnSpeed * tpf;
        } else if (cursorPos.x > game.getCamera().getWidth() - 3) {
            camLeftAngle -= camTurnSpeed * tpf;
        }
        if (cursorPos.y < 3f) {
            camUpAngle += camTurnSpeed * tpf;
        } else if (cursorPos.y > game.getCamera().getHeight() - 3) {
            camUpAngle -= camTurnSpeed * tpf;
        }
        camUpAngle = ensureMinMax(camUpAngle, -FastMath.QUARTER_PI, FastMath.HALF_PI);
        camRot.fromAngles(camUpAngle, camLeftAngle, 0);
        game.getCamera().setRotation(camRot);


        if (dragMode == DragMode.Selection) {
            Vector3f start = game.getCamera().getScreenCoordinates(dragModeStartPoint);
            Vector3f end = game.getCamera().getScreenCoordinates(dragModeEndPoint);
            selectionQuad.setLocalTranslation(Math.min(start.x, end.x), Math.min(start.y, end.y), 0);
            selectionQuad.setLocalScale(
                    FastMath.abs(start.x - end.x), FastMath.abs(start.y - end.y), 1);
        }





        for (Unit u : teamA) {
            u.update(tpf);

        }
        for (Unit u : teamB) {
            u.update(tpf);
        }

    }

        private void createSky() {
        sceneNode.attachChild(SkyFactory.createSky(game.getAssetManager(), "textures/Sky/Bright/BrightSky.dds", false));
    }
  
           public void CreateGrass() {
        Spatial spatial = createGrassSpatial();
        Node grassNode = new Node("grassPatch");
        
        System.out.println("terrain: "+terrain.heightmap.getSize());
          for (int j = 0; j < 1000; j++) {
            float x = FastMath.rand.nextInt(terrain.heightmap.getSize());
            float z = FastMath.rand.nextInt(terrain.heightmap.getSize());
            float y = terrain.getHeight(x-terrain.heightmap.getSize()/2, z-terrain.heightmap.getSize()/2);
            System.out.println(x+", "+z+" y: "+y);

            Spatial grass = spatial.clone();
            grass.setLocalTranslation(x, y-0.2f, z);

            grassNode.attachChild(grass);
        }
         spatial = createTreeSpatial();
         
      for (int i = 0; i < 30; i++) {
            float x = FastMath.rand.nextInt(terrain.heightmap.getSize());
            float z = FastMath.rand.nextInt(terrain.heightmap.getSize());
            float y = terrain.getHeight(x-terrain.heightmap.getSize()/2, z-terrain.heightmap.getSize()/2);
            System.out.println(x+", "+z+" y: "+y);

            Spatial grass = spatial.clone();
            grass.setLocalTranslation(x, y, z);
        grass.scale(FastMath.rand.nextInt(2)+2); // make tree bigger
       float angle = 45 * FastMath.DEG_TO_RAD;
        grass.setLocalRotation(new Quaternion(new float[]{0, angle, 0}));
 
            grassNode.attachChild(grass);
        }

        Node optimizedGrass = GeometryBatchFactory.optimize(grassNode, true);
  optimizedGrass.setShadowMode(RenderQueue.ShadowMode.CastAndReceive);
       optimizedGrass.setLocalTranslation(-terrain.heightmap.getSize()/2, 0, -terrain.heightmap.getSize()/2);
        sceneNode.attachChild(optimizedGrass);
    }

    public Spatial createGrassSpatial() {
        float angle = 90 * FastMath.DEG_TO_RAD;

        Quad q = new Quad(2f, 2f);
        Geometry g = new Geometry("Grass4_Quad1", q);

        //Material mat = game.getAssetManager().loadMaterial("MatDefs/Vegetation/Grass_4.j3m");
        Material mat = game.getAssetManager().loadMaterial("materials/myGrassmaterial.j3m");
        /*Material mat = game.getAssetManager().loadMaterial("Common/MatDefs/Misc/Unshaded.j3md");
        mat.setTexture("ColorMap", game.getAssetManager().loadTexture("Textures/manowaruvmap.png"));*/
 
        g.setMaterial(mat);
        g.setLocalTranslation(-1f, 0, 0);

        Quad q2 = new Quad(2f, 2f);
        Geometry g2 = new Geometry("Grass4_Quad2", q2);

        g2.setMaterial(mat);
        g2.setLocalTranslation(0, 0, 1f);
        g2.setLocalRotation(new Quaternion(new float[]{0, angle, 0}));

        Node bb = new Node("Grass4_Billboard");
 
        bb.attachChild(g);
        bb.attachChild(g2);

        return bb;
    }
      public Spatial createTreeSpatial() {
         
        Spatial treeSpat = game.getAssetManager().loadModel("models/Tree/Tree.mesh.j3o");

        treeSpat.setQueueBucket(Bucket.Transparent); // leaves are transparent
  
        
       Node treeNode = new Node("treePatch");
       treeNode.attachChild(treeSpat);

        return treeNode;
      }
      
    public int ensureMinMax(int value, int min, int max) {
        return Math.min(max, Math.max(min, value));
    }

    public float ensureMinMax(float value, float min, float max) {
        return Math.min(max, Math.max(min, value));


    }
}

