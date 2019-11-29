package place.server;

import place.PlaceBoard;
import place.PlaceException;
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
    /** the server socket */
    private static ServerSocket serverSocket;
    /** the place board */
    private static PlaceBoard placeBoard;
    /** the clients currently logged in to the server */
    private static ArrayList<ClientHandler> clients;

    private static Logger logger;

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
            logger = new Logger();
            logger.printToLogger("Starting up server");
            try {
                serverSocket = new ServerSocket(Integer.parseInt(args[0]));
                placeBoard = new PlaceBoard(Integer.parseInt(args[1]));
                connectClients();
            } catch (IOException e) {
                logger.printToLogger("ERROR [SERVER]" + e.getMessage());
            }
        }
    }

    /**
     * Attempt to update a tile on the server side place board
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
     * Send a message to all clients connected to the server
     * @param msg the message
     */
    static void sendToAll(PlaceRequest msg) {
        for(ClientHandler c : PlaceServer.clients){
            c.write(msg);
        }
    }

    /**
     * Add a client to the list of clients. Only adds the client if its username
     * is now already being currently used
     * @param client the client to add
     * @return if the client was able to be added
     */
    static boolean addClient(ClientHandler client) {
        for(ClientHandler c : PlaceServer.clients){
            if(c.getUsername().equals(client.getUsername())){
                return false;
            }
        }
        clients.add(client);
        return true;
    }

    /**
     * Remove a client from the list of clients
     * @param client the client to remove
     */
    static void removeClient(ClientHandler client) {
        clients.remove(client);
    }

    /**
     * Spawns client threads each time a new client connects.
     * @throws IOException if a network error occurs
     */
    private static void connectClients() throws IOException {
        clients = new ArrayList<>();
        logger.printToLogger("Waiting for clients...");
        while(true) {
            Socket clientSocket = serverSocket.accept();
            logger.printToLogger("[" + (clients.size() + 1) + "] New client connected");

            ObjectOutputStream networkOut = new ObjectOutputStream(clientSocket.getOutputStream());
            ObjectInputStream networkIn = new ObjectInputStream(clientSocket.getInputStream());

            Thread t = new ClientHandler(placeBoard, networkIn, networkOut, clients.size() + 1);
            t.start();
        }
    }
}