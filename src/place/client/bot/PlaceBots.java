package place.client.bot;

import place.model.ClientModel;
import place.model.NetworkClient;

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
        for(int i = 0; i < numBots; i++){
            String username = "placeBot-" + i;
            ClientModel model = new ClientModel();
            NetworkClient serverConnection = new NetworkClient(host, port, username, model);
            serverConnection.start();
            report("Assigning new thread to bot " + i);
            Thread b = new Bot(i, serverConnection, model, username);
            report("Starting thread for bot " + i);
            b.start();
        }
    }

    private static void report(String msg) { System.out.println(PlaceBots.class.getName() + " - Bot Handler > " + msg); }
}
