package place.client.gui;

import javafx.scene.control.Tooltip;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import place.PlaceTile;

import java.io.Serializable;

public class Tile extends Rectangle implements Serializable {
    private Tooltip info;
    private PlaceTile tile;

    Tile(PlaceTile tile, int side) {
        this.setWidth(side);
        this.setHeight(side);
        this.setFill(Color.rgb(tile.getColor().getRed(), tile.getColor().getGreen(), tile.getColor().getBlue()));
        this.tile = tile;

        this.info = new Tooltip("(" + tile.getRow() + ", " + tile.getCol() + ") " + tile.getColor().getName() + "\noriginal tile");
        Tooltip.install(this, info);
    }

    void setTile(PlaceTile tile) {
        this.tile = tile;
        this.setFill(Color.rgb(tile.getColor().getRed(), tile.getColor().getGreen(), tile.getColor().getBlue()));
        info.setText("(" + tile.getRow() + ", " + tile.getCol() + ") " +
                tile.getColor().getName() + "\n" + tile.getOwner() + "\n" + tile.getTime());
    }
    public PlaceTile getTile() { return this.tile; }
}
