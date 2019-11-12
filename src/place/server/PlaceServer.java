package place.server;

/**
 * The Place server is run on the command line as:
 *
 * $ java PlaceServer port DIM
 *
 * Where port is the port number of the host and DIM is the square dimension
 * of the board.
 *
 * @author Sean Strout @ RIT CS
 */
public class PlaceServer {
    /**
     * The main method starts the server and spawns client threads each time a new
     * client connects.
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Usage: java PlaceServer port DIM");
        } else {
        }
    }
}