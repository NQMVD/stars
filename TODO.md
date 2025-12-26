better api error handeling in client
- when rate limit is hit, backend should tell the frontend
- add messages or status bar of some sort to frontend

CRITICAL: pre checks in the backend for validating the csv file schema before processing it

the backend should listen to something that triggers a re-fetch of the catalogue from the source

add description to app cards

client doesnt reload the cataloge if the request failed or whatever

the library view is too different from what the updates view looks like. make the library look like the updates view, especially in terms of colors.


okay nice. now create some variants of a more specific idea i had that shows the progress of the steps of a download and installation progress with these steps:
1. backend downloads assets
2. transfer assets to client
3. client installs assets
4. client verifies installation

please refactor the sidebar.
i need the account component removed as i dont even plan to have my app have acounts.
instead its space is used for download and installation indicators.
these should be simple but informational, showing the process in steps like:
1. backend downloads assets
2. transfer assets to client
3. client installs assets
4. client verifies installation
heres an image of a design i found an liked, please recreate it but in the style of the appstore.


please go into the client in v0 and make the library actually work, by that i mean be able to pretent to download and install the apps when their install button is clicked, and then they show in the library. the library should be just a json file right now to save the state across sessions. also the apps should be removable from the list and updateable, all just to test the ui and stuff.
