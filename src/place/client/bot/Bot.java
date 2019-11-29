package place.client.bot;

import place.PlaceColor;
import place.PlaceTile;
import place.model.ClientModel;
import place.model.NetworkClient;
import place.server.ClientHandler;
import place.server.Logger;

import java.util.Random;

class Bot extends Thread {
    private static final int BOT_MAX_COOL_DOWN_TIME = 10000;
    private NetworkClient serverConnection;
    private ClientModel model;
    private int botNumber;
    private String username;
    private Logger logger;

    Bot(int botNumber, NetworkClient serverConnection, ClientModel model, String username) {
        this.botNumber = botNumber;
        this.serverConnection = serverConnection;
        this.model = model;
        this.username = username;
        this.logger = new Logger();
    }

    @Override
    public void run() {
        while(true) {
            try {
                Thread.sleep(new Random().nextInt(BOT_MAX_COOL_DOWN_TIME - ClientHandler.PLACE_COOL_DOWN_TIME) + ClientHandler.PLACE_COOL_DOWN_TIME);
            } catch (InterruptedException e) {
                logger.printToLogger("ERROR [" + this.botNumber + "]" + e.getMessage());
            }
            if(serverConnection.isReady()) {
                int row = new Random().nextInt(this.model.getDim());
                int col = new Random().nextInt(this.model.getDim());
                int color = new Random().nextInt(PlaceColor.TOTAL_COLORS);
                PlaceTile tile = new PlaceTile(row, col, this.username, PlaceColor.values()[color]);
                this.serverConnection.sendTileChange(tile);
            }
        }
    }
}
