package place.client.gui;

import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import place.PlaceBoard;
import place.PlaceColor;
import place.PlaceTile;

import java.io.Serializable;
import java.util.Random;

public class Tile extends Rectangle implements Serializable {
    private PlaceTile tile;

    Tile(PlaceTile tile, int side) {
        this.setWidth(side);
        this.setHeight(side);
        this.setFill(Color.rgb(tile.getColor().getRed(), tile.getColor().getGreen(), tile.getColor().getBlue()));
        this.tile = tile;
    }

    void setTile(PlaceTile tile) {
        this.tile = tile;
        this.setFill(Color.rgb(tile.getColor().getRed(), tile.getColor().getGreen(), tile.getColor().getBlue()));
    }
    public PlaceTile getTile() { return this.tile; }
}
