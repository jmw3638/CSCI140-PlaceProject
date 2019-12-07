package place.server;

import place.PlaceBoard;
import place.PlaceException;
import place.PlaceLogger;
import place.PlaceTile;
import place.network.PlaceRequest;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Date;

/**
 * Represents a client thread and handles all messages
 * from the client.
 *
 * @author Jake Waclawski
 */
public class ClientHandler extends Thread {
    /** the cool-down time between each user input */
    public static final int PLACE_COOL_DOWN_TIME = 1000;
    /** the incoming connection from the client */
    private ObjectInputStream networkIn;
    /** the outgoing connection to the client */
    private ObjectOutputStream networkOut;
    /** the local place board from the server */
    private PlaceBoard placeBoard;
    /** the client's username */
    private String username;
    /** the client's number */
    private int clientNumber;

    /**
     * Create a new connection to a client.
     * @param placeBoard the place board
     * @param networkIn the incoming connection from the client
     * @param networkOut the outgoing connection to the client
     */
    ClientHandler(PlaceBoard placeBoard, ObjectInputStream networkIn, ObjectOutputStream networkOut, int clientNumber) {
        this.placeBoard = placeBoard;
        this.networkIn = networkIn;
        this.networkOut = networkOut;
        this.clientNumber = clientNumber;
    }

    /**
     * Runs the client connection thread. Reads and handles
     * messages from the client.
     */
    @Override
    public void run() {
        while(true) {
            try {
                PlaceRequest<?> response = (PlaceRequest<?>) networkIn.readUnshared();
                PlaceLogger.log(PlaceLogger.LogType.DEBUG, this.getClass().getName(), "Client " + this.clientNumber + " " + response.toString());
                switch (response.getType()){
                    case LOGIN:
                        String user = (String) response.getData();
                        this.username = user;
                        if(PlaceServer.addClient(this)){
                            PlaceLogger.log(PlaceLogger.LogType.DEBUG, this.getClass().getName(), "Client " + this.clientNumber + " connected to server with username: " + user);
                            PlaceLogger.log(PlaceLogger.LogType.INFO, this.getClass().getName(), user + " logged in to server");
                            write(new PlaceRequest<>(PlaceRequest.RequestType.LOGIN_SUCCESS, this.clientNumber));
                            write(new PlaceRequest<>(PlaceRequest.RequestType.BOARD, this.placeBoard));
                        } else {
                            PlaceLogger.log(PlaceLogger.LogType.WARN, this.getClass().getName(), user + " failed to log in (username taken).");
                            write(new PlaceRequest<>(PlaceRequest.RequestType.ERROR, response.getData()));
                        }
                        break;
                    case CHANGE_TILE:
                        PlaceTile tile = (PlaceTile) response.getData();

                        Date date = new Date();
                        long timeMilliseconds = date.getTime();
                        tile.setTime(timeMilliseconds);

                        PlaceServer.updateTile(tile);
                        PlaceLogger.log(PlaceLogger.LogType.DEBUG, this.getClass().getName(), "Client " + this.clientNumber + " updating: " + tile);
                        PlaceServer.sendToAll(new PlaceRequest<>(PlaceRequest.RequestType.TILE_CHANGED, tile));

                        Thread.sleep(PLACE_COOL_DOWN_TIME);

                        write(new PlaceRequest<>(PlaceRequest.RequestType.READY, null));
                        break;
                    default:
                        PlaceLogger.log(PlaceLogger.LogType.WARN, this.getClass().getName(), PlaceLogger.getLineNumber(), "Client " + this.clientNumber + " : Unexpected type: " + response.getType());
                        break;
                }
            } catch (ClassNotFoundException | InterruptedException | PlaceException e) {
                PlaceLogger.log(PlaceLogger.LogType.WARN, this.getClass().getName(), PlaceLogger.getLineNumber(), "Client " + this.clientNumber + " : " + e.getMessage());
            } catch (IOException e) {
                PlaceLogger.log(PlaceLogger.LogType.DEBUG, this.getClass().getName(), "Client " + this.clientNumber + " disconnected from server");
                PlaceLogger.log(PlaceLogger.LogType.INFO, this.getClass().getName(), this.username + " logged out of server");
                PlaceServer.removeClient(this);
                break;
            }
        }
    }

    /**
     * Write a message to the client over the network.
     * @param msg the message to send
     */
    synchronized void write(PlaceRequest msg) {
        try {
            networkOut.writeUnshared(msg);
            networkOut.flush();
        } catch (IOException e) {
            PlaceLogger.log(PlaceLogger.LogType.WARN, this.getClass().getName(), PlaceLogger.getLineNumber(), "Client " + this.clientNumber + " : " + e.getMessage());
        }
    }

    /**
     * Get this client's username.
     * @return the username
     */
    String getUsername() { return this.username; }
}
