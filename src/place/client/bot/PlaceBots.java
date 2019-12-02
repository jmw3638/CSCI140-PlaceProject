package place.client.bot;

import place.model.ClientModel;
import place.model.NetworkClient;
import place.server.PlaceLogger;

import java.util.ArrayList;

/**
 * The Bot class for the Place game. Creates place bot threads
 * and connects them to the server.
 *
 * @author Jake Waclawski
 */
public class PlaceBots {
    /** list of all the bots */
    private static ArrayList<Bot> bots;

    /**
     * The main method creates all the bots and connects
     * them to the server.
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        if (args.length != 3) {
            PlaceLogger.log(PlaceLogger.LogType.FATAL, PlaceBots.class.getName(), "Usage: java PlaceBot host port num");
        } else {
            bots = new ArrayList<>();
            createBots(args[0], Integer.parseInt(args[1]), Integer.parseInt(args[2]));
        }
    }

    /**
     * Create all the bots and starts their threads.
     * @param host the host to connect to
     * @param port the port to connect to
     * @param numBots the number of bots to create
     */
    private static void createBots(String host, int port, int numBots) {
        for(int i = 0; i < numBots; i++){
            String username = "placeBot-" + i;
            PlaceLogger.log(PlaceLogger.LogType.INFO, PlaceBots.class.getName(), "New bot created, assigned username: " + username);
            ClientModel model = new ClientModel();
            NetworkClient serverConnection = new NetworkClient(host, port, username, model);
            serverConnection.start();
            Bot b = new Bot(serverConnection, model, username);
            bots.add(b);
        }
        for(Bot b : bots) {
            PlaceLogger.log(PlaceLogger.LogType.DEBUG, PlaceBots.class.getName(), "Starting up bot: " + b.getUsername());
            b.start();
        }
    }
}
