
// this file gives utility functions to the scripts for easier engine interoperability
// you can include this file with every custom campaign 


// plays a sound, really. Does not interfere with other sounds playing
// name is the specified refname of the sound file
def playSound(String name)
{
    game.playSound(name)  
}


// plays a music file. Stops the current running music
// name is the specified refname of the music file
def playMusic(String name)
{
    game.playMusic(name)  
}


// UI changes to another screen defined in the xml file
// name is the id of the screen
def changeUIScreen(String name)
{
    game.changeUIScreen(name)
}


// shows or hides an UI element of the current active screen
// name is the id of the element, show is a boolean
def showUIElement(String name,boolean show)
{
    game.showUIElement(name,show)
}

// toggles the visibility of an UI element (e.g. a layer or an image)
// name is the id of the element, show is a boolean
def toggleUIElement(String name)
{
    game.toggleUIElement(name)
}


// displays a blocking ui popup of the current active screen
// name is the id of the popup template in the xml file
def showUIPopUp(String name)
{
    showUIPopUp(name)
}

// closes a blocking ui popup that has been displayed with showUIPopUp()
// name is the id of the popup template in the xml file
def closeUIPopUp(String name)
{   
    closeUIPopUp(name)
}