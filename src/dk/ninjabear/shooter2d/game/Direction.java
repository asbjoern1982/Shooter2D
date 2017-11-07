package dk.ninjabear.shooter2d.game;

public enum Direction {
    UP, RIGHT, DOWN, LEFT;

    public static Direction toDirection(String direction) {
        if (direction.equals("up")) return UP;
        if (direction.equals("right")) return RIGHT;
        if (direction.equals("down")) return DOWN;
        if (direction.equals("left")) return LEFT;
        return null;
    }

    public static String toString(Direction direction) {
        if (direction == UP) return "up";
        if (direction == RIGHT) return "right";
        if (direction == DOWN) return "down";
        return "left";
    }
}
