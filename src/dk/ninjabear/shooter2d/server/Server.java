package dk.ninjabear.shooter2d.server;

import dk.ninjabear.shooter2d.game.Board;
import dk.ninjabear.shooter2d.game.Direction;
import dk.ninjabear.shooter2d.game.Player;
import dk.ninjabear.shooter2d.game.Shot;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class Server {
    public static void main(String[] args) {
        new Server("Vegans", 5001, 4);
    }

    private Thread serverThread;
    private Board board;
    private final List<ServerPlayer> players = new ArrayList<>();
    private boolean running;
    private String logfile;

    public Server(String serverName, int port, int playerlimit) {
        this(serverName, port, playerlimit, 20, 20, null);
    }

    public Server(String serverName, int port, int playerlimit, int width, int height, String logfile) {
        board = Board.generateRandomBoard(width, height);
        this.logfile = logfile;

        serverThread = new Thread(() -> {
            try {
                ServerSocket serverSocket = new ServerSocket(port);
                log("Server [" + serverName + "] is running on port: " + port);
                running = true;
                while (running) {
                    Socket socket = serverSocket.accept();
                    BufferedReader inFromClient = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    DataOutputStream outToClient = new DataOutputStream(socket.getOutputStream());

                    String request = inFromClient.readLine();

                    int id = 0;
                    String name = request;
                    if (request.startsWith("ai")) {
                        int numberOfAis = 0;
                        for (ServerPlayer serverPlayer : players)
                            if (serverPlayer.getId() == 0)
                                numberOfAis++;
                        numberOfAis++;
                        name += "" + numberOfAis;
                    } else {
                        boolean rejected = false;
                        int numberOfPlayers = 0;
                        for (ServerPlayer serverPlayer : players)
                            if (serverPlayer.getId() != 0)
                                numberOfPlayers++;
                        if (numberOfPlayers >= playerlimit)
                            rejected = true;
                        if (request == null || request.length() < 1 || request.contains(":") || request.contains(";"))
                            rejected = true;
                        for (ServerPlayer player : players)
                            if (player.getName().equals(request))
                                rejected = true;

                        if (rejected) {
                            log("connection rejected from [" + socket.getInetAddress() + "] name: " + request);
                            outToClient.writeBytes("rejected\n");
                            continue;
                        }

                        int[] ids = new int[4];
                        for (ServerPlayer serverPlayer : players)
                            if (serverPlayer.getId() != 0)
                                ids[serverPlayer.getId()-1]++;
                        for (int i = 0; i < ids.length; i++)
                            if (ids[i] == 0) {
                                id = i + 1;
                                break;
                            }
                    }


                    int x, y;
                    while (true) {
                        x = (int)(Math.random() * board.getWidth());
                        y = (int)(Math.random() * board.getHeight());
                        if (    x < 0 || x > board.getWidth() ||
                                y < 0 || y > board.getHeight() ||
                                board.get(x, y).startsWith("w")) continue;
                        for (ServerPlayer exsistingPlayer : players)
                            if (exsistingPlayer.getX() == x && exsistingPlayer.getY() == y) //TODO this seems not to work
                                continue;
                        break;
                    }

                    int randomNumber = (int) (Math.random() * 4);
                    Direction direction = Direction.UP;
                    if (randomNumber == 1) direction = Direction.RIGHT;
                    if (randomNumber == 2) direction = Direction.DOWN;
                    if (randomNumber == 3) direction = Direction.LEFT;

                    ServerPlayer player = new ServerPlayer(name, id, x, y, direction, outToClient);

                    log("connection accepted from [" + socket.getInetAddress() + "] with name: " + name + " at (" + x + ", " + y + ")");
                    outToClient.writeBytes("accepted:" + serverName + ":" + name + "\n");
                    outToClient.writeBytes(board.serialize() + "\n");

                    if (players.isEmpty()) outToClient.writeBytes("\n");
                    else outToClient.writeBytes(players.stream().map(p -> p.serialize()).collect(Collectors.joining(";")) + "\n");

                    players.add(player);
                    for (ServerPlayer serverPlayer : players)
                        serverPlayer.getOutToClient().writeBytes("joined:" + player.serialize() + "\n");

                    new Thread(() ->{
                        try {
                            while (true) {
                                String input = inFromClient.readLine();
                                if (!handleInFromClient(player, input))
                                    break;
                            }
                        } catch (IOException ioe) {
                            try {
                                handleInFromClient(player, null);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }).start();
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        serverThread.start();
    }

    // returns falls if the player has left
    private synchronized boolean handleInFromClient(Player player, String input) throws IOException {
        if (input == null) {
            log(player.getName() + " left the game");
            players.remove(player);
            for (ServerPlayer serverPlayer : players)
                serverPlayer.getOutToClient().writeBytes("left:" + player.getName() + "\n");
            return false;
        } else if (input.equals("shoot")) {
            List<Shot> shots = new ArrayList<>();
            if (player.getDirection() == Direction.UP)
                for (int y2 = player.getY() - 1; y2 > 0; y2--)
                    if (board.get(player.getX(), y2).startsWith("f"))
                        shots.add(new Shot(player.getX(), y2, player.getDirection()));
                    else break;
            if (player.getDirection() == Direction.RIGHT)
                for (int x2 = player.getX() + 1; x2 > 0; x2++)
                    if (board.get(x2, player.getY()).startsWith("f"))
                        shots.add(new Shot(x2, player.getY(), player.getDirection()));
                    else break;
            if (player.getDirection() == Direction.DOWN)
                for (int y2 = player.getY() + 1; y2 > 0; y2++)
                    if (board.get(player.getX(), y2).startsWith("f"))
                        shots.add(new Shot(player.getX(), y2, player.getDirection()));
                    else break;
            if (player.getDirection() == Direction.LEFT)
                for (int x2 = player.getX() - 1; x2 > 0; x2--)
                    if (board.get(x2, player.getY()).startsWith("f"))
                        shots.add(new Shot(x2, player.getY(), player.getDirection()));
                    else break;

            // add 1 point per hit player
            int points = 0;
            for (Shot shot : shots)
                for (Player p : players)
                    if (p.getX() == shot.getX() && p.getY() == shot.getY())
                        points++;
            player.addPoints(points);

            // tell everyone that the player has shot and how many points he has
            for (ServerPlayer serverPlayer : players)
                serverPlayer.getOutToClient().writeBytes("shot:" + player.getName() + ":" + player.getPoints() + "\n");
            log(player.getName() + " shot");

        } else if (input.startsWith("move")) {
            Direction dir = Direction.toDirection(input.split(":")[1]);
            int newX = player.getX();
            int newY = player.getY();
            if (dir == Direction.UP) newY--;
            if (dir == Direction.RIGHT) newX++;
            if (dir == Direction.DOWN) newY++;
            if (dir == Direction.LEFT) newX--;

            boolean allowed = true;
            if (board.get(newX, newY).startsWith("w"))
                allowed = false;
            for (ServerPlayer serverPlayer : players)
                if (serverPlayer.getX() == newX && serverPlayer.getY() == newY)
                    allowed = false;

            if (allowed) {
                player.setX(newX);
                player.setY(newY);
                player.setDirection(dir);
                for (ServerPlayer serverPlayer : players)
                    serverPlayer.getOutToClient().writeBytes("move:" + player.getName() + ":" + newX + ":" + newY + ":" + Direction.toString(dir) + "\n");
                log(player.getName() + " moved " + Direction.toString(dir));
            }
        }
        return true;
    }

    public synchronized void stop() {
        running = false;
        for (ServerPlayer player : players) {
            try {
                player.getOutToClient().close();
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
        log("Server stopped");
        System.exit(0);
    }

    private synchronized void log(String message) {
        String logentry = new SimpleDateFormat("yyyy-MM-dd HH:mm;ss").format(new Date()) + "> " + message;
        if (logfile == null)
            System.out.println(logentry);
        else {
            try {
                if (!new File(logfile).exists())
                    new File(logfile).createNewFile();
                Files.write(Paths.get(logfile), (logentry + "\n").getBytes(), StandardOpenOption.APPEND);
            } catch (IOException ioe) {ioe.printStackTrace();}
        }
    }
}
