package place.server;

import place.PlaceBoard;
import place.PlaceException;
import place.PlaceTile;
import place.network.PlaceRequest;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Handles the client thread and all messages from the client
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
    /** the client's number of the currently connected clients */
    private int clientNumber;
    /** the client's username */
    private String username;

    /**
     * Represents a new client connection
     * @param placeBoard the place board
     * @param networkIn the incoming connection from the client
     * @param networkOut the outgoing connection to the client
     * @param clientNum the client number
     */
    ClientHandler(PlaceBoard placeBoard, ObjectInputStream networkIn, ObjectOutputStream networkOut, int clientNum) {
        this.placeBoard = placeBoard;
        this.networkIn = networkIn;
        this.networkOut = networkOut;
        this.clientNumber = clientNum;
    }

    /**
     * Send a message to the client
     * @param msg the message
     */
    synchronized void write(PlaceRequest msg) {
            try {
                networkOut.writeUnshared(msg);
                networkOut.flush();
            } catch (IOException e) {
                PlaceLogger.log(PlaceLogger.LogType.ERROR, this.getClass().getName(), e.getMessage());
            }
    }

    /**
     * Get the client's username
     * @return the username
     */
    String getUsername() { return this.username; }

    /**
     * Runs the thread and handles messages from the client
     */
    @Override
    public void run() {
        while(true) {
            try {
                PlaceRequest<?> response = (PlaceRequest<?>) networkIn.readUnshared();
                switch (response.getType()){
                    case LOGIN:
                        String user = (String) response.getData();
                        this.username = user;
                        if(PlaceServer.addClient(this)){
                            PlaceLogger.log(PlaceLogger.LogType.INFO, this.getClass().getName(), user + " logged in to server");
                            write(new PlaceRequest<>(PlaceRequest.RequestType.LOGIN_SUCCESS, this.clientNumber));
                            write(new PlaceRequest<PlaceBoard>(PlaceRequest.RequestType.BOARD, this.placeBoard));
                        } else {
                            PlaceLogger.log(PlaceLogger.LogType.WARN, this.getClass().getName(), user + " failed to log in (username taken).");
                            write(new PlaceRequest<>(PlaceRequest.RequestType.ERROR, response.getData()));
                        }
                        break;
                    case CHANGE_TILE:
                        DateTimeFormatter dateTime = DateTimeFormatter.ofPattern("MM/dd/yyyy @ h:mm:ss a");
                        LocalDateTime now = LocalDateTime.now();

                        PlaceTile tile = (PlaceTile) response.getData();
                        tile.setTime(dateTime.format(now));
                        PlaceServer.updateTile(tile);
                        PlaceLogger.log(PlaceLogger.LogType.DEBUG, this.getClass().getName(), "Updating: " + tile);
                        PlaceServer.sendToAll(new PlaceRequest<PlaceTile>(PlaceRequest.RequestType.TILE_CHANGED, tile));
                        Thread.sleep(PLACE_COOL_DOWN_TIME);
                        write(new PlaceRequest<>(PlaceRequest.RequestType.READY, null));
                        break;
                    default:
                        PlaceLogger.log(PlaceLogger.LogType.WARN, this.getClass().getName(), "Unexpected type: " + response.getType());
                        break;
                }
            } catch (ClassNotFoundException | PlaceException | InterruptedException e) {
                PlaceLogger.log(PlaceLogger.LogType.ERROR, this.getClass().getName(), e.getMessage());
            } catch (IOException e) {
                PlaceLogger.log(PlaceLogger.LogType.INFO, this.getClass().getName(), this.username + " Disconnected from server");
                PlaceServer.removeClient(this);
                break;
            }
        }
    }
}
