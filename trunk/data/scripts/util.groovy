
// this file gives utility functions to the scripts for easy engine interoperability
// you can include this file with every custom campaign 


// plays a sound, really. Does not interfere with other sounds playing
// name is the specified refname of the sound file
def playSound(String name)
{
    n = game.DB.soundNodes.get(name)
    if(n == null)
    {
        println "Cannot find sound: " + name
        return
    }    
    n.play()   
}


// plays a music file. Stops the current running music
// name is the specified refname of the music file
def playMusic(String name)
{
    n = game.DB.musicNodes.get(name)
    if(n == null)
    {
        println "Cannot find music: " + name
        return
    }
    n.play()
}


// UI changes to another screen defined in the xml file
// name is the id of the screen
def changeUIScreen(String name)
{
    game.nifty.gotoScreen(name)
}


// shows or hides an UI element of the current active screen
// name is the id of the element, show is a boolean
def showUIElement(String name,boolean show)
{
    element = game.nifty.getCurrentScreen().findElementByName(name)   
    if(element == null)
    {
        println "Cannot find gui element: " + name
        return
    }  
    element.setVisible(show)
    return
}

// toggles the visibility of an UI element (e.g. a layer or an image)
// name is the id of the element, show is a boolean
def toggleUIElement(String name)
{
    element = game.nifty.getCurrentScreen().findElementByName(name)   
    if(element == null)
    {
        println "Cannot find gui element: " + name
        return
    }  
    element.setVisible(!element.isVisible())
    return
}



// displays a blocking ui popup of the current active screen
// name is the id of the popup template in the xml file
// returns the popup element, needed to close the popup again!
def String showUIPopUp(String name)
{
    element = game.nifty.createPopup(name).getId()
    if(element == null)
    {
        println "Cannot find popup template: " + name
        return
    } 
    game.nifty.showPopup(game.nifty.getCurrentScreen(), element, null)
    return element

}

// closes a blocking ui popup that has been displayed with showUIPopUp()
// element is the id of the popup that was returned by showUIPopUp()
def closeUIPopUp(String element)
{    if(element == null)
    {
        println "Cannot find popup with id: " + element
        return
    } 
    game.nifty.closePopup(element);
}