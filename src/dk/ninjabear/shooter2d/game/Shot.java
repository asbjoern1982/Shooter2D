package dk.ninjabear.shooter2d.game;

public class Shot {
    private int x, y;
    private Direction direction;
    public Shot(int x, int y, Direction direction) {
        this.x = x; this.y = y;
        this.direction = direction;
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

    public String toString() {
        return "(" + x + ", " + y + ")";
    }
}
