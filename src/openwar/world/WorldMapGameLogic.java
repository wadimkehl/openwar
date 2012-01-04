/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package openwar.world;

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

    public void beginGame() {
        for (Faction f : Main.DB.factions) {
            for (Army a : f.armies) {
                a.resetMovePoints();
            }
        }

        for (Settlement s : Main.DB.settlements) {
            s.resetMovePoints();

        }

        Main.DB.currentTurn = Main.DB.factions.get(0).refName;
        game.doScript("onBeginGame()");

        beginTurn();
    }

    public void beginRound() {
        Main.DB.currentRound++;

        for (Faction f : Main.DB.factions) {
            for (Army a : f.armies) {
                a.resetMovePoints();
            }
        }

        for (Settlement s : Main.DB.settlements) {
            s.newRound();
            s.resetMovePoints();

        }

        Main.DB.currentTurn = Main.DB.factions.get(0).refName;
        game.doScript("onBeginRound(" + Main.DB.currentRound + ")");

        beginTurn();
    }

    public void beginTurn() {
        Faction f = Main.DB.hashedFactions.get(Main.DB.currentTurn);


        if (Main.DB.currentTurn.equals(Main.DB.playerFaction)) {


            if (game.worldMapState.map.selectedArmy != null) {
                game.worldMapState.map.selectArmy(game.worldMapState.map.selectedArmy);
            } else if (game.worldMapState.map.selectedSettlement != null) {
                game.worldMapState.uiController.selectSettlement(game.worldMapState.map.selectedSettlement);
                game.worldMapState.uiController.drawReachableArea();
            }
        }


        game.doScript("onBeginTurn('" + Main.DB.currentTurn + "')");


        if (!Main.DB.currentTurn.equals(Main.DB.playerFaction)) {
            endTurn();
        }

    }

    public void endTurn() {

        Faction f = Main.DB.hashedFactions.get(Main.DB.currentTurn);

        if (Main.DB.currentTurn.equals(Main.DB.playerFaction)) {
            game.worldMapState.uiController.deselectAll();

            game.playSound("turn_end");



        }


        game.doScript("onEndTurn('" + Main.DB.currentTurn + "')");
        int next = Main.DB.factions.indexOf(f) + 1;
        if (next == Main.DB.factions.size()) {
            endRound();
        } else {
            Main.DB.currentTurn = Main.DB.factions.get(next).refName;
            beginTurn();
        }

    }

    public void endRound() {

        game.doScript("onEndRound(" + Main.DB.currentRound + ")");

        beginRound();

    }

    public WorldMapGameLogic(Main m) {
        game = m;
    }
}
