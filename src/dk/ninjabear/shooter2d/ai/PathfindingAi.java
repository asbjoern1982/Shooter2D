package dk.ninjabear.shooter2d.ai;


import dk.ninjabear.shooter2d.game.Board;
import dk.ninjabear.shooter2d.game.Direction;
import dk.ninjabear.shooter2d.game.Player;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.*;

public class PathfindingAi {
    public static void main(String[] args) {new PathfindingAi("localhost", 5001);}

    private final List<Player> players = new ArrayList<>();
    private String myName;
    private Board board;
    private DataOutputStream outToServer;

    private boolean connected = true;

    public PathfindingAi(String address, int port) {
        try {
            Socket socket = new Socket(address, port);
            outToServer = new DataOutputStream(socket.getOutputStream());
            BufferedReader inFromServer = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            outToServer.writeBytes("ai\n");
            String response = inFromServer.readLine();
            if (response.equals("rejected")) {System.out.println("connection rejected"); return;}

            myName = response.split(":")[2];

            board = Board.deserialize(inFromServer.readLine());
            String playersString = inFromServer.readLine();
            if (!playersString.isEmpty()) {
                String[] playersTokens = playersString.split(";");
                for (String token : playersTokens)
                    synchronized (this) {
                        Player player = Player.deserialize(token);
                        players.add(player);
                    }
            }

            new Thread(() -> {
                while(connected) {
                    try {
                        String message = inFromServer.readLine();
                        if (message == null) break;
                        handleAction(message);
                    } catch (IOException ioe) {break;}
                }
            }).start();

            new Thread(() -> {
                try {
                    while(true) {
                        takeTurn();
                        Thread.sleep(500);
                    }
                } catch (InterruptedException ie) {ie.printStackTrace();}
            }).start();


        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    private synchronized void takeTurn() {
        try {
            Player me = null;
            for (Player player : players)
                if (player.getName().equals(myName))
                    me = player;

            if (me == null) return; // might not have received a full player list yet

            // if a player is in front of the ai, shoot at it.. maybe
            if (Math.random() > 0.1) // to break situations where two bots just shoot at each other
                for (Player player : players) {
                    if (me.getDirection() == Direction.UP
                            && player.getX() == me.getX()
                            && player.getY() < me.getY()) {
                        boolean clearShot = true;
                        for (int y = me.getY(); y > player.getY(); y--) {
                            if (board.get(me.getX(), y).startsWith("w"))
                                clearShot = false;
                        }
                        if (clearShot) {
                            outToServer.writeBytes("shoot\n");
                            return;
                        }
                    } else if (me.getDirection() == Direction.RIGHT
                            && player.getX() > me.getX()
                            && player.getY() == me.getY()) {
                        boolean clearShot = true;
                        for (int x = me.getX(); x < player.getX(); x++) {
                            if (board.get(x, me.getY()).startsWith("w"))
                                clearShot = false;
                        }
                        if (clearShot) {
                            outToServer.writeBytes("shoot\n");
                            return;
                        }
                    } else if (me.getDirection() == Direction.DOWN
                            && player.getX() == me.getX()
                            && player.getY() > me.getY()) {
                        boolean clearShot = true;
                        for (int y = me.getY(); y < player.getY(); y++) {
                            if (board.get(me.getX(), y).startsWith("w"))
                                clearShot = false;
                        }
                        if (clearShot) {
                            outToServer.writeBytes("shoot\n");
                            return;
                        }
                    } else if (me.getDirection() == Direction.LEFT
                            && player.getX() < me.getX()
                            && player.getY() == me.getY()) {
                        boolean clearShot = true;
                        for (int x = me.getX(); x > player.getX(); x--) {
                            if (board.get(x, me.getY()).startsWith("w"))
                                clearShot = false;
                        }
                        if (clearShot) {
                            outToServer.writeBytes("shoot\n");
                            return;
                        }
                    }
                }

            // else find nearest enemy
            Player nearestPlayer = null;
            double nearestDistance = Double.MAX_VALUE;
            for (Player player : players) {
                double x1 = me.getX();
                double y1 = me.getY();
                double x2 = player.getX();
                double y2 = player.getY();
                double distance = Math.sqrt((x2-x1)*(x2-x1) + (y2-y1)*(y2-y1));


                if (distance == 0)
                    continue; // this is me

                if (distance < nearestDistance) {
                    nearestPlayer = player;
                    nearestDistance = distance;
                }
            }



            if (nearestPlayer != null && Math.random() > 0.1) {
                Direction[] path = a_star(me.getX(), me.getY(), nearestPlayer.getX(), nearestPlayer.getY());
                move(me, path[0]);
            } else {
                // walk in a random direction
                List<Integer> randoms = new ArrayList<>();
                int[] indexes = {0,1,2,3};
                for (int index : indexes)
                    randoms.add(index);
                Collections.shuffle(randoms);

                for (int index : randoms)
                    if (moveWithCheck(me, index))
                        break;
            }
        } catch (IOException ioe) {connected = false;}
    }

    private Direction[] a_star(int myX, int myY, int targetX, int targetY) {
        // -------------------------------- setup -------------------------------- \\
        // The set of nodes already evaluated
        List<int[]> closedSet = new ArrayList<>();

        // The set of currently discovered nodes that are not evaluated yet.
        // Initially, only the start node is known.
        List<int[]> openSet = new ArrayList<>();
        int[] startNode = {myX, myY};
        openSet.add(startNode);

        // For each node, which node it can most efficiently be reached from.
        // If a node can be reached from many nodes, cameFrom will eventually contain the
        // most efficient previous step.
        Map<int[], int[]> cameFrom = new HashMap<>();

        // For each node, the cost of getting from the start node to that node.
        double[][] gScore = new double[board.getWidth()][board.getHeight()];
        for (int x = 0; x < board.getWidth(); x++)
            for (int y = 0; y < board.getHeight(); y++)
                gScore[x][y] = Double.MAX_VALUE;

        // The cost of going from start to start is zero.
        gScore[myX][myY] = 0;

        // For each node, the total cost of getting from the start node to the goal
        // by passing by that node. That value is partly known, partly heuristic.
        double[][] fScore = new double[board.getWidth()][board.getHeight()];
        for (int x = 0; x < board.getWidth(); x++)
            for (int y = 0; y < board.getHeight(); y++)
                fScore[x][y] = Double.MAX_VALUE;

        // For the first node, that value is completely heuristic.
        fScore[myX][myY] = dist_between(myX, myY, targetX, targetY);


        // -------------------------------- algorithm -------------------------------- \\
        int[] current = startNode;
        while (!openSet.isEmpty()) {
            // set current to the node in openSet having the lowest fScore[] value
            double lowest_fScore = Double.MAX_VALUE;
            for (int[] node : openSet) {
                if (fScore[node[0]][node[1]] < lowest_fScore) {
                    lowest_fScore = fScore[node[0]][node[1]];
                    current = node;
                }
            }

            if (current[0] == targetX && current[1] == targetY)
                return getPathWithDirection(reconstruct_path(cameFrom, current));

            for (int[] node : openSet) {
                if (node[0] == current[0] && node[1] == current[1]) {
                    openSet.remove(node);
                    break;
                }
            }
            closedSet.add(current);

            outer : for (int[] neighbor : neighborsOf(current)) {
                // Ignore the neighbor which is already evaluated.
                for (int[] node : closedSet)
                    if (neighbor[0] == node[0] && neighbor[1] == node[1])
                        continue outer;

                // Discover a new node
                boolean isInOpenSet = false;
                for (int[] node : openSet)
                    if (neighbor[0] == node[0] && neighbor[1] == node[1])
                        isInOpenSet = true;
                if (isInOpenSet == false)
                    openSet.add(neighbor);

                // The distance from start to a neighbor
                double tentative_gScore = gScore[current[0]][current[1]] + dist_between(current[0], current[1], neighbor[0], neighbor[1]);
                if (tentative_gScore > gScore[neighbor[0]][neighbor[1]])
                    continue;

                // This path is the best until now. Record it!
                cameFrom.put(neighbor, current);
                gScore[neighbor[0]][neighbor[1]] = tentative_gScore;
                fScore[neighbor[0]][neighbor[1]] = gScore[neighbor[0]][neighbor[1]] + dist_between(neighbor[0], neighbor[1], targetX, targetY);
            }
        }
        System.out.println("ERROR: no path found");
        return null;
    }

    private static double dist_between(int x1, int y1, int x2, int y2) {
        return Math.sqrt((x2-x1)*(x2-x1) + (y2-y1)*(y2-y1));
    }

    private List<int[]> neighborsOf(int[] current) {
        List<int[]> neighbors = new ArrayList<>();
        if (board.get(current[0], current[1] - 1).startsWith("f")) {
            int[] neighbor = {current[0], current[1] - 1};
            neighbors.add(neighbor);
        }
        if (board.get(current[0] - 1, current[1]).startsWith("f")) {
            int[] neighbor = {current[0] - 1, current[1]};
            neighbors.add(neighbor);
        }
        if (board.get(current[0], current[1] + 1).startsWith("f")) {
            int[] neighbor = {current[0], current[1] + 1};
            neighbors.add(neighbor);
        }
        if (board.get(current[0] + 1, current[1]).startsWith("f")) {
            int[] neighbor = {current[0] + 1, current[1]};
            neighbors.add(neighbor);
        }

        return neighbors;
    }

    private static List<int[]> reconstruct_path(Map<int[], int[]> cameFrom, int[] current) {
        List<int[]> total_path = new ArrayList<>();
        total_path.add(current);

        while (cameFrom.containsKey(current)) {
            int[] temp_current = cameFrom.get(current);
            cameFrom.remove(current);
            current = temp_current;
            total_path.add(current);
        }

        return total_path;
    }

    private static Direction[] getPathWithDirection(List<int[]> path) {
        Collections.reverse(path);

        List<Direction> directions = new ArrayList<>();

        int[] lastNode = path.get(0);
        for (int i = 1; i < path.size(); i++) {
            int[] current = path.get(i);
            if (lastNode[1] < current[1])
                directions.add(Direction.DOWN);
            else if (lastNode[1] > current[1])
                directions.add(Direction.UP);
            else if (lastNode[0] > current[0])
                directions.add(Direction.LEFT);
            else if (lastNode[0] < current[0])
                directions.add(Direction.RIGHT);
            lastNode = current;
        }
        return directions.toArray(new Direction[0]);
    }

    private boolean move(Player me, Direction direction) throws IOException {
        outToServer.writeBytes("moveWithCheck:" + Direction.toString(direction) + "\n");
        return true;
    }

    private boolean moveWithCheck(Player me, int dir) throws IOException {
        if (dir == 0 && board.get(me.getX(), me.getY() - 1).startsWith("f")) {
            outToServer.writeBytes("moveWithCheck:" + Direction.toString(Direction.UP) + "\n"); return true;
        } else if (dir == 1 && board.get(me.getX() + 1, me.getY()).startsWith("f")) {
            outToServer.writeBytes("moveWithCheck:" + Direction.toString(Direction.RIGHT) + "\n"); return true;
        } else if (dir == 2 && board.get(me.getX(), me.getY() + 1).startsWith("f")) {
            outToServer.writeBytes("moveWithCheck:" + Direction.toString(Direction.DOWN) + "\n"); return true;
        } else if (dir == 3 && board.get(me.getX() - 1, me.getY()).startsWith("f")) {
            outToServer.writeBytes("moveWithCheck:" + Direction.toString(Direction.LEFT) + "\n"); return true;
        }
        return false;
    }

    private synchronized void handleAction(String message) {
        String[] tokens = message.split(":");
        String action = tokens[0];
        String name = tokens[1];
        Player player = null;
        for (Player currentplayer : players)
            if (currentplayer.getName().equals(name))
                player = currentplayer;

        if (action.equals("move")) {
            int newX = Integer.parseInt(tokens[2]);
            int newY = Integer.parseInt(tokens[3]);
            Direction direction = Direction.toDirection(tokens[4]);
            player.setX(newX);
            player.setY(newY);
            player.setDirection(direction);
        } else if (action.equals("joined")) {
            String serializedPlayer = message.substring(action.length() + 1);
            Player newPlayer = Player.deserialize(serializedPlayer);
            players.add(newPlayer);
        } else if (action.equals("left")) {
            players.remove(player);
        }
    }
}
