

// called when a building was constructed
// faction, region and building are refnames; level is an integer
def onBuildingBuilt(faction,region,building,level) 
{
    println faction + " built " + building + "(" + level + ") in " + region
}


// called when a faction ends its turn
// faction is refname, turn is integer 
def onEndTurn(faction, turn)
{

}


// called when a faction begins its turn
// faction is refname, turn is integer 
def onBeginTurn(faction,turn)
{
    
}

