package place.client.gui;

import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import place.PlaceColor;
import place.PlaceTile;

import java.io.Serializable;
import java.util.Random;

public class Tile extends Rectangle implements Serializable {
    private int row;
    private int col;
    private PlaceTile tile;

    Tile(int row, int col, int side) {
        this.row = row;
        this.col = col;
        this.setWidth(side);
        this.setHeight(side);
        this.setFill(Color.rgb(
                new Random().nextInt(255),
                new Random().nextInt(255),
                new Random().nextInt(255)));
        tile = new PlaceTile(row, col, "", PlaceColor.WHITE);
    }

    int getRow() { return this.row; }

    int getCol() { return this.col; }

    public PlaceTile getTile() { return this.tile; }
}
