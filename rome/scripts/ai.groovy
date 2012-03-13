


// this function is called at the beginning of each turn of every AI controlled faction
// faction is refname
def doAI(String faction)
{
    
    switch(faction)
    {
        
        case "greeks":
        doAIgreeks()
        break
        
        case "rebels":
        doAIrebels()
        break          
        
    }
       

}


def doAIgreeks()
{
    
    f = game.DB.factions.get("greeks")
    println f.gold
    
    
    
}


def doAIrebels()
{
    
    f = game.DB.factions.get("rebels")
    println f.gold
    
    
}