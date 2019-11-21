package place.client.ptui;

import place.PlaceTile;
import place.model.ClientModel;
import place.model.Observer;
import place.network.PlaceRequest;
import place.server.PlaceServer;

import java.io.*;
import java.net.Socket;

/**
 * Represents a client of the Place board. Establishes a connection with the server
 * and then responds to requests from the server.
 *
 * @author Jake Waclawski
 */
public class PlacePTUI implements Observer<ClientModel, PlaceTile> {
    /** the client socket which expects a host and port */
    private static Socket clientSocket;
    /** the incoming connection from the server */
    private static ObjectInputStream networkIn;
    /** the outgoing connection to the server */
    private static ObjectOutputStream networkOut;
    /** handle user inputs */
    private static BufferedReader input;

    @Override
    public void update(ClientModel model, PlaceTile tile) {
    }

    /**
     * The main method creates the client
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        if (args.length != 3) {
            System.out.println("Usage: java PlaceClient host port username");
            System.exit(1);
        }
        else {
            try {
                createClient(args[0], Integer.parseInt(args[1]), args[2]);
                input = new BufferedReader(new InputStreamReader(System.in));
                go();
                shutDown();
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    /**
     * Handles all client-side logic and server messages
     */
    private static void go() {
        while(true) {
            try {
                PlaceRequest response = (PlaceRequest) networkIn.readObject();

                if(response.getType().equals(PlaceRequest.RequestType.LOGIN_SUCCESS)){
                    System.out.println(response.getData() + " connected to server");
                    networkOut.writeObject(new PlaceRequest<>(PlaceRequest.RequestType.TEST, "Ready for input"));
                } else {
                    System.out.print("Input: ");
                    String test = input.readLine();
                    if(test.equals("EXIT")){
                        networkOut.writeObject(new PlaceRequest<>(PlaceRequest.RequestType.LOGOUT, ""));
                    } else {
                        System.out.println("\"" + test + "\" sent to server");
                        networkOut.writeObject(new PlaceRequest<>(PlaceRequest.RequestType.TEST, test));
                    }
                }
            } catch (IOException | ClassNotFoundException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    /**
     * Creates the client and connects it to the server socket
     * @param host the host ip to connect to
     * @param port the host port to connect to
     * @param user the username of the client
     */
    private static void createClient(String host, int port, String user) {
        try {
            clientSocket = new Socket(host, port);

            networkOut = new ObjectOutputStream(clientSocket.getOutputStream());
            networkIn = new ObjectInputStream(clientSocket.getInputStream());

            networkOut.writeObject(new PlaceRequest<>(PlaceRequest.RequestType.LOGIN, user));
            System.out.println("Connecting to server");
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * Called at the end of the game to close down the network connection.
     *
     * @throws IOException a network error occurred
     */
    private static void shutDown() throws IOException {
        System.out.println("Closing connection to server...");
        clientSocket.shutdownInput();
        clientSocket.shutdownOutput();
        clientSocket.close();
    }
}
