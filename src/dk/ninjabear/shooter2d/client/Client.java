package dk.ninjabear.shooter2d.client;

import dk.ninjabear.shooter2d.ai.PathfindingAi;
import dk.ninjabear.shooter2d.game.Board;
import dk.ninjabear.shooter2d.game.Direction;
import dk.ninjabear.shooter2d.game.Player;
import dk.ninjabear.shooter2d.game.Shot;
import dk.ninjabear.shooter2d.server.Server;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

// todo shooting too fast, keydown instead? no rapid fire
// todo on serverside, save time of last command and ignore it if there are less than 500 ms between
// todo if the neareste player is just beside this, go in a random direction
// todo readme with the protocol, upload to github

public class Client extends Application {
    public static void main(String[] args) {launch(args);}

    private Image wall01;
    private Image wall02;
    private Image wall03;
    private Image wall04;
    private Image floor01;
    private Image floor02;
    private Image floor03;
    private Image floor04;
    private Image shotVertical01;
    private Image shotVertical02;
    private Image shotVertical03;
    private Image shotVertical04;
    private Image shotHorizontal01;
    private Image shotHorizontal02;
    private Image shotHorizontal03;
    private Image shotHorizontal04;

    private Image aiUp;
    private Image aiRight;
    private Image aiDown;
    private Image aiLeft;
    private Image player01Up;
    private Image player01Right;
    private Image player01Down;
    private Image player01Left;
    private Image player02Up;
    private Image player02Right;
    private Image player02Down;
    private Image player02Left;
    private Image player03Up;
    private Image player03Right;
    private Image player03Down;
    private Image player03Left;
    private Image player04Up;
    private Image player04Right;
    private Image player04Down;
    private Image player04Left;

    private List<Image> images = new ArrayList<>();


    private final List<Player> players = new ArrayList<>();
    private Board board;
    private GridPane gamePane = new GridPane();
    private GridPane scorePane = new GridPane();
    private final List<Shot> firedShots = new ArrayList<>();
    private int image_size;
    private Stage primaryStage;
    private Stage gameStage;

    private Server server;

    @Override
    public void start(Stage primaryStage) throws Exception {
        this.primaryStage = primaryStage;

        image_size = 40;
        wall01 = new Image("file:img/wall01.png", image_size, image_size, true, false);
        wall02 = new Image("file:img/wall02.png", image_size, image_size, true, false);
        wall03 = new Image("file:img/wall03.png", image_size, image_size, true, false);
        wall04 = new Image("file:img/wall04.png", image_size, image_size, true, false);
        floor01 = new Image("file:img/floor01.png", image_size, image_size, true, false);
        floor02 = new Image("file:img/floor02.png", image_size, image_size, true, false);
        floor03 = new Image("file:img/floor03.png", image_size, image_size, true, false);
        floor04 = new Image("file:img/floor04.png", image_size, image_size, true, false);
        shotVertical01 = new Image("file:img/shotVertical01.png", image_size, image_size, true, false);
        shotVertical02 = new Image("file:img/shotVertical02.png", image_size, image_size, true, false);
        shotVertical03 = new Image("file:img/shotVertical03.png", image_size, image_size, true, false);
        shotVertical04 = new Image("file:img/shotVertical04.png", image_size, image_size, true, false);
        shotHorizontal01 = new Image("file:img/shotHorizontal01.png", image_size, image_size, true, false);
        shotHorizontal02 = new Image("file:img/shotHorizontal02.png", image_size, image_size, true, false);
        shotHorizontal03 = new Image("file:img/shotHorizontal03.png", image_size, image_size, true, false);
        shotHorizontal04 = new Image("file:img/shotHorizontal04.png", image_size, image_size, true, false);

        aiUp    = new Image("file:img/aiUp.png", image_size, image_size, true, false);
        aiRight = new Image("file:img/aiRight.png", image_size, image_size, true, false);
        aiDown  = new Image("file:img/aiDown.png", image_size, image_size, true, false);
        aiLeft  = new Image("file:img/aiLeft.png", image_size, image_size, true, false);
        player01Up    = new Image("file:img/player01Up.png", image_size, image_size, true, false);
        player01Right = new Image("file:img/player01Right.png", image_size, image_size, true, false);
        player01Down  = new Image("file:img/player01Down.png", image_size, image_size, true, false);
        player01Left  = new Image("file:img/player01Left.png", image_size, image_size, true, false);
        player02Up    = new Image("file:img/player02Up.png", image_size, image_size, true, false);
        player02Right = new Image("file:img/player02Right.png", image_size, image_size, true, false);
        player02Down  = new Image("file:img/player02Down.png", image_size, image_size, true, false);
        player02Left  = new Image("file:img/player02Left.png", image_size, image_size, true, false);
        player03Up    = new Image("file:img/player03Up.png", image_size, image_size, true, false);
        player03Right = new Image("file:img/player03Right.png", image_size, image_size, true, false);
        player03Down  = new Image("file:img/player03Down.png", image_size, image_size, true, false);
        player03Left  = new Image("file:img/player03Left.png", image_size, image_size, true, false);
        player04Up    = new Image("file:img/player04Up.png", image_size, image_size, true, false);
        player04Right = new Image("file:img/player04Right.png", image_size, image_size, true, false);
        player04Down  = new Image("file:img/player04Down.png", image_size, image_size, true, false);
        player04Left  = new Image("file:img/player04Left.png", image_size, image_size, true, false);

        Image[] imageArray = {
                wall01, wall02, wall03, wall04,
                floor01, floor02, floor03, floor04,
                shotVertical01, shotVertical02, shotVertical03, shotVertical04,
                shotHorizontal01, shotHorizontal02, shotHorizontal03, shotHorizontal04,
                aiUp, aiRight, aiDown, aiLeft,
                player01Up, player01Right, player01Down, player01Left,
                player02Up, player02Right, player02Down, player02Left,
                player03Up, player03Right, player03Down, player03Left,
                player04Up, player04Right, player04Down, player04Left
        };

        for (Image image : imageArray)
            images.add(image);

        String addressHistoryFile = "addressHistory.txt";
        List<String> addressHistory = new ArrayList<>();
        try {
            if (!new File(addressHistoryFile).exists())
                new File(addressHistoryFile).createNewFile();
            else
                addressHistory.addAll(Files.readAllLines(Paths.get(addressHistoryFile)));
        } catch (IOException ioe) {ioe.printStackTrace();}

        primaryStage.setTitle("Shooter2D");
        GridPane joinRoot = new GridPane();
        joinRoot.setPadding(new Insets(10));
        joinRoot.setVgap(5);
        joinRoot.setHgap(5);
        int row = 0;
        joinRoot.add(new Label("Playername:"), 0, row);
        TextField joinPlayername = new TextField("Player1");
        joinRoot.add(joinPlayername, 1, row);
        row++;
        joinRoot.add(new Label("Port:"), 0, row);
        TextField joinPort = new TextField("5001");
        joinRoot.add(joinPort, 1, row);
        row++;
        joinRoot.add(new Label("Address:"), 0, row);

        ComboBox<String> joinAddress = new ComboBox<>();
        joinAddress.getItems().addAll(addressHistory);
        joinAddress.setEditable(true);

        joinRoot.add(joinAddress, 1, row);
        row++;
        Button joinButton = new Button("join");
        joinButton.setDefaultButton(true);
        GridPane.setHalignment(joinButton, HPos.RIGHT);
        joinRoot.add(joinButton, 0, row, 2, 1);
        joinButton.setOnAction(e -> {
            String name = joinPlayername.getText();
            if (name.startsWith("ai")) {
                System.out.println("Name cannot start with \"ai\"");
                return;
            }
            String address  = joinAddress.getSelectionModel().getSelectedItem();
            String portStr = joinPort.getText();
            if (!portStr.matches("[0-9]+")) {
                System.out.println("Port has to be a number");
                return;
            }
            int port = Integer.parseInt(portStr);

            try {
                if (!addressHistory.contains(address))
                    Files.write(Paths.get(addressHistoryFile), (address + "\n").getBytes(), StandardOpenOption.APPEND);
            } catch (IOException ioe) {ioe.printStackTrace();}

            joinServer(name, address, port);
        });

        row++;
        joinRoot.add(new Label("Servername:"), 0, row);
        TextField serverNameText = new TextField("Server1");
        joinRoot.add(serverNameText, 1, row);
        row++;
        joinRoot.add(new Label("Number of Bots:"), 0, row);
        TextField serverBots = new TextField("2");
        joinRoot.add(serverBots, 1, row);
        row++;

        joinRoot.add(new Label("Width: "), 0, row);
        TextField serverWidth = new TextField("20");
        joinRoot.add(serverWidth, 1, row);
        row++;

        joinRoot.add(new Label("Height: "), 0, row);
        TextField serverHeight = new TextField("20");
        joinRoot.add(serverHeight, 1, row);
        row++;

        Button serverButton = new Button("create and join");
        serverButton.setDefaultButton(false);
        GridPane.setHalignment(serverButton, HPos.RIGHT);
        joinRoot.add(serverButton, 0, row, 2, 1);
        serverButton.setOnAction(e -> {
            String name = joinPlayername.getText();
            if (name.startsWith("ai")) {
                System.out.println("Name cannot start with \"ai\"");
                return;
            }
            String portStr = joinPort.getText();
            if (!portStr.matches("[0-9]+")) {
                System.out.println("Port has to be a number");
                return;
            }
            int port = Integer.parseInt(portStr);

            String botSrt = serverBots.getText();
            if (!portStr.matches("[0-9]+")) {
                System.out.println("Bots has to be a number");
                return;
            }
            int numberOfBots = Integer.parseInt(botSrt);

            String widthSrt = serverWidth.getText();
            if (!portStr.matches("[0-9]+")) {
                System.out.println("width has to be a number");
                return;
            }
            int boardWidth = Integer.parseInt(widthSrt);

            String heightSrt = serverHeight.getText();
            if (!portStr.matches("[0-9]+")) {
                System.out.println("height has to be a number");
                return;
            }
            int boardHeight = Integer.parseInt(heightSrt);

            server = new Server(serverNameText.getText(), port, 4, boardWidth, boardHeight, null);
            for (int i = 0; i < numberOfBots; i++)
                new PathfindingAi("localhost", port);

            joinServer(name, "localhost", port);
        });


        primaryStage.setScene(new Scene(joinRoot));
        primaryStage.show();

        joinPlayername.requestFocus();
    }

    private synchronized void joinServer(String name, String address, int port) {
        try {
            if (name.equals("ai") || name.contains(":") || name.contains(";")) return;
            Socket socket = new Socket(address, port);
            DataOutputStream outToServer = new DataOutputStream(socket.getOutputStream());
            BufferedReader inFromServer = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            outToServer.writeBytes(name + "\n");
            String response = inFromServer.readLine();
            if (response.equals("rejected")) {System.out.println("connection rejected"); return;}
            primaryStage.close();

            String serverName = response.matches(".*:.+") ? response.split(":")[1] : "default game";
            board = Board.deserialize(inFromServer.readLine());
            String playersString = inFromServer.readLine();
            if (!playersString.isEmpty()) {
                String[] playersTokens = playersString.split(";");
                for (String token : playersTokens)
                    synchronized (this) {
                        players.add(Player.deserialize(token));
                    }
            }

            new Thread(() -> {
                while(true) {
                    try {
                        String message = inFromServer.readLine();
                        if (message == null) break;
                        handleAction(message);
                    } catch (IOException ioe) {break;}
                }
            }).start();

            BorderPane root = new BorderPane(gamePane);
            root.setRight(scorePane);
            repaintBoard();
            ColumnConstraints colIcon = new ColumnConstraints(30);
            ColumnConstraints colText = new ColumnConstraints(80);
            scorePane.getColumnConstraints().addAll(colIcon, colText);
            scorePane.setPadding(new Insets(10));


            gameStage = new Stage();
            gameStage.setTitle(serverName);
            gameStage.setOnCloseRequest(event -> {try {socket.close(); if (server != null) server.stop();} catch (IOException ioe) {ioe.printStackTrace();}});
            gameStage.setScene(new Scene(root));
            gameStage.getScene().addEventFilter(KeyEvent.KEY_PRESSED, event -> {
                try {
                    if (event.getCode() == KeyCode.W)     outToServer.writeBytes("move:" + Direction.toString(Direction.UP) + "\n");
                    if (event.getCode() == KeyCode.D)     outToServer.writeBytes("move:" + Direction.toString(Direction.RIGHT) + "\n");
                    if (event.getCode() == KeyCode.S)     outToServer.writeBytes("move:" + Direction.toString(Direction.DOWN) + "\n");
                    if (event.getCode() == KeyCode.A)     outToServer.writeBytes("move:" + Direction.toString(Direction.LEFT) + "\n");
                    if (event.getCode() == KeyCode.SPACE) outToServer.writeBytes("shoot\n");
                } catch (IOException ioe) {ioe.printStackTrace();}
            });

            Platform.runLater(() -> { // hokes into the refresh loop, so it is shown after gamePane is painted
                gameStage.show();
            });

        } catch (IOException exception) {}
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

            repaintBoard();
        } else if (action.equals("joined")) {
            String serializedPlayer = message.substring(action.length() + 1);
            Player newPlayer = Player.deserialize(serializedPlayer);
            players.add(newPlayer);
            repaintBoard();
            updateScoreBoard();
        } else if (action.equals("left")) {

            players.remove(player);
            repaintBoard();
            updateScoreBoard();
        } else if(action.equals("shot")) {
            int points = Integer.parseInt(tokens[2]);
            player.setPoints(points);
            updateScoreBoard();

            int x = player.getX();
            int y = player.getY();
            Direction direction = player.getDirection();

            List<Shot> shots = new ArrayList<>();
            if (direction == Direction.UP)      {for (int y2 = y - 1; y2 > 0; y2--) if (board.get(x, y2).startsWith("f")) {shots.add(new Shot(x, y2, direction));} else break; }
            if (direction == Direction.RIGHT)   {for (int x2 = x + 1; x2 > 0; x2++) if (board.get(x2, y).startsWith("f")) {shots.add(new Shot(x2, y, direction));} else break; }
            if (direction == Direction.DOWN)    {for (int y2 = y + 1; y2 > 0; y2++) if (board.get(x, y2).startsWith("f")) {shots.add(new Shot(x, y2, direction));} else break; }
            if (direction == Direction.LEFT)    {for (int x2 = x - 1; x2 > 0; x2--) if (board.get(x2, y).startsWith("f")) {shots.add(new Shot(x2, y, direction));} else break; }

            addShots(shots);
            repaintBoard();
            // wait half a second and remove all shots
            new Thread(() -> {
                try {
                    Thread.sleep(100);
                    removeShots(shots);
                    repaintBoard();
                } catch (InterruptedException ie) {ie.printStackTrace();}
            }).start();
        }
    }

    private synchronized void addShots(List<Shot> shots) {
        firedShots.addAll(shots);
    }

    private synchronized void removeShots(List<Shot> shots) {
        firedShots.removeAll(shots);
    }

    private void repaintBoard() {
        Platform.runLater(() -> {
            synchronized (this) {
                gamePane.getChildren().clear();

                for (int x = 0; x < board.getWidth(); x++)
                    for (int y = 0; y < board.getHeight(); y++) {
                        if (board.get(x, y).equals("w01")) gamePane.add(new ImageView(wall01), x, y);
                        if (board.get(x, y).equals("w02")) gamePane.add(new ImageView(wall02), x, y);
                        if (board.get(x, y).equals("w03")) gamePane.add(new ImageView(wall03), x, y);
                        if (board.get(x, y).equals("w04")) gamePane.add(new ImageView(wall04), x, y);
                        if (board.get(x, y).equals("f01")) gamePane.add(new ImageView(floor01), x, y);
                        if (board.get(x, y).equals("f02")) gamePane.add(new ImageView(floor02), x, y);
                        if (board.get(x, y).equals("f03")) gamePane.add(new ImageView(floor03), x, y);
                        if (board.get(x, y).equals("f04")) gamePane.add(new ImageView(floor04), x, y);
                    }

                for (Player player : players) {
                    ImageView imageView = null;
                    if (player.getId() == 1) {
                        if (player.getDirection() == Direction.UP) imageView = new ImageView(player01Up);
                        if (player.getDirection() == Direction.RIGHT) imageView = new ImageView(player01Right);
                        if (player.getDirection() == Direction.DOWN) imageView = new ImageView(player01Down);
                        if (player.getDirection() == Direction.LEFT) imageView = new ImageView(player01Left);
                    } else if (player.getId() == 2) {
                        if (player.getDirection() == Direction.UP) imageView = new ImageView(player02Up);
                        if (player.getDirection() == Direction.RIGHT) imageView = new ImageView(player02Right);
                        if (player.getDirection() == Direction.DOWN) imageView = new ImageView(player02Down);
                        if (player.getDirection() == Direction.LEFT) imageView = new ImageView(player02Left);
                    } else if (player.getId() == 3) {
                        if (player.getDirection() == Direction.UP) imageView = new ImageView(player03Up);
                        if (player.getDirection() == Direction.RIGHT) imageView = new ImageView(player03Right);
                        if (player.getDirection() == Direction.DOWN) imageView = new ImageView(player03Down);
                        if (player.getDirection() == Direction.LEFT) imageView = new ImageView(player03Left);
                    } else if (player.getId() == 4) {
                        if (player.getDirection() == Direction.UP) imageView = new ImageView(player04Up);
                        if (player.getDirection() == Direction.RIGHT) imageView = new ImageView(player04Right);
                        if (player.getDirection() == Direction.DOWN) imageView = new ImageView(player04Down);
                        if (player.getDirection() == Direction.LEFT) imageView = new ImageView(player04Left);
                    } else {
                        if (player.getDirection() == Direction.UP) imageView = new ImageView(aiUp);
                        if (player.getDirection() == Direction.RIGHT) imageView = new ImageView(aiRight);
                        if (player.getDirection() == Direction.DOWN) imageView = new ImageView(aiDown);
                        if (player.getDirection() == Direction.LEFT) imageView = new ImageView(aiLeft);
                    }
                    gamePane.add(imageView, player.getX(), player.getY());
                }

                for (Shot shot : firedShots) {
                    int randomImage = (int) (Math.random() * 4);
                    if (shot.getDirection() == Direction.UP || shot.getDirection() == Direction.DOWN) {
                        if (randomImage == 0) gamePane.add(new ImageView(shotVertical01), shot.getX(), shot.getY());
                        else if (randomImage == 1) gamePane.add(new ImageView(shotVertical02), shot.getX(), shot.getY());
                        else if (randomImage == 2) gamePane.add(new ImageView(shotVertical03), shot.getX(), shot.getY());
                        else gamePane.add(new ImageView(shotVertical04), shot.getX(), shot.getY());
                    } else {
                        if (randomImage == 0) gamePane.add(new ImageView(shotHorizontal01), shot.getX(), shot.getY());
                        else if (randomImage == 1) gamePane.add(new ImageView(shotHorizontal02), shot.getX(), shot.getY());
                        else if (randomImage == 2) gamePane.add(new ImageView(shotHorizontal03), shot.getX(), shot.getY());
                        else gamePane.add(new ImageView(shotHorizontal04), shot.getX(), shot.getY());
                    }
                }

                for (Node node : gamePane.getChildren()) {
                    ImageView view = (ImageView) node;
                    view.setPreserveRatio(true);
                }

                double tile_size = gameStage.getScene().getHeight() / board.getHeight();
                tile_size = Math.round(tile_size);
                for (Node node : gamePane.getChildren()) {
                    ImageView imageView = (ImageView) node;
                    imageView.setFitWidth(tile_size);
                    imageView.setFitHeight(tile_size);
                }
            }
            updateScoreBoard();
        });
    }

    private synchronized void updateScoreBoard() {
        Platform.runLater(() -> {
            scorePane.getChildren().clear();
            for (int row = 0; row < players.size(); row++) {
                Player player = players.get(row);
                ImageView imageView = null;
                if (player.getId() == 1) {
                    if (player.getDirection() == Direction.UP) imageView = new ImageView(player01Up);
                    if (player.getDirection() == Direction.RIGHT) imageView = new ImageView(player01Right);
                    if (player.getDirection() == Direction.DOWN) imageView = new ImageView(player01Down);
                    if (player.getDirection() == Direction.LEFT) imageView = new ImageView(player01Left);
                } else if (player.getId() == 2) {
                    if (player.getDirection() == Direction.UP) imageView = new ImageView(player02Up);
                    if (player.getDirection() == Direction.RIGHT) imageView = new ImageView(player02Right);
                    if (player.getDirection() == Direction.DOWN) imageView = new ImageView(player02Down);
                    if (player.getDirection() == Direction.LEFT) imageView = new ImageView(player02Left);
                } else if (player.getId() == 3) {
                    if (player.getDirection() == Direction.UP) imageView = new ImageView(player03Up);
                    if (player.getDirection() == Direction.RIGHT) imageView = new ImageView(player03Right);
                    if (player.getDirection() == Direction.DOWN) imageView = new ImageView(player03Down);
                    if (player.getDirection() == Direction.LEFT) imageView = new ImageView(player03Left);
                } else if (player.getId() == 4) {
                    if (player.getDirection() == Direction.UP) imageView = new ImageView(player04Up);
                    if (player.getDirection() == Direction.RIGHT) imageView = new ImageView(player04Right);
                    if (player.getDirection() == Direction.DOWN) imageView = new ImageView(player04Down);
                    if (player.getDirection() == Direction.LEFT) imageView = new ImageView(player04Left);
                } else {
                    if (player.getDirection() == Direction.UP) imageView = new ImageView(aiUp);
                    if (player.getDirection() == Direction.RIGHT) imageView = new ImageView(aiRight);
                    if (player.getDirection() == Direction.DOWN) imageView = new ImageView(aiDown);
                    if (player.getDirection() == Direction.LEFT) imageView = new ImageView(aiLeft);
                }
                imageView.setFitHeight(20);
                imageView.setFitWidth(20);
                scorePane.add(imageView, 0, row);
                scorePane.add(new Label(player.getName() + " " + player.getPoints()), 1, row);
            }
        });
    }
}
