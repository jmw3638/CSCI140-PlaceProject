package place.server;

import place.PlaceBoard;
import place.PlaceTile;
import place.model.ClientModel;
import place.network.PlaceRequest;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

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
    /** the clients currently logged in to the server */
    static ArrayList<ClientHandler> clients;

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

    static void updateTile(PlaceTile tile) { placeBoard.setTile(tile); }

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

    static void sendToAll(PlaceRequest msg) throws IOException {
        for(ClientHandler c : PlaceServer.clients){
            c.write(msg);
        }
    }

    static boolean addClient(ClientHandler client) {
        for(ClientHandler c : PlaceServer.clients){
            if(c.getUsername().equals(client.getUsername())){
                return false;
            }
        }
        clients.add(client);
        return true;
    }

    static void removeClient(ClientHandler client) {
        clients.remove(client);
    }

    /**
     * Spawns client threads each time a new client connects.
     * @throws IOException if a network error occurs
     */
    private static void connectClients() throws IOException {
        clients = new ArrayList<>();
        report("Waiting for clients...");
        while(true) {
            Socket clientSocket = serverSocket.accept();

            report( "New client connected [" + (clients.size() + 1) + "]");

            ObjectOutputStream networkOut = new ObjectOutputStream(clientSocket.getOutputStream());
            ObjectInputStream networkIn = new ObjectInputStream(clientSocket.getInputStream());

            report("Assigning new thread for client [" + (clients.size() + 1 )+ "]");
            Thread t = new ClientHandler(placeBoard, networkIn, networkOut, clients.size() + 1);
            report("Starting thread for client [" + (clients.size() + 1) + "]");
            t.start();
        }
    }
}