package place.client.ptui;

import place.PlaceColor;
import place.PlaceException;
import place.PlaceTile;
import place.model.ClientModel;
import place.model.NetworkClient;
import place.model.Observer;
import place.server.PlaceLogger;

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
    /** the model for the game */
    private ClientModel model;
    /** the username of the client */
    private String username;
    /** the connection to the server */
    private NetworkClient serverConnection;
    /** the user input */
    private Scanner userIn;
    /** the output to the user */
    private PrintWriter userOut;

    /**
     * The main method creates the client.
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        if (args.length != 3) {
            PlaceLogger.log(PlaceLogger.LogType.FATAL, PlacePTUI.class.getName(), "Usage: java PlaceClient host port username");
        }
        else {
            ConsoleApplication.launch(PlacePTUI.class, args);
        }
    }

    /**
     * Initialize the model and connection to the server.
     */
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
     * Handles all client-side logic and server messages.
     * @param userIn the user input
     * @param userOut the output to the server
     */
    @Override
    public void go(Scanner userIn, PrintWriter userOut) {
        this.userIn = userIn;
        this.userOut = userOut;

        this.model.addObserver(this);
        this.serverConnection.start();

        this.refresh();

        while(true) {
            this.userOut.flush();
            int row = this.userIn.nextInt();
            if(row == -1) {
                stop();
            }
            int col = this.userIn.nextInt();
            int color = this.userIn.nextInt();
            PlaceTile tile = new PlaceTile(row, col, this.username, PlaceColor.values()[color]);
            PlaceLogger.log(PlaceLogger.LogType.DEBUG, this.getClass().getName(), "Chose: " + tile);
            try {
                this.serverConnection.sendTileChange(tile);
                this.userOut.println(this.userIn.nextLine());
            } catch (PlaceException e) {
                PlaceLogger.log(PlaceLogger.LogType.WARN, this.getClass().getName(), "Failed to send tile: invalid tile change");
                this.userOut.println("Invalid move");
            }
        }
    }

    /**
     * Output the updated board to the client.
     */
    private void refresh() {
        PlaceTile[][] tiles = this.model.getTiles();
        for(int r = 0; r < tiles.length; r++){
            for(int c = 0; c < tiles[0].length; c++){
                userOut.print(tiles[r][c].getColor().getNumber());
            }
            userOut.println();
        }
        this.userOut.println("Type tile change as row, column, color: ");
    }

    /**
     * Request from the client model to update the board.
     * @param model the client model
     * @param tile the tile to update (ignored)
     */
    @Override
    public void update(ClientModel model, PlaceTile tile) {
        this.refresh();
    }

    /**
     * Close the network connection.
     */
    @Override
    public void stop() {
        PlaceLogger.log(PlaceLogger.LogType.INFO, this.getClass().getName(), "Disconnecting from server");

        this.userIn.close();
        this.userOut.close();
        this.serverConnection.shutDown();

        System.exit(0);
    }
}
