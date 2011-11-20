
// this file holds all world map event functions that are called during the game


// this function is called once at the beginning of the game
def onGameBegin()
{
    println "Game begins"
    
}


// called when a building was constructed
// faction, region and building are refnames; level is the new level of the building
def onBuildingBuilt(String faction,String region,String building,int level) 
{
    println faction + " built " + building + "(" + level + ") in " + region
}

// called when a unit was recruited
// faction and region are refnames, unit is a pointer to the unit
def onUnitRecruited(String faction, String region, unit)
{
    println faction + " recruited " + unit.refName + " in " + region
}


// called when a faction ends its turn
// faction is refname, round is current round number 
def onEndTurn(String faction, int round)
{
    println faction + " ended round " + round
}


// called when a faction begins its turn
// faction is refname, round is current round number 
def onBeginTurn(String faction,int round)
{
    println faction + " began round " + round
    
    if(game.worldMapState.map.selectedArmy != null)
    {
        game.worldMapState.map.selectArmy(game.worldMapState.map.selectedArmy);
    }
    else if (game.worldMapState.map.selectedSettlement != null) {
        game.worldMapState.uiController.drawReachableArea()
    }
    
}

// called when a new round begins (i.e. all factions had their turn)
def onBeginRound()
{        
    
    playSound("turn_begin")

    game.DB.currentRound++

    for(f in game.DB.factions)
    {
        for(a in f.armies)
        {
            a.resetMovePoints()
        }
    }
    
    for(s in game.DB.settlements)
    {
        s.resetMovePoints()
        
    }
    
    onBeginTurn("lol",    game.DB.currentRound)
    
}

// called when the player clicks any element on the world map gui
// element is the name defined in the ui xml file
// button is which button was pressed: 0=left,1=right,2=middle
def onWorldMapUIClicked(String element, int button)
{
    if(element == "turn")
    {
        onEndTurn("player",game.DB.currentRound)  
        onBeginRound()
        
        return;
    }
    
    if (element == "build")
    {
        //playSound("ui_window_open")
        //toggleUIElement("settlement_layer")
        s = game.worldMapState.map.selectedSettlement
        if(s != null)
        {
          game.worldMapState.uiController.switchToUnitsLayer(s.units)

        }

    }
    
    if (element == "recruit")
    {
        s = game.worldMapState.map.selectedSettlement
        if(s != null)
        {
          game.worldMapState.uiController.switchToBuildingsLayer(s.buildings)

        }
        //toggleUIElement("front_unit_layer")
        //toggleUIElement("front_building_layer")

    }
    
    
    
}

def onSettlementSelected()
{
}

def onUnitSelected()
{

}

def onArmySelected()
{

}
