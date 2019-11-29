package place.client.bot;

import place.model.ClientModel;
import place.model.NetworkClient;
import place.server.Logger;

public class PlaceBots {
    private static Logger logger;

    public static void main(String[] args) {
        if (args.length != 3) {
            System.out.println("Usage: java PlaceBot host port num");
            System.exit(-1);
        } else {
            logger = new Logger();
            createBots(args[0], Integer.parseInt(args[1]), Integer.parseInt(args[2]));
        }
    }

    private static void createBots(String host, int port, int numBots) {
        for(int i = 1; i <= numBots; i++){
            String username = "placeBot-" + i;
            logger.printToLogger("[" + i + "] New bot created with username: " + username);
            ClientModel model = new ClientModel();
            NetworkClient serverConnection = new NetworkClient(host, port, username, model);
            serverConnection.start();
            Thread b = new Bot(i, serverConnection, model, username);
            b.start();
        }
    }
}
