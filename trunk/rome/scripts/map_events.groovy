
// this file holds all world map event functions that are called during the game


// this function is called once at the beginning of the game
def onBeginGame()
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
// faction and region are refnames, unit is a refname
def onUnitRecruited(String faction, String region, String unit)
{
    println faction + " recruited " + unit + " in " + region
}


// called when a faction ends its turn
// faction is refname
def onEndTurn(String faction)
{
    println faction + " ended turn "
}


// called when a faction begins its turn
// faction is refname 
def onBeginTurn(String faction)
{
    println faction + " began turn"

    
}

// called when a new round begins (i.e. all factions had their turn)
// round is current round number 
def onBeginRound(round)
{        
        println "Round began"

   
    
}

// called when a round ends (i.e. all factions had their turn)
// round is current round number 
def onEndRound(round)
{        
    
    println "Round ended"
   
    
}

// called when the player clicks any element on the world map gui
// element is the name defined in the ui xml file
// button is which button was pressed: 0=left,1=right,2=middle
def onWorldMapUIClicked(String element, int button)
{
    if(element == "turn")
    {
        game.worldMapState.logic.endTurn()
        return;
    }
    
    if (element == "build")
    {
        playSound("ui_window_open")
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
