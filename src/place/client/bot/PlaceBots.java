package place.client.bot;

import place.PlaceLogger;
import place.model.ClientModel;
import place.model.NetworkClient;

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
        if (args.length != 5) {
            PlaceLogger.log(PlaceLogger.LogType.FATAL, PlaceBots.class.getName(), "Usage: java PlaceBot host port type username num");
        } else {
            bots = new ArrayList<>();
            createBots(args[0], Integer.parseInt(args[1]), Integer.parseInt(args[2]), args[3], Integer.parseInt(args[4]));
        }
    }

    /**
     * Create all the bots and starts their threads.
     * @param host the host to connect to
     * @param port the port to connect to
     * @param type the type of bots to create
     * @param username the base username of the bots
     * @param numBots the number of bots to create
     */
    private static void createBots(String host, int port, int type, String username, int numBots) {
        for(int i = 0; i < numBots; i++){
            if(type < 0 || type >= Bot.BotType.values().length) { PlaceLogger.log(PlaceLogger.LogType.FATAL, PlaceBots.class.getName(), "Invalid bot type: " + type); }
            Bot.BotType botType = Bot.BotType.values()[type];
            String botUsername = username + "-" + i;

            ClientModel model = new ClientModel();
            NetworkClient serverConnection = new NetworkClient(host, port, botUsername, model);
            serverConnection.start();

            Bot b = new Bot(botType, serverConnection, model, botUsername);
            bots.add(b);
            PlaceLogger.log(PlaceLogger.LogType.INFO, PlaceBots.class.getName(), "New " + botType + " bot created, assigned username: " + botUsername);
        }
        for(Bot b : bots) {
            PlaceLogger.log(PlaceLogger.LogType.DEBUG, PlaceBots.class.getName(), "Starting up bot: " + b.getUsername());
            b.start();
        }
    }
}
