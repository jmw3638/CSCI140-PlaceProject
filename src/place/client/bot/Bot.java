package place.client.bot;

import place.PlaceColor;
import place.PlaceException;
import place.PlaceTile;
import place.model.ClientModel;
import place.model.NetworkClient;
import place.server.ClientHandler;
import place.server.PlaceLogger;

import java.util.Random;

/**
 * Represents a place bot. Acts as a player on the
 * place board. Randomly changes tiles at random intervals
 * of time.
 *
 * @author Jake Waclawski
 */
class Bot extends Thread {
    /** the maximum amount of time between bot tile updates */
    private static final int BOT_MAX_COOL_DOWN_TIME = 10000;
    /** the connection to the server */
    private NetworkClient serverConnection;
    /** the model for the game */
    private ClientModel model;
    /** the username of the bot */
    private String username;

    /**
     * Create a new place bot.
     * @param serverConnection the connection to the server
     * @param model the model for the game
     * @param username the username of the bot
     */
    Bot(NetworkClient serverConnection, ClientModel model, String username) {
        this.serverConnection = serverConnection;
        this.model = model;
        this.username = username;
    }

    /**
     * Run the bot. Chooses a tile to update and then sleep
     * for a random amount of time.
     */
    @Override
    public void run() {
        while(true) {
            try {
                Thread.sleep(new Random().nextInt(BOT_MAX_COOL_DOWN_TIME - ClientHandler.PLACE_COOL_DOWN_TIME) + ClientHandler.PLACE_COOL_DOWN_TIME);
                int row = new Random().nextInt(this.model.getDim());
                int col = new Random().nextInt(this.model.getDim());
                int color = new Random().nextInt(PlaceColor.TOTAL_COLORS);
                PlaceTile tile = new PlaceTile(row, col, this.username, PlaceColor.values()[color]);
                PlaceLogger.log(PlaceLogger.LogType.DEBUG, this.getClass().getName(), " Chose: " + tile);
                this.serverConnection.sendTileChange(tile);
            } catch (PlaceException e) {
                PlaceLogger.log(PlaceLogger.LogType.WARN, this.getClass().getName(), e.getMessage());
            } catch (InterruptedException e) {
                PlaceLogger.log(PlaceLogger.LogType.ERROR, this.getClass().getName(), e.getMessage());
            }
        }
    }
}
