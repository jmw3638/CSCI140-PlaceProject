package place.server;

import place.PlaceBoard;
import place.PlaceException;
import place.PlaceLogger;
import place.PlaceTile;
import place.network.PlaceRequest;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

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
    /** the server socket connection to the clients */
    private static ServerSocket serverSocket;
    /** the server-side place board */
    private static PlaceBoard placeBoard;
    /** the clients currently logged in to the server */
    private static ArrayList<ClientHandler> clients;

    /**
     * The main method starts the server and initializes the
     * server-side place board.
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        if (args.length != 2) {
            PlaceLogger.log(PlaceLogger.LogType.FATAL, PlaceServer.class.getName(), "Usage: java PlaceServer port DIM");
        } else {
            PlaceLogger.log(PlaceLogger.LogType.INFO, PlaceServer.class.getName(), "Starting up server");
            try {
                int port = Integer.parseInt(args[0]);
                int dim = Integer.parseInt(args[1]);
                serverSocket = new ServerSocket(port);
                placeBoard = new PlaceBoard(dim);
                PlaceLogger.log(PlaceLogger.LogType.DEBUG, PlaceServer.class.getName(), "Server started on port: " + port);
                connectClients();
            } catch (IOException e) {
                PlaceLogger.log(PlaceLogger.LogType.ERROR, PlaceServer.class.getName(), e.getMessage());
            }
        }
    }

    /**
     * Spawns and starts client threads each time a new client
     * connects to the server.
     * @throws IOException if a network error occurs
     */
    private static void connectClients() throws IOException {
        clients = new ArrayList<>();
        PlaceLogger.log(PlaceLogger.LogType.INFO, PlaceServer.class.getName(), "Waiting for clients...");
        while(true) {
            Socket clientSocket = serverSocket.accept();
            PlaceLogger.log(PlaceLogger.LogType.DEBUG, PlaceServer.class.getName(), "New client connected, assigned number: " + clients.size());

            ObjectOutputStream networkOut = new ObjectOutputStream(clientSocket.getOutputStream());
            ObjectInputStream networkIn = new ObjectInputStream(clientSocket.getInputStream());

            Thread t = new ClientHandler(placeBoard, networkIn, networkOut, clients.size());
            t.start();
        }
    }

    /**
     * Send a message to all clients currently connected to the server.
     * Individually calls each client's write() method.
     * @param msg the message to send
     */
    static void sendToAll(PlaceRequest msg) {
        try {
            for(ClientHandler c : PlaceServer.clients){
                c.write(msg);
            }
        } catch (Exception e) {
            PlaceLogger.log(PlaceLogger.LogType.WARN, PlaceServer.class.getName(), PlaceLogger.getLineNumber(), e.toString());
        }
    }

    /**
     * Attempt to update a tile on the server-side place board.
     * @param tile the tile to update
     * @throws PlaceException if the tile coordinates are invalid
     */
    static void updateTile(PlaceTile tile) throws PlaceException {
        if(placeBoard.isValid(tile)) {
            placeBoard.setTile(tile);
        } else {
            throw new PlaceException("Invalid tile coordinates");
        }
    }

    /**
     * Add a client to the list of clients. Only adds the client if its username
     * is not already currently taken.
     * @param client the client to add
     * @return if the client was able to be added
     */
    static boolean addClient(ClientHandler client) {
        for(ClientHandler c : clients){
            if(c.getUsername().equals(client.getUsername())){
                return false;
            }
        }
        clients.add(client);
        StringBuilder msg = new StringBuilder(clients.size() + " connected user(s): [ ");
        for(ClientHandler c : clients) { msg.append(c.getUsername()).append(" , "); }
        msg.append("]");
        PlaceLogger.log(PlaceLogger.LogType.DEBUG, PlaceServer.class.getName(), msg.toString());
        return true;
    }

    /**
     * Remove a client from the list of clients.
     * @param client the client to remove
     */
    static void removeClient(ClientHandler client) {
        clients.remove(client);
    }
}