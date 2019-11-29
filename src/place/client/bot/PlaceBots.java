package place.client.bot;

import place.model.ClientModel;
import place.model.NetworkClient;
import place.server.PlaceLogger;

public class PlaceBots {
    public static void main(String[] args) {
        if (args.length != 3) {
            System.out.println("Usage: java PlaceBot host port num");
            System.exit(-1);
        } else {
            createBots(args[0], Integer.parseInt(args[1]), Integer.parseInt(args[2]));
        }
    }

    private static void createBots(String host, int port, int numBots) {
        for(int i = 1; i <= numBots; i++){
            String username = "placeBot-" + i;
            PlaceLogger.log(PlaceLogger.LogType.DEBUG, PlaceBots.class.getName(), " New bot created, assigned number: " + i);
            ClientModel model = new ClientModel();
            NetworkClient serverConnection = new NetworkClient(host, port, username, model);
            serverConnection.start();
            Thread b = new Bot(i, serverConnection, model, username);
            b.start();
        }
    }
}
