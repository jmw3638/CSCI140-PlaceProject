package place.model;

import place.PlaceBoard;
import place.PlaceException;
import place.PlaceTile;
import place.network.PlaceRequest;
import place.server.PlaceLogger;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * Represents a client in the network. Reads and handles
 * all server messages and user inputs.
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
    /** the client's number */
    private int clientNumber;
    /** boolean value if the client should listen for server messages */
    private boolean go;
    /** boolean value if the client is ready to place another tile */
    private boolean ready;
    /** using local time format? */
    private boolean localTimeFormat;

    /**
     * Represents a client connected to the network.
     * Waits for the board to be sent from the server
     * before running the client.
     * @param username the client username
     * @param model the client model
     */
    public NetworkClient(String host, int port, String username, ClientModel model) {
        try {
            this.clientSocket = new Socket(host, port);
            this.networkOut = new ObjectOutputStream(this.clientSocket.getOutputStream());
            this.networkIn = new ObjectInputStream(this.clientSocket.getInputStream());
            this.model = model;
            this.go = true;
            this.ready = true;

            this.localTimeFormat = true;

            networkOut.writeUnshared(new PlaceRequest<>(PlaceRequest.RequestType.LOGIN, username));

            boolean proceed = false;
            while (!proceed) {
                PlaceRequest<?> response = (PlaceRequest<?>) this.networkIn.readUnshared();
                PlaceLogger.log(PlaceLogger.LogType.DEBUG, this.getClass().getName(), "Server " + response.toString());
                switch (response.getType()) {
                    case LOGIN_SUCCESS:
                        try {
                            this.clientNumber = (Integer) response.getData();
                        } catch(Exception e) {
                            this.clientNumber = -1;
                            this.localTimeFormat = false;
                            PlaceLogger.log(PlaceLogger.LogType.WARN, this.getClass().getName(), e.getMessage());
                        }
                        PlaceLogger.log(PlaceLogger.LogType.DEBUG, this.getClass().getName(), "Assigned client number: " + this.clientNumber);
                        PlaceLogger.log(PlaceLogger.LogType.INFO, this.getClass().getName(), "Successfully logged in with username: " + username);
                        break;
                    case ERROR:
                        PlaceLogger.log(PlaceLogger.LogType.WARN, this.getClass().getName(), "Failed to log in, username taken: " + username);
                        shutDown();
                        break;
                    case BOARD:
                        ClientModel.initBoard((PlaceBoard) response.getData());
                        PlaceLogger.log(PlaceLogger.LogType.DEBUG, this.getClass().getName(), "PlaceBoard received from server and initialized");
                        proceed = true;
                        break;
                    default:
                        PlaceLogger.log(PlaceLogger.LogType.WARN, this.getClass().getName(), "Unexpected type: " + response.getType());
                        break;
                }
            }
        } catch (ClassNotFoundException e) {
            PlaceLogger.log(PlaceLogger.LogType.WARN, this.getClass().getName(), e.getMessage());
        } catch (IOException e) {
            PlaceLogger.log(PlaceLogger.LogType.FATAL, this.getClass().getName(), e.getMessage());
        }
    }

    /**
     * Runs the client. Reads and handles messages
     * from the server.
     */
    @Override
    public void run() {
        while (this.go) {
            try {
                PlaceRequest<?> response = (PlaceRequest<?>) networkIn.readUnshared();
                PlaceLogger.log(PlaceLogger.LogType.DEBUG, this.getClass().getName(), "Server " + response.toString());
                switch(response.getType()) {
                    case TILE_CHANGED:
                        PlaceTile tile = (PlaceTile) response.getData();
                        this.model.tileChanged(tile);
                        PlaceLogger.log(PlaceLogger.LogType.DEBUG, this.getClass().getName(), "Updated: " + tile);
                        break;
                    case READY:
                        this.ready = true;
                        break;
                    default:
                        PlaceLogger.log(PlaceLogger.LogType.WARN, this.getClass().getName(), "Unexpected type: " + response.getType());
                        break;
                }
            } catch (IOException e) {
                PlaceLogger.log(PlaceLogger.LogType.FATAL, this.getClass().getName(), e.getMessage());
            } catch (ClassNotFoundException e) {
                PlaceLogger.log(PlaceLogger.LogType.WARN, this.getClass().getName(), e.getMessage());
            }
        }
    }

    /**
     * Send a tile change request to the server. Only
     * send the request if the change is valid.
     * @param tile the tile to change
     */
    public void sendTileChange(PlaceTile tile) throws PlaceException {
        if(ready) {
            PlaceLogger.log(PlaceLogger.LogType.DEBUG, this.getClass().getName(),"Client " + this.clientNumber + " chose: " + tile);
            if (this.model.isValidChange(tile.getRow(), tile.getCol(), tile.getColor().getNumber())) {
                try {
                    PlaceLogger.log(PlaceLogger.LogType.INFO, this.getClass().getName(), "Sending: " + tile);
                    if(localTimeFormat) { this.ready = false; }
                    networkOut.writeUnshared(new PlaceRequest<PlaceTile>(PlaceRequest.RequestType.CHANGE_TILE, tile));
                    networkOut.flush();
                } catch (IOException e) {
                    PlaceLogger.log(PlaceLogger.LogType.ERROR, this.getClass().getName(), e.getMessage());
                }
            } else {
                PlaceException e = new PlaceException("Invalid tile change: " + tile);
                PlaceLogger.log(PlaceLogger.LogType.WARN, this.getClass().getName(), e.getMessage());
                throw e;
            }
        } else { PlaceLogger.log(PlaceLogger.LogType.DEBUG, this.getClass().getName(), "Failed to choose tile: on cool-down"); }
    }

    /**
     * Stop listening for server messages and close
     * down the client socket.
     */
    public void shutDown() {
        this.go = false;
        try {
            PlaceLogger.log(PlaceLogger.LogType.DEBUG, this.getClass().getName(), "Client " + this.clientNumber + " shutting down network connection");
            PlaceLogger.log(PlaceLogger.LogType.INFO, this.getClass().getName(), "Logging out of server");
            clientSocket.shutdownOutput();
            clientSocket.shutdownInput();
            clientSocket.close();
            System.exit(0);
        } catch (IOException e) {
            PlaceLogger.log(PlaceLogger.LogType.FATAL, this.getClass().getName(), e.getMessage());
        }
    }
}
