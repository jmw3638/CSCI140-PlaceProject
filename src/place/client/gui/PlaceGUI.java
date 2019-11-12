package place.client.gui;

import javafx.application.Application;
import javafx.stage.Stage;
import place.PlaceTile;
import place.model.ClientModel;
import place.model.Observer;

public class PlaceGUI extends Application implements Observer<ClientModel, PlaceTile> {
    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.show();
    }

    @Override
    public void update(ClientModel model, PlaceTile tile) {
    }

    public static void main(String[] args) {
        if (args.length != 3) {
            System.out.println("Usage: java PlaceGUI host port username");
            System.exit(-1);
        } else {
            Application.launch(args);
        }
    }
}
