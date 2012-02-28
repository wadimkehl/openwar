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

        Faction f = Main.DB.factions.get(Main.DB.currentTurn);

        if (Main.DB.currentTurn.equals(Main.DB.playerFaction)) {
            game.worldMapState.uiController.deselectAll();

            game.playSound("turn_end");



        }


        game.doScript("onEndTurn('" + Main.DB.currentTurn + "')");
        
        boolean next=false;
        for(String s : Main.DB.factions.keySet())
        {

            if(next)
            {
                if(Main.DB.playerFaction.equals(s))
                next = false;
                else
                {
                    Main.DB.currentTurn = s;
                    break;
                }
            }
                       
            if(s.equals(Main.DB.currentTurn))
            {
                next=true;
            }
        }
        
        if (next) {
            beginTurn();          
        } else {
            endRound();
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
