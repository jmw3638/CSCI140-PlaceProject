package place.client.bot;

import place.PlaceColor;
import place.PlaceException;
import place.PlaceTile;
import place.model.ClientModel;
import place.model.NetworkClient;
import place.server.ClientHandler;
import place.server.PlaceLogger;

import java.util.Random;

class Bot extends Thread {
    private static final int BOT_MAX_COOL_DOWN_TIME = 10000;
    private NetworkClient serverConnection;
    private ClientModel model;
    private int botNumber;
    private String username;

    Bot(int botNumber, NetworkClient serverConnection, ClientModel model, String username) {
        this.botNumber = botNumber;
        this.serverConnection = serverConnection;
        this.model = model;
        this.username = username;
    }

    @Override
    public void run() {
        while(true) {
            try {
                Thread.sleep(new Random().nextInt(BOT_MAX_COOL_DOWN_TIME - ClientHandler.PLACE_COOL_DOWN_TIME) + ClientHandler.PLACE_COOL_DOWN_TIME);
            } catch (InterruptedException e) {
                PlaceLogger.log(PlaceLogger.LogType.ERROR, this.getClass().getName(), e.getMessage());
            }
            int row = new Random().nextInt(this.model.getDim());
            int col = new Random().nextInt(this.model.getDim());
            int color = new Random().nextInt(PlaceColor.TOTAL_COLORS);
            PlaceTile tile = new PlaceTile(row, col, this.username, PlaceColor.values()[color]);
            PlaceLogger.log(PlaceLogger.LogType.DEBUG, this.getClass().getName(), " Chose: " + tile);
            try {
                this.serverConnection.sendTileChange(tile);
            } catch (PlaceException e) {
                PlaceLogger.log(PlaceLogger.LogType.WARN, this.getClass().getName(), e.getMessage());
            }
        }
    }
}
