package dk.ninjabear.shooter2d.game;

public class Player {
    private String name;
    private int id, x, y, points;
    private Direction direction;

    public Player(String name, int id, int x, int y, Direction direction) {
        this.name = name; this.id = id; this.x = x; this.y = y; this.direction = direction;
        points = 0;
    }

    public String getName() {
        return name;
    }

    public int getId() {
        return id;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public Direction getDirection() {
        return direction;
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }

    public void setDirection(Direction direction) {
        this.direction = direction;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public void addPoints(int points) {
        this.points += points;
    }

    public String serialize() {
        return name + ":" + id + ":" + x + ":" + y + ":" + dirToStr(direction) + ":" + points;
    }

    public static Player deserialize(String text) {
        String[] tokens = text.split(":");
        String name = tokens[0];
        int id = Integer.parseInt(tokens[1]);
        int x = Integer.parseInt(tokens[2]);
        int y = Integer.parseInt(tokens[3]);
        Direction direction = strToDir(tokens[4]);
        int points = Integer.parseInt(tokens[5]);
        Player player = new Player(name, id, x, y, direction);
        player.setPoints(points);
        return player;
    }

    private static String dirToStr(Direction direction) {
        if (direction == Direction.UP) return "up";
        if (direction == Direction.RIGHT) return "right";
        if (direction == Direction.DOWN) return "down";
        if (direction == Direction.LEFT) return "left";
        return null;
    }

    private static Direction strToDir(String direction) {
        if (direction.equals("up")) return Direction.UP;
        if (direction.equals("right")) return Direction.RIGHT;
        if (direction.equals("down")) return Direction.DOWN;
        if (direction.equals("left")) return Direction.LEFT;
        return null;
    }
}
