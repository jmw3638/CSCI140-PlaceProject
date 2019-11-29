package place.model;

import place.PlaceBoard;
import place.PlaceTile;
import place.network.PlaceRequest;
import place.server.Logger;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * Represents a client in the network, handles all server messages and
 * user inputs
 *
 * @author Jake Waclawski
 */
public class NetworkClient extends Thread {
    /** the client socket connection to the server */
    private Socket clientSocket;
    /** the incoming connection from the server */
    private ObjectInputStream networkIn;
    /** the outgoing connection to the server */
    private ObjectOutputStream networkOut;
    /** the client model */
    private ClientModel model;
    /** the client number */
    private int clientNumber;
    /** boolean value if the client should listen for server messages */
    private boolean go;
    /** boolean value if the client is ready to place another tile */
    private boolean ready;

    private Logger logger;

    /**
     * Represents a new connection to the server
     * @param username the client username
     * @param model the client model
     */
    public NetworkClient(String host, int port, String username, ClientModel model) {
        try {
            this.clientSocket = new Socket(host, port);
            this.networkOut = new ObjectOutputStream(this.clientSocket.getOutputStream());
            this.networkIn = new ObjectInputStream(this.clientSocket.getInputStream());
            this.model = model;
            this.logger = new Logger();
            this.go = true;
            this.ready = true;

            networkOut.writeUnshared(new PlaceRequest<>(PlaceRequest.RequestType.LOGIN, username));

            boolean ready = false;
            while (!ready) {
                PlaceRequest<?> response = (PlaceRequest<?>) this.networkIn.readUnshared();
                switch (response.getType()) {
                    case LOGIN_SUCCESS:
                        this.clientNumber = Integer.parseInt(response.getData().toString());
                        logger.printToLogger("[" + this.clientNumber + "] Successfully logged in with username: " + username);
                        break;
                    case ERROR:
                        logger.printToLogger("[" + this.clientNumber + "] Failed to log in, username taken: " + username);
                        shutDown();
                        break;
                    case BOARD:
                        ClientModel.initBoard((PlaceBoard) response.getData());
                        ready = true;
                        break;
                    default:
                        logger.printToLogger("ERROR [" + this.clientNumber + "] Unexpected type: " + response.getType());
                        break;
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            logger.printToLogger("ERROR [" + this.clientNumber + "]" + e.getMessage());
        }
    }

    /**
     * Listen and handle server messages
     */
    @Override
    public void run() {
        while (this.go) {
            try {
                PlaceRequest<?> response = (PlaceRequest<?>) networkIn.readUnshared();
                switch(response.getType()) {
                    case TILE_CHANGED:
                        this.model.tileChanged((PlaceTile) response.getData());
                        break;
                    case READY:
                        this.ready = true;
                        break;
                    default:
                        logger.printToLogger("ERROR [" + this.clientNumber + "] Unexpected type: " + response.getType());
                        break;
                }
            } catch (IOException | ClassNotFoundException e) {
                logger.printToLogger("ERROR [" + this.clientNumber + "]" + e.getMessage());
                shutDown();
            }
        }
    }

    /**
     * Stop listening for server messages and close down the client socket.
     * Exit the program.
     */
    public void shutDown() {
        this.go = false;
        try {
            clientSocket.shutdownOutput();
            clientSocket.shutdownInput();
            clientSocket.close();
            System.exit(0);
        } catch (IOException e) {
            logger.printToLogger("ERROR [" + this.clientNumber + "]" + e.getMessage());
        }
    }

    /**
     * Send a tile change request to the server
     * @param tile the tile to change
     */
    public void sendTileChange(PlaceTile tile) {
        try {
            logger.printToLogger("[" + this.clientNumber + "] Sending " + tile);
            this.ready = false;
            networkOut.writeUnshared(new PlaceRequest<PlaceTile>(PlaceRequest.RequestType.CHANGE_TILE, tile));
            networkOut.flush();
        } catch (IOException e) {
            logger.printToLogger("ERROR [" + this.clientNumber + "]" + e.getMessage());
        }
    }

    /**
     * States whether the client is ready to place another tile or not
     * @return if the client is ready
     */
    public boolean isReady() { return this.ready; }
}
