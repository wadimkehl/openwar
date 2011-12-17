


// this function tells whether a building has met all prerequisites to be built
// faction, region and building are refnames, level is the queried building level
def canBeBuilt(String faction,String region,String building,int level)
{
    
    switch(building)
    {
        
        case "barracks":
            if (faction != "humans") return false;
        break
        
        
        
    }
    
    return true;
}


// this function returns a list of units that can be recruited for that building in that region
// faction, region and building are refnames, level is the building level
def canBeRecruited(String faction,String region,String building,int level)
{
    def units = []
    
    
    
    return units
}