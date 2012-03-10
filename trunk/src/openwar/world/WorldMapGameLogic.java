/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package openwar.world;

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

    public void beginGame() {
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
        Main.DB.currentRound++;

        for (Faction f : Main.DB.factions.values()) {
            for (Army a : f.armies) {
                a.resetMovePoints();
            }
        }

        for (Settlement s : Main.DB.settlements.values()) {
            s.newRound();
            s.resetMovePoints();

        }

        Main.DB.currentTurn = Main.DB.playerFaction;
        game.doScript("onBeginRound(" + Main.DB.currentRound + ")");

        beginTurn();
    }

    public void beginTurn() {
        Faction f = Main.DB.factions.get(Main.DB.currentTurn);
        game.doScript("onBeginTurn('" + Main.DB.currentTurn + "')");



        if (Main.DB.currentTurn.equals(Main.DB.playerFaction)) {
            if (game.worldMapState.map.selectedArmy != null) {
                game.worldMapState.map.selectArmy(game.worldMapState.map.selectedArmy);
            } else if (game.worldMapState.map.selectedSettlement != null) {
                game.worldMapState.uiController.selectSettlement(game.worldMapState.map.selectedSettlement);
                game.worldMapState.uiController.drawReachableArea();
            }


        } else {

            System.err.println("Calculating influence map for " + f.refName);
            ai.calculateInfluenceMap(f);
            endTurn();
        }



    }

    public void endTurn() {

        Faction f = Main.DB.factions.get(Main.DB.currentTurn);
        game.doScript("onEndTurn('" + Main.DB.currentTurn + "')");



        if (Main.DB.currentTurn.equals(Main.DB.playerFaction)) {
            game.worldMapState.uiController.deselectAll();

            game.playSound("turn_end");



        }


        // Check if we finish this round
        Set factions = Main.DB.factions.keySet();
        for (int i = 0; i < factions.size(); i++) {
            String s = (String) factions.toArray()[i];
            if (s.equals(Main.DB.currentTurn)) {
                if (i + 1 == factions.size()) {
                    endRound();
                } else {
                    Main.DB.currentTurn = (String) factions.toArray()[i + 1];
                    beginTurn();
                }
            }
        }

    }

    public void endRound() {

        game.doScript("onEndRound(" + Main.DB.currentRound + ")");

        beginRound();

    }

    public WorldMapGameLogic(Main m) {
        game = m;
        ai = new WorldAI(this);
    }
}
