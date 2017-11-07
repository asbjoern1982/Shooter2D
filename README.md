# Shooter2D
This is a small network game, it is not intended to be played but to explore networking with sockets in java and a few fun algorithms.

## Running the game
The main way to run the game is to run the main method in the Client class. It will open a selection window where you can either join a game or create your own server with a number of bots and join that.
If running on a dedicated machine the server can be run by calling the main method in the ServerConsoleMain class. It will write the log to log.txt instead of to the console.

## Protocol
### Joining
- A client opens a connection to the game and just sends the wanted name: **"\<playername\>"**
- if **<playername>** is illegal, for example in use or a reserved command, the server replys: **"rejected"**
- else the server sends the board in a serialized state, a list of serialized players.
- The server then informs all connected clients that a new player has joined: **"joined:\<playername\>"**

### Leaving
- The client closes the connection and the server informs all connected clients: **"left:\<playername\>"**

### Moving
- The client sends: **"move:\<direction\>"**
- If the move is legal, the server informs all connected clients: **"move:\<playername\>:\<x\>:\<y\>:\<direction\>"**

### Shooting
- The client simply sends **"shoot"**
- The server updates the players new points if any players are hit.<br>The server informs all connected clients: **"shot:\<playername\>:\<playerpoints\>"**

## Algorithms
There are a few interesting algorithms used in this game.

### A* algorithm
The AI uses a [A* search algorithm](https://en.wikipedia.org/wiki/A*_search_algorithm) to find a path to the nearest player.

### Map algorithm
The server generates a random map and uses a map algorithm to check that a board has no pockets that a player could be trapped in when joining the game, it continues to generates map till it creates one with no pockets.
pseudo code:
1. get first tile that starts with f
2. color all surrounding tiles with f
3. call this for all surrounding tiles with f
4. at end, check if there are any tiles starting with f that is not colored
