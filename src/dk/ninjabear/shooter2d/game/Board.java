package dk.ninjabear.shooter2d.game;

public class Board {

    private String[][] board;

    public Board(int width, int height) {
        board = new String[width][height];
    }

    public int getWidth() {
        return board.length;
    }

    public int getHeight() {
        return board[0].length;
    }

    public void set(int x, int y, String tile) {
        board[x][y] = tile;
    }

    public String get(int x, int y) {
        return board[x][y];
    }

    public String serialize() {
        StringBuilder sb = new StringBuilder();
        for (int y = 0; y < getHeight(); y++) {
            for (int x = 0; x < getWidth(); x++) {
                sb.append(board[x][y]);
                if (x < getWidth() - 1)
                    sb.append(":");
            }
            if (y < getHeight() - 1)
                sb.append(";");
        }
        return sb.toString();
        //return Arrays.asList(board).stream().map(arr -> String.join(":", arr)).collect(Collectors.joining(";"));
    }

    public static Board deserialize(String text) {
        String[] lines = text.split(";");
        int height = lines.length;
        int width = lines[0].split(":").length;

        Board board = new Board(width, height);

        for (int y = 0; y < height; y++) {
            String[] tiles = lines[y].split(":");
            for (int x = 0; x < width; x++)
                board.set(x, y, tiles[x]);
        }
        return board;
    }

    public static Board generateRandomBoard(int width, int height) {
        // assert that the board is at least 3x3
        if (width < 3 || height < 3) return null;

        Board board = new Board(width, height);
        for (int x = 0; x < width; x++)
            for (int y = 0; y < height; y++)
                board.set(x, y, "w01");


        while (!board.validate())
            for (int x = 1; x < width-1; x++)
                for (int y = 1; y < height-1; y++)
                    if (Math.random() * 6 < 1) {
                        board.set(x, y, "f01");
                    }

        // randomize what tiles are used for floor and walls
        for (int x = 0; x < width; x++)
            for (int y = 0; y < height; y++) {
                int number = (int)(Math.random() * 4) + 1;
                if (board.get(x, y).equals("w01"))
                    board.set(x, y, "w0" + number);
                if (board.get(x, y).equals("f01"))
                    board.set(x, y, "f0" + number);
            }
        return board;
    }

    public static Board generateMazedBoard(int width, int height) {
        // assert that the board is at least 3x3
        if (width < 3 || height < 3) return null;

        // build walls around the edges of the board
        Board board = new Board(width, height);
        for (int x = 0; x < width; x++)
            for (int y = 0; y < height; y++) {
                if (x == 0 || x == width -1 || y == 0 || y == height - 1)
                    board.set(x, y, "w01");
                else {
                    if (Math.random() * 6 < 1)
                        board.set(x, y, "w01");
                    else
                        board.set(x, y, "f01");
                }
            }

        int startX = 1;
        int startY = 1;
        int direction = (int) (Math.random() * 4);
        board.set(startX, startY, "w01");



        return board;
    }

    private boolean validate() {
        // first validate that all sites are walls
        for (int x = 0; x < getWidth(); x++)
            if (    board[x][0].startsWith("f") ||
                    board[x][getHeight()-1].startsWith("f"))
                return false;
        for (int y = 1; y < getHeight()-1; y++)
            if (    board[0][y].startsWith("f") ||
                    board[getWidth()-1][y].startsWith("f"))
                return false;

        // pseudo code:
        // get first tile that starts with f
        // color all surrounding tiles with f
        // call this for all surrounding tiles with f
        // at end, check if there are any tiles starting with f that is not colored

        // copy board that we can mess with
        Board copy = new Board(getWidth(), getWidth());
        for (int x = 0; x < getWidth(); x++)
            for (int y = 0; y < getHeight(); y++)
                copy.set(x, y, board[x][y]);

        // find first tile that starts with f
        int startX = -1;
        int startY = -1;
outer:  for (int x = 0; x < getWidth(); x++)
            for (int y = 0; y < getHeight(); y++)
                if (copy.get(x, y).startsWith("f")) {
                    startX = x;
                    startY = y;
                    break outer;
                }
        if (startX == -1 || startY == -1)
            return false;

        // color it and surrounding tiles and recursively their surrounding tiles
        copy.set(startX, startY, "colored");
        colorSurroundingTiles(copy, startX, startY);

        // check if there are any tiles starting with f
        for (int x = 0; x < getWidth(); x++)
            for (int y = 0; y < getHeight(); y++)
                if (copy.get(x, y).startsWith("f"))
                    return false;

        return true;
    }

    private void colorSurroundingTiles(Board copy, int x, int y) {
        if (copy.get(x, y-1).startsWith("f")) {
            copy.set(x, y-1, "colored");
            colorSurroundingTiles(copy, x, y-1);
        }
        if (copy.get(x, y+1).startsWith("f")) {
            copy.set(x, y+1, "colored");
            colorSurroundingTiles(copy, x, y+1);
        }
        if (copy.get(x-1, y).startsWith("f")) {
            copy.set(x-1, y, "colored");
            colorSurroundingTiles(copy, x-1, y);
        }
        if (copy.get(x+1, y).startsWith("f")) {
            copy.set(x+1, y, "colored");
            colorSurroundingTiles(copy, x+1, y);
        }
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (String[] line : board) {
            for (String tile : line)
                sb.append(tile + " ");
            sb.append("\n");
        }
        return sb.toString();
    }
}
