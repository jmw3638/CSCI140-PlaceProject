package place.server;

import place.PlaceBoard;
import place.PlaceTile;
import place.client.gui.Tile;
import place.network.PlaceRequest;

import java.io.*;
import java.lang.reflect.Array;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles client threads
 *
 * @author Jake Waclawski
 */
public class ClientHandler extends Thread {
    private ObjectInputStream networkIn;
    private ObjectOutputStream networkOut;
    private PlaceBoard placeBoard;
    private int clientNum;
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
        this.clientNum = clientNum;
    }

    /**
     * Reports a message to the server output
     * @param msg the message
     */
    private void report(String msg) { System.out.println("Client-" + clientNum + " > " + msg); }

    /**
     * Reports and error to the server output then shuts down the program
     * @param msg the error message
     */
    private void error(String msg) {
        System.out.println("Error-" + clientNum + " > " + msg);
        System.exit(1);
    }

    void write(PlaceRequest msg) {
        try {
            networkOut.writeUnshared(msg);
        } catch (IOException e) {
            error(e.getMessage());
        }
    }

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
                        //if(PlaceServer.addUser((String) response.getData())){
                            report(response.getData() + " logged in to server");
                            this.username = (String) response.getData();
                            write(new PlaceRequest<>(PlaceRequest.RequestType.LOGIN_SUCCESS, clientNum));
                            write(new PlaceRequest<PlaceBoard>(PlaceRequest.RequestType.BOARD, this.placeBoard));
                        //} else {
                        //    report(response.getData() + " failed to log in (username taken).");
                        //    networkOut.writeUnshared(new PlaceRequest<>(PlaceRequest.RequestType.ERROR, response.getData()));
                        //}
                        break;
                    case LOGOUT:
                        report("Closing connection");
                        PlaceServer.clients.remove(this);
                        break;
                    case CHANGE_TILE:
                        PlaceTile tile = (PlaceTile) response.getData();
                        report(tile.getRow() + " " + tile.getCol() + " to " + tile.getColor().getName());
                        System.out.println("send tile change");
                        PlaceServer.sendToAll(new PlaceRequest<PlaceTile>(PlaceRequest.RequestType.TILE_CHANGED, tile));
                        break;
                    case TEST:
                        report((String) response.getData());
                        networkOut.writeUnshared(new PlaceRequest<>(PlaceRequest.RequestType.TEST, ""));
                        break;
                    default:
                        error("Unexpected type: " + response.getType());
                        break;
                }
            } catch (IOException | ClassNotFoundException e) {
                error(e.getMessage());
                break;
            }
        }
    }
}
