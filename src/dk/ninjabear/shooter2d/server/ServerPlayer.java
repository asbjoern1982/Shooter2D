package dk.ninjabear.shooter2d.server;

import dk.ninjabear.shooter2d.game.Direction;
import dk.ninjabear.shooter2d.game.Player;

import java.io.DataOutputStream;

public class ServerPlayer extends Player {
    private DataOutputStream outToClient;

    public ServerPlayer(String name, int id, int x, int y, Direction direction, DataOutputStream outToClient) {
        super(name, id,  x, y, direction);
        this.outToClient = outToClient;
    }

    public DataOutputStream getOutToClient() {
        return outToClient;
    }
}
