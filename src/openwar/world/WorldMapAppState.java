/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package openwar.world;

import openwar.DB.Army;
import com.jme3.app.Application;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.collision.CollisionResult;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.AnalogListener;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.UpdateControl;
import com.jme3.terrain.geomipmap.TerrainPatch;
import java.util.ArrayList;
import openwar.DB.Settlement;
import openwar.DB.Unit;
import openwar.Main;
import openwar.battle.BattleAppState;

/**
 *
 * @author kehl
 */
public class WorldMapAppState extends AbstractAppState {

    float cameraAngle = 3f * FastMath.QUARTER_PI;
    public Node sceneNode;
    public boolean showGrid;
    public boolean shiftPressed, ctrlPressed, altPressed;
    public WorldMap map;
    public Main game;
    public WorldMapUI uiController;
    public WorldMapGameLogic logic;
    
    public ArrayList<Army> army1,army2;
    
    
    private AnalogListener analogListener = new AnalogListener() {

        @Override
        public void onAnalog(String name, float value, float tpf) {

            if (!isEnabled()) {
                return;
            }

            Vector3f loc = game.getCamera().getLocation();
            Vector3f dir = game.getCamera().getDirection();

            if (name.equals("map_strafeup")) {
                loc.addLocal(0, 0, tpf * -35f);
            } else if (name.equals("map_strafedown")) {
                loc.addLocal(0, 0, tpf * 35f);
            } else if (name.equals("map_strafeleft")) {
                loc.addLocal(tpf * -35f, 0, 0);
            } else if (name.equals("map_straferight")) {
                loc.addLocal(tpf * 35f, 0, 0);
            } else if (name.equals("map_scrollup")) {
                cameraAngle += tpf * value * 0.8f;
            } else if (name.equals("map_scrolldown")) {
                cameraAngle -= tpf * value * 0.8f;
            }

            if (!Main.devMode) {
                // Some math to ensure we don't leave the map
                float c = -loc.y / dir.y;
                float z0 = -c * dir.z;
                float z1 = map.width - c * dir.z;
                if (loc.x < 0f) {
                    loc.x = 0;
                } else if (loc.x > map.width) {
                    loc.x = map.width;
                }
                if (loc.z < z0) {
                    loc.z = z0;
                } else if (loc.z > z1) {
                    loc.z = z1;
                }
                if (cameraAngle <= 2.15f) {
                    cameraAngle = 2.15f;
                } else if (cameraAngle >= 2.7f) {
                    cameraAngle = 2.7f;
                }

                loc.y = 20f * FastMath.pow(2, 3f * (3f * FastMath.QUARTER_PI - cameraAngle));
            }

            game.getCamera().setLocation(loc);
            Quaternion rot = new Quaternion();
            rot.lookAt(dir, game.getCamera().getUp());
            Quaternion q = new Quaternion().fromAngleAxis(cameraAngle, Vector3f.UNIT_X);
            dir = q.mult(rot).mult(dir);
            game.getCamera().lookAtDirection(dir, game.getCamera().getUp());




        }
    };
    public ActionListener actionListener = new ActionListener() {

        @Override
        public void onAction(String name, boolean pressed, float tpf) {

            if (!isEnabled() || uiController.currentPopUpActive) {
                return;
            }


            if (name.equals("mouse_left") && !pressed) {

                CollisionResult r = game.getMousePick(map.scene);
                if (r != null) {
                    leftMouseClick(r);
                }


            } else if (name.equals("mouse_right") && !pressed) {

                if ((map.selectedArmy != null) || !uiController.selectedUnits.isEmpty()) {
                    CollisionResult r = game.getMousePick(map.scene);
                    if (r != null) {
                        rightMouseClick(r);
                    }
                }

            } else if (name.equals("show_grid") && !pressed) {
                showGrid = !showGrid;
                map.showGrid(showGrid);
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
        String name = r.getGeometry().getName();
        if (r.getGeometry() instanceof TerrainPatch || (name != null && name.equals("reachableArea"))) {

            if (Main.devMode) {
                System.err.println(map.worldTiles[x][z]);
            }

            map.deselectAll();
            return;

        }

        Spatial spat = (Spatial) r.getGeometry();
        Army a = map.getArmy(spat);
        if(a == null) a = map.getArmy(spat.getParent());
        if (a != null && a != map.selectedArmy) {

            if (!a.owner.equals(logic.playerFaction) && !Main.devMode) {
                return;
            }

            map.selectArmy(a);
            game.playSound("world_select_army");

            return;
        }

        Settlement s = map.getSettlement(spat);
        if (s != null) {

            if (!s.owner.equals(logic.playerFaction) && !Main.devMode) {
                return;
            }

            map.selectSettlement(s);
            game.playSound("world_select_settlement");

            return;
        }

    }

    public void rightMouseClick(CollisionResult r) {

        Vector3f pt = r.getContactPoint();
        int x = (int) pt.x;
        int z = (int) pt.z;

        Army a = null;
        if (!uiController.selectedUnits.isEmpty()) {


            int points = 10000;
            for (Unit u : uiController.selectedUnits) {
                points = Math.min(u.currMovePoints, points);
            }
            if (points < map.getTileCosts(x, z)) {
                map.game.playSound("army_deny");
                return;

            } else if (map.selectedArmy != null) {

                if (uiController.selectedUnits.size() < map.selectedArmy.units.size()) {
                    a = map.selectedArmy.splitThisArmy(uiController.selectedUnits);
                    map.game.playSound("army_split");
                } else {
                    a = map.selectedArmy;
                    map.game.playSound("army_march");

                }

            } else {
                a = map.selectedSettlement.dispatchArmy(uiController.selectedUnits);
                map.game.playSound("army_dispatch");

            }

            map.selectArmy(a);
            map.marchTo(a, x, z);
            return;


        } else if (map.selectedArmy != null) {
            a = map.selectedArmy;
        }

        if (map.selectedArmy.currMovePoints < map.getTileCosts(x, z)) {
            map.game.playSound("army_deny");
            return;
        }

        // Check if we ordered a march command
        if (r.getGeometry() instanceof TerrainPatch || r.getGeometry().getName().equals("reachableArea")) {
            map.marchTo(a, x, z);
            game.playSound("army_march");
            return;
        }

        // TODO: BLENDER exports spatial into two cascaded nodes!
        Spatial s = (Spatial) r.getGeometry();
        Army ar = map.getArmy(s);
        if(ar == null) ar = map.getArmy(s.getParent());
        if (ar != null) {

            if (ar == map.selectedArmy) {
                if (ar.canCargo() && !ar.cargo.isEmpty()) {
                    ar.unloadCargo();
                }
            }

            if (!ar.owner.equals(a.owner)) {
                game.playSound("army_attack");
            } else {
                game.playSound("army_march");
            }
            map.marchTo(a, ar);
            return;
        }

        Settlement c = map.getSettlement(s);
        if (c != null) {
            map.marchTo(a, c);
            game.playSound("army_march");
            return;
        }

    }

    public WorldMapAppState(Main game) {
        
          logic = new WorldMapGameLogic(game);

    }

    @Override
    public void initialize(AppStateManager stateManager, Application app) {
        game = (Main) app;

        game.getInputManager().addListener(actionListener, "mouse_left");
        game.getInputManager().addListener(actionListener, "mouse_right");
        game.getInputManager().addListener(actionListener, "show_grid");
        game.getInputManager().addListener(actionListener, "shift");
        game.getInputManager().addListener(actionListener, "ctrl");
        game.getInputManager().addListener(actionListener, "alt");

        game.getInputManager().addListener(analogListener, "map_strafeup");
        game.getInputManager().addListener(analogListener, "map_strafedown");
        game.getInputManager().addListener(analogListener, "map_strafeleft");
        game.getInputManager().addListener(analogListener, "map_straferight");
        game.getInputManager().addListener(analogListener, "map_scrollup");
        game.getInputManager().addListener(analogListener, "map_scrolldown");


        sceneNode = new Node("WorldMapScene");
        sceneNode.addControl(new UpdateControl());

        game.rootNode.attachChild(sceneNode);
        

        map = new WorldMap(game, sceneNode);
        if (!map.createWorldMap()) {
            game.wishToQuit = true;
        }

        

        uiController = new WorldMapUI(game);
        game.nifty.fromXml("ui/worldmap/ui.xml", "start", uiController);
             

        initialized = true;



    }
    
    
    public void beginGame()
    {
        map.minimap = new WorldMinimap(map);

        logic.beginGame();

        
        game.getCamera().lookAtDirection(new Vector3f(0f, -1f, -1f).normalizeLocal(), Vector3f.UNIT_Y);
        game.getCamera().getLocation().y = 20f;
        moveCameraTo(Main.DB.settlements.get(Main.DB.factions.get(logic.playerFaction).capital));

    }

    @Override
    public void update(float tpf) {

            map.update(tpf);

    }

    public void enableStateAndScene() {
        setEnabled(true);
        game.rootNode.attachChild(sceneNode);
        game.getViewPort().addProcessor(map.fpp);
        game.getViewPort().addProcessor(map.pssm);
    }

    public void disableStateAndScene() {
        setEnabled(false);
        game.guiNode.detachAllChildren();
        game.rootNode.detachAllChildren();
        game.getViewPort().removeProcessor(map.fpp);
        game.getViewPort().removeProcessor(map.pssm);
    }

    public void moveCameraTo(Tile t) {
        moveCameraTo(t.x, t.z);
    }

    public void moveCameraTo(Spatial s) {
        moveCameraTo((int) s.getWorldTranslation().x, (int) s.getWorldTranslation().z);
    }

    public void moveCameraTo(Army a) {
        moveCameraTo(a.posX, a.posZ);
    }

    public void moveCameraTo(Settlement s) {
        moveCameraTo(s.posX, s.posZ);
    }

    public void moveCameraTo(int x, int z) {
        Vector3f l = game.getCamera().getLocation();
        Vector3f d = game.getCamera().getDirection();
        Vector3f goal = new Vector3f(x + 0.5f, 0f, z + 0.5f);
        game.getCamera().setLocation(goal.add(d.mult(l.y / d.y)));

    }

    public int ensureMinMax(int value, int min, int max) {
        return Math.min(max, Math.max(min, value));


    }
    
    public int resolveBattle()
    {
          int power1 = 0, power2 = 0;

        for (Army a : army1) {
            power1 += a.units.size();
        }
        for (Army a : army2) {
            power2 += a.units.size();
        }

        if (power1 >= power2) {
            for (Army a : army2) {
                map.removeArmy(a);
                game.playSound("army_death");
            }
            return 1;
        }
        if (power1 < power2) {
            for (Army a : army1) {
                map.removeArmy(a);
                game.playSound("army_death");

            }
            return 2;
        } else {
            return 0;
        }
    }

    public void initiateBattle(ArrayList<Army> a1, ArrayList<Army> a2) {

//        BattleAppState b = new BattleAppState(a1, a2, new Tile(a1.get(0).posX, a1.get(0).posZ));
//        game.gameLoaderState.loadBattle(b);
//
//
//        return 0;
            army1 = a1;
            army2=a2;
            game.createUIPopUp("popup_battle");
            game.setPopUpText("popup_text0","BATTLE!!!");
            game.showUIPopUp();

            
      
    }

    public int siege(ArrayList<Army> a1, ArrayList<Army> a2, Settlement s) {
        int power1 = 0, power2 = 0;

        for (Army a : a1) {
            power1 += a.units.size();
        }
        for (Army a : a2) {
            power2 += a.units.size();
        }

        power2 += s.units.size();

        if (power1 > power2) {
            for (Army a : a2) {
                map.removeArmy(a);
                game.playSound("army_death");
            }
            s.units.clear();
            s.changeOwner(a1.get(0).owner);
            for (Army a : a1) {
                a.garrisonArmy(s);
            }

            return 1;
        }
        if (power1 < power2) {
            for (Army a : a1) {
                map.removeArmy(a);
                game.playSound("army_death");

            }
            return 2;
        } else {
            return 0;
        }
    }
}
