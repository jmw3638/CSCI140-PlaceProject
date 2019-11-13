package place.server;

import place.network.PlaceRequest;

import java.io.*;
import java.net.Socket;

/**
 * Handles client threads
 *
 * @author Jake Waclawski
 */
public class ClientHandler extends Thread {
    private ObjectInputStream networkIn;
    private ObjectOutputStream networkOut;
    private Socket socket;
    private int clientNum;

    /**
     * Represents a new client connection
     * @param socket the client socket
     * @param networkIn the incoming connection from the client
     * @param networkOut the outgoing connection to the client
     * @param clientNum the client number
     */
    public ClientHandler(Socket socket, ObjectInputStream networkIn, ObjectOutputStream networkOut, int clientNum) {
        this.socket = socket;
        this.networkIn = networkIn;
        this.networkOut = networkOut;
        this.clientNum = clientNum;
    }

    /**
     * Reports a message to the server output
     * @param msg the message
     */
    private void report(String msg) {
        System.out.println("Client [" + clientNum + "] > " + msg);
    }

    /**
     * Reports and error to the server output then shuts down the program
     * @param msg the error message
     */
    private void error(String msg) {
        System.out.println("Error [" + clientNum + "] > " + msg);
        System.exit(1);
    }

    /**
     * Runs the thread and handles messages from the client
     */
    @Override
    public void run() {
        while(true) {
            try {
                PlaceRequest response = (PlaceRequest) networkIn.readObject();

                if(response.getType().equals(PlaceRequest.RequestType.LOGOUT)){
                    report("Closing connection");
                    networkOut.writeObject(new PlaceRequest<>(PlaceRequest.RequestType.LOGOUT_SUCCESS, clientNum));
                    break;
                } else if(response.getType().equals(PlaceRequest.RequestType.LOGIN)){
                    report(response.getData() + " logged in to server");
                    networkOut.writeObject(new PlaceRequest<>(PlaceRequest.RequestType.LOGIN_SUCCESS, response.getData()));
                } else {
                    report((String) response.getData());
                    networkOut.writeObject(new PlaceRequest<>(PlaceRequest.RequestType.TEST, ""));
                }
            } catch (IOException | ClassNotFoundException e) {
                error(e.getMessage());
            }
        }
    }
}
