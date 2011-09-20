
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
}

// called when a new round begins (i.e. all factions had their turn)
def onBeginRound()
{
    game.DB.currentRound++
    
}

// called when the player clicks any element on the world map gui
// element is the name defined in the ui xml file
// button is which button was pressed: 0=left,1=right,2=middle
def onWorldMapUIClicked(String element, int button)
{
    if(element == "turn")
    {
        playSound("ui_close")
        onEndTurn("player",game.DB.currentRound)  
        onBeginRound()
        return;
    }
    
    if (element == "build")
    {
        playSound("ui_open")
        toggleUIElement("scrolls_layer")

    }
    
}

