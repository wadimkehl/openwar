/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package openwar.world;

import java.util.ArrayList;
import java.util.Set;
import openwar.DB.Army;
import openwar.DB.Faction;
import openwar.DB.Settlement;
import openwar.Main;

/**
 *
 * @author kehl
 */
public class WorldMapGameLogic {

    Main game;
    WorldAI ai;
    public String playerFaction;
    public int currentRound = 0;
    public String currentTurn;
    public ArrayList<String> turnOrder = new ArrayList<String>();
    public int turnOrderCounter = 0;

    public void beginGame() {


        ai = new WorldAI(this);

        turnOrder.clear();
        turnOrderCounter = 0;


        // Create the correct turn order for the factions, starting with the player
        turnOrder.add(playerFaction);
        while (turnOrder.size() != Main.DB.factions.values().size()) {

            int currMin = 1000000;
            String currFaction = null;
            for (Faction f : Main.DB.factions.values()) {

                if (Main.DB.genFactions.get(f.refName).turnOrder < currMin && !turnOrder.contains(f.refName)) {
                    currMin = Main.DB.genFactions.get(f.refName).turnOrder;
                    currFaction = f.refName;
                }
            }
            turnOrder.add(currFaction);
        }


        for (Faction f : Main.DB.factions.values()) {
            for (Army a : f.armies) {
                a.resetMovePoints();
            }
        }

        for (Settlement s : Main.DB.settlements.values()) {
            s.resetMovePoints();

        }

        game.doScript("onBeginGame()");

        beginTurn();
    }

    public void beginRound() {
        currentRound++;
        turnOrderCounter = 0;

        for (Faction f : Main.DB.factions.values()) {
            for (Army a : f.armies) {
                a.resetMovePoints();
            }
        }

        for (Settlement s : Main.DB.settlements.values()) {
            s.newRound();
            s.resetMovePoints();

        }

        currentTurn = turnOrder.get(turnOrderCounter);
        game.doScript("onBeginRound(" + currentRound + ")");

        beginTurn();
    }

    public void beginTurn() {


        Faction f = Main.DB.factions.get(currentTurn);
        game.doScript("onBeginTurn('" + currentTurn + "')");



        if (currentTurn.equals(playerFaction)) {
            if (game.worldMapState.map.selectedArmy != null) {
                game.worldMapState.map.selectArmy(game.worldMapState.map.selectedArmy);
            } else if (game.worldMapState.map.selectedSettlement != null) {
                game.worldMapState.uiController.selectSettlement(game.worldMapState.map.selectedSettlement);
                game.worldMapState.uiController.drawReachableArea();
            }


        } else {

            ai.calculateInfluenceMap(f);
            game.doScript("doAI('"+f.refName+"')");
            endTurn();
        }
        



    }

    public void endTurn() {

        Faction f = Main.DB.factions.get(currentTurn);
        game.doScript("onEndTurn('" + currentTurn + "')");



        if (currentTurn.equals(playerFaction)) {
            game.worldMapState.uiController.deselectAll();

            game.playSound("turn_end");



        }


        turnOrderCounter++;
        if (turnOrderCounter == turnOrder.size()) {
            endRound();
        } else {
            currentTurn = turnOrder.get(turnOrderCounter);
            beginTurn();
        }



    }

    public void endRound() {

        game.doScript("onEndRound(" + currentRound + ")");

        beginRound();

    }

    public WorldMapGameLogic(Main m) {
        game = m;
    }
}
