package place.model;

import place.PlaceBoard;
import place.PlaceTile;
import place.network.PlaceRequest;

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
    /** the username of the client */
    private String username;
    /** boolean value if the client should listen for server messages */
    private boolean go;
    /** boolean value if the client is ready to place another tile */
    private boolean ready;

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
            this.username = username;
            this.model = model;
            this.go = true;
            this.ready = true;

            networkOut.writeUnshared(new PlaceRequest<>(PlaceRequest.RequestType.LOGIN, this.username));

            boolean ready = false;
            while (!ready) {
                PlaceRequest<?> response = (PlaceRequest<?>) this.networkIn.readUnshared();
                switch (response.getType()) {
                    case LOGIN_SUCCESS:
                        report("Successfully logged in (Client-" + response.getData() + ")");
                        break;
                    case ERROR:
                        report("Failed to log in, username taken (" + response.getData() + ")");
                        shutDown();
                        break;
                    case BOARD:
                        ClientModel.initBoard((PlaceBoard) response.getData());
                        ready = true;
                        break;
                    default:
                        error("Unexpected type: " + response.getType());
                        break;
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            error(e.getMessage());
        }
    }

    /**
     * Reports a message to the client output
     *
     * @param msg the message
     */
    private void report(String msg) {
        System.out.println(getClass().getName() + " - Client [" + this.username + "] > " + msg);
    }

    /**
     * Reports and error to the client output then shuts down the program
     *
     * @param msg the error message
     */
    private void error(String msg) {
        System.out.println(getClass().getName() + " - Error > " + msg);
        System.exit(1);
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
                        report(response.getData().toString());
                        this.model.tileChanged((PlaceTile) response.getData());
                        break;
                    case READY:
                        this.ready = true;
                        break;
                    default:
                        error("Unexpected type: " + response.getType());
                        break;
                }
            } catch (IOException | ClassNotFoundException e) {
                report(e.getMessage());
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
            error(e.getMessage());
        }
    }

    /**
     * Send a tile change request to the server
     * @param tile the tile to change
     */
    public void sendTileChange(PlaceTile tile) {
        try {
            this.ready = false;
            report(tile.toString());
            networkOut.writeUnshared(new PlaceRequest<PlaceTile>(PlaceRequest.RequestType.CHANGE_TILE, tile));
            networkOut.flush();
        } catch (IOException e) {
            error(e.getMessage());
        }
    }

    /**
     * States whether the client is ready to place another tile or not
     * @return if the client is ready
     */
    public boolean isReady() { return this.ready; }
}
