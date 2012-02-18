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
import com.jme3.light.DirectionalLight;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.UpdateControl;
import com.jme3.shadow.PssmShadowRenderer;
import com.jme3.system.NanoTimer;
import com.jme3.terrain.geomipmap.TerrainPatch;
import java.util.ArrayList;
import openwar.AudioAppState;
import openwar.DB.Army;
import openwar.Main;
import openwar.world.Tile;

/**
 *
 * @author kehl
 */
public class BattleAppState extends AbstractAppState {

    NanoTimer timer = new NanoTimer();
    long lastClickTime;
    float cameraAngle = 3f * FastMath.QUARTER_PI;
    public Node sceneNode;
    public boolean shiftPressed, ctrlPressed, altPressed;
    public Main game;
    public Terrain terrain;
    public ArrayList<Unit> teamA, teamB, selectedUnits;
    public Tile tile;
    public BattleUI uiController;
    public float speed = 80f;
    private AnalogListener analogListener = new AnalogListener() {

        @Override
        public void onAnalog(String name, float value, float tpf) {

            Vector3f loc = game.getCamera().getLocation();
            Vector3f dir = game.getCamera().getDirection();

            if (name.equals("battle_forward")) {
                loc.addLocal(tpf * speed * dir.x, 0, tpf * speed * dir.z);
            } else if (name.equals("battle_backward")) {
                loc.addLocal(tpf * speed * -dir.x, 0, tpf * speed * -dir.z);
            } else if (name.equals("battle_strafeleft")) {
                loc.addLocal(tpf * speed, 0, 0);
            } else if (name.equals("battle_straferight")) {
                loc.addLocal(tpf * -speed, 0, 0);
            } else if (name.equals("battle_strafeup")) {
                loc.addLocal(0, tpf * 10 * value, 0);
            } else if (name.equals("battle_strafedown")) {
                loc.addLocal(0, tpf * 10 * -value, 0);
            }


            game.getCamera().setLocation(loc);
//            Quaternion rot = new Quaternion();
//            rot.lookAt(dir, game.getCamera().getUp());
//            Quaternion q = new Quaternion().fromAngleAxis(cameraAngle, Vector3f.UNIT_X);
//            dir = q.mult(rot).mult(dir);
//            game.getCamera().lookAtDirection(dir, game.getCamera().getUp());




        }
    };
    public ActionListener actionListener = new ActionListener() {

        @Override
        public void onAction(String name, boolean pressed, float tpf) {




            if (name.equals("mouse_left") && !pressed) {

                CollisionResult r = game.getMousePick(sceneNode);
                if (r != null) {
                    leftMouseClick(r);
                }


            } else if (name.equals("mouse_right") && !pressed) {

                CollisionResult r = game.getMousePick(sceneNode);
                if (r != null) {
                    rightMouseClick(r);
                }


            } else if (name.equals("shift")) {
                shiftPressed = pressed;
            } else if (name.equals("ctrl")) {
                ctrlPressed = pressed;
            }else if (name.equals("alt")) {
                altPressed = pressed;
            }
        }
    };

    public void leftMouseClick(CollisionResult r) {
        Vector3f pt = r.getContactPoint();
        int x = (int) pt.x;
        int z = (int) pt.z;

        if (r.getGeometry() == null) {
            return;
        }

        if (r.getGeometry() instanceof TerrainPatch) {
            deselectAll();
            return;

        }

        Spatial spat = (Spatial) r.getGeometry();
        Unit u = getUnitBySoldier(spat);
        if (u != null) {

            // If unit is not fleeing from battle
            if (u.status != Unit.Status.Rout) {
                deselectAll();
                selectUnit(u);
            }



            return;
        }

    }

    public void rightMouseClick(CollisionResult r) {

        Vector3f pt = r.getContactPoint();
        int x = (int) pt.x;
        int z = (int) pt.z;

        if (r.getGeometry() instanceof TerrainPatch) {

            if (selectedUnits.isEmpty()) {
                return;
            }


            // Check for double click and either walk or run
            long time = timer.getTime();
            boolean run = (time - lastClickTime < 300000000);
            lastClickTime = time;

            // Check if the unit direction should change
            if(altPressed)
            {
                float dx = x-selectedUnits.get(0).currPos.x;
                float dz = z-selectedUnits.get(0).currPos.y;
                float sum = dx+dz;
                selectedUnits.get(0).setGoal(x, z,dx/sum,dz/sum,run);
            }
            else
                selectedUnits.get(0).setGoal(x, z, run);
            
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

    public BattleAppState(ArrayList<openwar.DB.Unit> a, ArrayList<openwar.DB.Unit> b) {

        teamA = new ArrayList<Unit>();
        teamB = new ArrayList<Unit>();
        selectedUnits = new ArrayList<Unit>();

        for (openwar.DB.Unit u : a) {
            teamA.add(new Unit(this, u, "A"));
        }

        for (openwar.DB.Unit u : b) {
            teamB.add(new Unit(this, u, "B"));
        }


        terrain = new Terrain(this);
        sceneNode = new Node("Battle");
        sceneNode.addControl(new UpdateControl());


    }

    public BattleAppState(ArrayList<Army> a, ArrayList<Army> b, Tile t) {


        terrain = new Terrain(this);
        tile = t;
        sceneNode = new Node("Battle");


        teamA = new ArrayList<Unit>();
        teamB = new ArrayList<Unit>();

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


        terrain.createData();


        float x = 200, z = 100;
        for (Unit u : teamA) {
            u.createData();
            u.setPosition(x, z, 0, 1);
            z -= 10;

        }

        x = 200;
        z = 200;
        for (Unit u : teamB) {
            u.createData();
            u.setPosition(x, z, 0, -1);

            z += 10;

        }



        sceneNode.attachChild(terrain.terrainQuad);

        game.rootNode.attachChild(sceneNode);
        game.getCamera().lookAtDirection(new Vector3f(0f, -1f, 2f).normalizeLocal(), Vector3f.UNIT_Y);
        game.getCamera().setLocation(new Vector3f(215, 50, 40));



        DirectionalLight dlight = new DirectionalLight();
        Vector3f col = Main.DB.sun_color;
        dlight.setColor(new ColorRGBA(col.x / 255f, col.y / 255f, col.z / 255f, 1));
        dlight.setDirection(Main.DB.sun_direction);
        sceneNode.addLight(dlight);
        PssmShadowRenderer pssm = new PssmShadowRenderer(game.getAssetManager(), 1024, 8);
        pssm.setDirection(Main.DB.sun_direction);
        game.getViewPort().addProcessor(pssm);





        game.audioState.setMusicMode(AudioAppState.MusicMode.BattleIdle);
        uiController = new BattleUI();
        uiController.game = game;
        game.nifty.fromXml("ui/battle/ui.xml", "start", uiController);



        initialized = true;



    }

    @Override
    public void update(float tpf) {

        for (Unit u : teamA) {
            u.update(tpf);

        }
        for (Unit u : teamB) {
            u.update(tpf);
        }

    }

    public int ensureMinMax(int value, int min, int max) {
        return Math.min(max, Math.max(min, value));
    }

    public float ensureMinMax(float value, float min, float max) {
        return Math.min(max, Math.max(min, value));


    }
}
