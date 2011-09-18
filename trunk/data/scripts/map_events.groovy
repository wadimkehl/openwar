

// called when a building was constructed
// faction, region and building are refnames; level is an integer
def onBuildingBuilt(faction,region,building,level) 
{
    println faction + " built " + building + "(" + level + ") in " + region
}

// called when a unit was recruited
// faction and region are refnames, unit is a pointer to the unit
def onUnitRecruited(faction, region, unit)
{
    println faction + " recruited " + unit.refName + " in " + region
}


// called when a faction ends its turn
// faction is refname, turn is integer 
def onEndTurn(faction, turn)
{
    println faction + " ended turn " + turn
}


// called when a faction begins its turn
// faction is refname, turn is integer 
def onBeginTurn(faction,turn)
{
    println faction + " began turn " + turn
}


// called when the player clicks anything on the world map gui
// element is the name defined in the ui xml file
def onWorldMapUIClicked(element)
{
    
}

