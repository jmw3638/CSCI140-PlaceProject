package place.client.bot;

import place.PlaceColor;
import place.PlaceException;
import place.PlaceLogger;
import place.PlaceTile;
import place.model.ClientModel;
import place.model.NetworkClient;
import place.server.ClientHandler;

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
    private static final int BOT_MAX_COOL_DOWN_TIME = 5000;

    /**
     * Represents different types of bots which behaves differently
     * when choosing tiles.
     */
    enum BotType {
        /**
         * A bot that randomly chooses tile locations and colors.
         */
        RANDOM,

        /**
         * A bot that randomly chooses tile locations but always
         * the same color.
         */
        COLOR,

        /**
         * A bot that randomly chooses tile locations within a given
         * region but always the same color.
         */
        REGION,

        /**
         * A bot that converts all tiles of a certain color to another
         * color.
         */
        GRIEF
    }

    /** the type of bot */
    private BotType botType;
    /** the connection to the server */
    private NetworkClient serverConnection;
    /** the model for the game */
    private ClientModel model;
    /** the username of the bot */
    private String username;

    /**
     * Create a new place bot.
     * @param botType the type of bot
     * @param serverConnection the connection to the server
     * @param model the model for the game
     * @param username the username of the bot
     */
    Bot(BotType botType, NetworkClient serverConnection, ClientModel model, String username) {
        this.botType = botType;
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

                PlaceTile tile = null;
                switch(botType) {
                    case RANDOM:
                        tile = randomTile();
                        break;
                    case COLOR:
                        tile = colorTile(PlaceColor.RED);
                        break;
                    case REGION:
                        tile = regionTile(40, 60, 40, 60, PlaceColor.RED);
                        break;
                    case GRIEF:
                        tile = griefTile(PlaceColor.WHITE);
                        break;
                    default:
                        PlaceLogger.log(PlaceLogger.LogType.FATAL, this.getClass().getName(), "Invalid bot type: " + botType);
                        break;
                }

                PlaceLogger.log(PlaceLogger.LogType.DEBUG, this.getClass().getName(), "Bot choosing: " + tile);
                this.serverConnection.sendTileChange(tile);
            } catch (PlaceException e) {
                PlaceLogger.log(PlaceLogger.LogType.WARN, this.getClass().getName(), PlaceLogger.getLineNumber(),e.getMessage());
            } catch (InterruptedException e) {
                PlaceLogger.log(PlaceLogger.LogType.ERROR, this.getClass().getName(), PlaceLogger.getLineNumber(), e.getMessage());
            } catch (Exception e) {
                PlaceLogger.log(PlaceLogger.LogType.FATAL, this.getClass().getName(), PlaceLogger.getLineNumber(), e.getMessage());
            }
        }
    }

    /**
     * Choose a completely random tile.
     * @return the tile
     */
    private PlaceTile randomTile() {
        int row = new Random().nextInt(this.model.getDim());
        int col = new Random().nextInt(this.model.getDim());
        int color = new Random().nextInt(PlaceColor.TOTAL_COLORS);
        return new PlaceTile(row, col, this.username, PlaceColor.values()[color]);
    }

    /**
     * Choose a random tile that is not already a specific color
     * and change it to that color.
     * @param color the chosen color
     * @return the tile
     */
    private PlaceTile colorTile(PlaceColor color) {
        int row, col;
        do {
            row = new Random().nextInt(this.model.getDim());
            col = new Random().nextInt(this.model.getDim());
        } while(this.model.getTiles()[row][col].getColor().equals(color));
        return new PlaceTile(row, col, this.username, color);
    }

    /**
     * Choose a random tile within a specific region that is not
     * already a specific color and change it to that color.
     * @param lowRowBound the low row bound of the region
     * @param highRowBound the high row bound of the region
     * @param lowColBound the low column bound of the region
     * @param highColBound the high column bound of the region
     * @param color the chosen color
     * @return the tile
     */
    private PlaceTile regionTile(int lowRowBound, int highRowBound, int lowColBound, int highColBound, PlaceColor color) {
        int row, col;
        do {
            row = lowRowBound + (new Random().nextInt(highRowBound - lowRowBound + 1));
            col = lowColBound + (new Random().nextInt(highColBound - lowColBound + 1));
        } while(this.model.getTiles()[row][col].getColor().equals(color));
        return new PlaceTile(row, col, this.username, color);
    }

    /**
     * Choose a random tile that is a certain color and change it
     * to another random color
     * @param color the color to grief
     * @return the tile
     */
    private PlaceTile griefTile(PlaceColor color) {
        int row, col, toColor;
        do {
            row = new Random().nextInt(this.model.getDim());
            col = new Random().nextInt(this.model.getDim());
            toColor = new Random().nextInt(PlaceColor.TOTAL_COLORS);
        } while(!this.model.getTiles()[row][col].getColor().equals(color) || toColor == color.getNumber());

        return new PlaceTile(row, col, this.username, PlaceColor.values()[toColor]);
    }

    /**
     * Get the bot's username.
     * @return the username
     */
    String getUsername() { return this.username; }
}
