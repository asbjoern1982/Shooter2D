package dk.ninjabear.shooter2d;

import dk.ninjabear.shooter2d.ai.PathfindingAi;
import dk.ninjabear.shooter2d.server.Server;

import java.util.Scanner;

public class ServerConsoleMain {
    public static void main(String[] args) {
        Scanner scan = new Scanner(System.in);
        System.out.println("Welcome to Shooter2D");
        System.out.println("Enter the name of the server:");
        String name = scan.nextLine();
        System.out.println("Enter what port the server should listen at:");
        int port = scan.nextInt();
        System.out.println("Enter number of bots:");
        int numberOfBots = scan.nextInt();

        String logfile = "log.txt";

        new Server(name, port, 4, 20, 20, logfile);
        for (int i = 0; i < numberOfBots; i++)
            new PathfindingAi("localhost", port);
    }
}
