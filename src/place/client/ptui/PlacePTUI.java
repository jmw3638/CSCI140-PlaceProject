package place.client.ptui;

import place.PlaceColor;
import place.PlaceTile;
import place.model.ClientModel;
import place.model.NetworkClient;
import place.model.Observer;

import java.io.PrintWriter;
import java.util.List;
import java.util.Scanner;

/**
 * Represents a client of the Place board. Establishes a connection with the server
 * and then responds to requests from the server.
 *
 * @author Jake Waclawski
 */
public class PlacePTUI extends ConsoleApplication implements Observer<ClientModel, PlaceTile> {
    private ClientModel model;
    private String username;
    private NetworkClient serverConnection;
    private Scanner userIn;
    private PrintWriter userOut;

    /**
     * The main method creates the client
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        if (args.length != 3) {
            System.out.println("Usage: java PlaceClient host port username");
            System.exit(0);
        }
        else {
            ConsoleApplication.launch(PlacePTUI.class, args);
        }
    }

    @Override
    public void init() {
        List<String> args = super.getArguments();
        String host = args.get(0);
        int port = Integer.parseInt(args.get(1));
        this.username = args.get(2);

        this.model = new ClientModel();

        this.serverConnection = new NetworkClient(host, port, this.username, this.model);
    }

    /**
     * Handles all client-side logic and server messages
     */
    @Override
    public void go(Scanner userIn, PrintWriter userOut) {
        this.userIn = userIn;
        this.userOut = userOut;

        this.model.addObserver(this);
        this.serverConnection.start();

        this.refresh();
        while(true) { }
    }

    private void refresh() {
        PlaceTile[][] tiles = this.model.getTiles();
        for(int r = 0; r < tiles.length; r++){
            for(int c = 0; c < tiles[0].length; c++){
                userOut.print(tiles[r][c].getColor().getNumber());
            }
            userOut.println();
        }
        boolean done = false;
        do {
            this.userOut.print("type tile change as row, column, color: ");
            this.userOut.flush();
            int row = this.userIn.nextInt();
            if(row == -1) { System.exit(0); }
            int col = this.userIn.nextInt();
            int color = this.userIn.nextInt();
            if(this.model.isValidMove(row, col, color)) {
                this.serverConnection.sendTileChange(new PlaceTile(row, col, this.username, PlaceColor.values()[color]));
                this.userOut.println(this.userIn.nextLine());
                done = true;
            }
        } while (!done);
    }

    @Override
    public void update(ClientModel model, PlaceTile tile) {
        this.refresh();
    }

    @Override
    public void stop() {
        this.userIn.close();
        this.userOut.close();
        this.serverConnection.shutDown();
    }
}
