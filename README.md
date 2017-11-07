# Shooter2D
This is a small network game, it is not intended to be played but to explore networking with sockets in java and a few fun algorithms.

## Running the game
The main way to run the game is to run the main method in the Client class. It will open a selection window where you can either join a game or create your own server with a number of bots and join that.
If running on a dedicated machine the server can be run by calling the main method in the ServerConsoleMain class. It will write the log to log.txt instead of to the console.

## Protocol

## Algorithms
There are a few interesting algorithms used in this game.

### A* algorithm
The AI uses A* algorithm to find the nearest player.

### Map algorithm
It uses a map algorithm to check that a board has no pockets that a player could be trapped in when joining the game.
pseudo code:
1. get first tile that starts with f
2. color all surrounding tiles with f
3. call this for all surrounding tiles with f
4. at end, check if there are any tiles starting with f that is not colored
