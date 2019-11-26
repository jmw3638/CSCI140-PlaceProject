package place.client.gui;

import javafx.scene.control.Tooltip;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import place.PlaceTile;

import java.io.Serializable;

/**
 * Represents a GUI tile on the place board. Contains information
 * such as its location, the actual PlaceTile object it holds, and
 * its tool tip.
 *
 * @author Jake Waclawski
 */
public class Tile extends Rectangle implements Serializable {
    /** the tooltip information to show when hovered over */
    private Tooltip info;
    /** the PlaceTile object stored in the tile */
    private PlaceTile tile;

    /**
     * Create a new Tile object and initialize its appearance on the GUI board
     * @param tile the PlaceTile object to store
     * @param side the side length of the rectangle
     */
    Tile(PlaceTile tile, int side) {
        this.setWidth(side);
        this.setHeight(side);
        this.setFill(Color.rgb(tile.getColor().getRed(), tile.getColor().getGreen(), tile.getColor().getBlue()));
        this.tile = tile;

        this.info = new Tooltip("(" + tile.getRow() + ", " + tile.getCol() + ") " + tile.getColor().getName() + "\noriginal tile");
        Tooltip.install(this, info);
    }

    /**
     * Re-set the PlaceTile object to store
     * @param tile the PlaceTile object
     */
    void setTile(PlaceTile tile) {
        this.tile = tile;
        this.setFill(Color.rgb(tile.getColor().getRed(), tile.getColor().getGreen(), tile.getColor().getBlue()));
        info.setText("(" + tile.getRow() + ", " + tile.getCol() + ") " +
                tile.getColor().getName() + "\n" + tile.getOwner() + "\n" + tile.getTime());
    }

    /**
     * Get the PlaceTile object stored in the tile
     * @return the PlaceTile
     */
    public PlaceTile getTile() { return this.tile; }
}
