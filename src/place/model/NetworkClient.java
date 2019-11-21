package place.model;

import place.PlaceBoard;
import place.PlaceColor;
import place.PlaceTile;
import place.client.gui.Tile;
import place.network.PlaceRequest;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class NetworkClient {
    private Socket clientSocket;
    private ObjectInputStream networkIn;
    private ObjectOutputStream networkOut;
    private ClientModel model;
    private String username;
    private boolean go;

    public NetworkClient(String host, int port, String username, ClientModel model) {
        try {
            this.clientSocket = new Socket(host, port);
            this.networkOut = new ObjectOutputStream(clientSocket.getOutputStream());
            this.networkIn = new ObjectInputStream(clientSocket.getInputStream());
            this.username = username;
            this.model = model;
            go = true;

            networkOut.writeUnshared(new PlaceRequest<>(PlaceRequest.RequestType.LOGIN, this.username));

            boolean ready = false;
            while (!ready) {
                PlaceRequest<?> response = (PlaceRequest<?>) this.networkIn.readUnshared();
                switch (response.getType()) {
                    case LOGIN_SUCCESS:
                        report("Successfully logged in (Client-" + response.getData() + ")");
                        break;
                    case ERROR:
                        System.out.println("Failed to log in, username taken (" + response.getData() + ")");
                        break;
                    case BOARD:
                        ClientModel.initBoard((PlaceBoard) response.getData());
                        ready = true;
                        break;
                    default:
                        System.err.println("Unexpected type: " + response.getType());
                        System.exit(1);
                        break;
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
    }

    /**
     * Reports a message to the client output
     *
     * @param msg the message
     */
    private void report(String msg) {
        System.out.println("Client > " + msg);
    }

    /**
     * Reports and error to the client output then shuts down the program
     *
     * @param msg the error message
     */
    private void error(String msg) {
        System.out.println("Error > " + msg);
        System.exit(1);
    }

    private void run() {
        while (this.go) {
            try {
                PlaceRequest<?> response = (PlaceRequest<?>) networkIn.readUnshared();

                if (response.getType() == PlaceRequest.RequestType.TILE_CHANGED) {
                    System.out.println("got tile change");
                    this.model.tileChanged((PlaceTile) response.getData());
                } else {
                    error("Unexpected type: " + response.getType());
                }
            } catch (IOException | ClassNotFoundException e) {
                stop();
                error(e.getMessage());
                break;
            }
        }
    }

    public void startListener() {
        // Run rest of client in separate thread.
        // This threads stops on its own at the end of the game and
        // does not need to rendezvous with other software components.
        Thread netThread = new Thread(this::run);
        netThread.start();
    }

    private synchronized void stop() {
        this.go = false;
    }

    public void sendTileChange(PlaceTile tile) {
        try {
            networkOut.writeUnshared(new PlaceRequest<PlaceTile>(PlaceRequest.RequestType.CHANGE_TILE, tile));
            networkOut.flush();
        } catch (IOException e) {
            error(e.getMessage());
        }
    }
}
