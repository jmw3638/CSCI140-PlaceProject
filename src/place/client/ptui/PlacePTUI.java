package place.client.ptui;

import place.PlaceTile;
import place.model.ClientModel;
import place.model.Observer;

public class PlacePTUI implements Observer<ClientModel, PlaceTile> {

    @Override
    public void update(ClientModel model, PlaceTile tile) {
    }

    public static void main(String[] args) {
        if (args.length != 3) {
            System.out.println("Usage: java PlaceClient host port username");
        }
    }
}
