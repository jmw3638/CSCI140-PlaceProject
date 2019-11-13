package place.server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * The Place server is run on the command line as:
 *
 * $ java PlaceServer port DIM
 *
 * Where port is the port number of the host and DIM is the square dimension
 * of the board.
 *
 * @author Sean Strout @ RIT CS
 * @author Jake Waclawski
 */
public class PlaceServer {
    /** the server socket */
    private static ServerSocket serverSocket;

    /**
     * The main method starts the server
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Usage: java PlaceServer port DIM");
            System.exit(1);
        } else {
            report("Starting up server");
            try {
                serverSocket = new ServerSocket(Integer.parseInt(args[0]));
                connectClients();
            } catch (IOException e) {
                error(e.getMessage());
            }
        }
    }

    /**
     * Reports a message to the server output
     * @param msg the message
     */
    private static void report(String msg) {
        System.out.println("Server > " + msg);
    }

    /**
     * Reports and error to the server output then shuts down the program
     * @param msg the error message
     */
    private static void error(String msg) {
        System.out.println("Error > " + msg);
        System.exit(1);
    }

    /**
     * Spawns client threads each time a new client connects.
     * @throws IOException if a network error occurs
     */
    private static void connectClients() throws IOException {
        int clientCount = 0;
        report("Waiting for clients...");
        while(true) {
            Socket clientSocket = null;

            clientSocket = serverSocket.accept();

            clientCount++;
            report( "New client connected [" + clientCount + "]");

            ObjectOutputStream networkOut = new ObjectOutputStream(clientSocket.getOutputStream());
            ObjectInputStream networkIn = new ObjectInputStream(clientSocket.getInputStream());

            report("Assigning new thread for client [" + clientCount + "]");
            Thread t = new ClientHandler(clientSocket, networkIn, networkOut, clientCount);
            report("Starting thread for client [" + clientCount + "]");
            t.start();
        }
    }
}