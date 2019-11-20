package place.server;

import place.PlaceBoard;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

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
    /** the place board */
    private static PlaceBoard placeBoard;
    /** the users currently logged in to the server */
    private static List<String> users;

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
                placeBoard = new PlaceBoard(Integer.parseInt(args[1]));
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
        users = new ArrayList<>();
        report("Waiting for clients...");
        while(true) {
            Socket clientSocket = null;

            clientSocket = serverSocket.accept();

            report( "New client connected [" + users.size() + "]");

            ObjectOutputStream networkOut = new ObjectOutputStream(clientSocket.getOutputStream());
            ObjectInputStream networkIn = new ObjectInputStream(clientSocket.getInputStream());

            report("Assigning new thread for client [" + users.size() + "]");
            Thread t = new ClientHandler(placeBoard, networkIn, networkOut, users.size());
            report("Starting thread for client [" + users.size() + "]");
            t.start();
        }
    }

    /**
     * attempt to add a new user to the list of connected clients.
     * A user can only be added if its username has not already been taken.
     * @param user the user to add
     * @return if the user was added to the list
     */
    static boolean addUser(String user) {
        for(String u : users){
            if(user.equals(u)){ return false; }
        }
        users.add(user);
        return true;
    }

    public static void delUser(String user) { users.remove(user); }
}