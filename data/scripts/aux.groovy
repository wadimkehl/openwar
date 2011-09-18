
// this file gives auxilliary functions to the scripts for easy engine interoperability
// you can include this file with every scenarion BUT MAKE SURE that these functions are loaded first!
// Either by naming the file with the lowest lexicographical order or by incorporating these functions into other files


// plays a sound, really. Does not interfere with other sounds playing
// name is the specified refname of the sound file
def playSound(String name)
{
    game.DB.soundNodes.get(name).play()
    
}


// plays a music file. Stops the current running music
// name is the specified refname of the music file
def playMusic(String name)
{
    game.DB.musicNodes.get(name).play()

}


// UI changes to another screen defined in the xml file
// name is the id of the screen
def changeUIScreen(String name)
{
    game.nifty.gotoScreen(name)
}


// shows or hides an UI layer of the current active screen
// name is the id of the element, show is a boolean
def showUILayer(String name,boolean show)
{
    element = game.nifty.getCurrentScreen().findElementByName( "name" )
    if (show) element.show()
    else
    element.hide()
}


// displays a blocking ui popup of the current active screen
// name is the id of the popup template in the xml file
// returns the popup element, needed to close the popup again!
def String showUIPopUp(String name)
{
    element = game.nifty.createPopup(name).getId()
    game.nifty.showPopup(game.nifty.getCurrentScreen(), element, null)
    return element

}

// closes a blocking ui popup that has been displayed with showUIPopUp()
// element is the id of the popup that was returned by showUIPopUp()
def closeUIPopUp(String element)
{
    game.nifty.closePopup(element);
}