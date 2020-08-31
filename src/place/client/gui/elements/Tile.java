package place.client.gui.elements;

import javafx.scene.control.Tooltip;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import place.PlaceTile;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

/**
 * Represents a GUI tile on the place board. Contains information
 * such as its location, the actual PlaceTile object it holds, and
 * its Tooltip.
 *
 * @author Jake Waclawski
 */
public class Tile extends Rectangle implements Serializable {
    /** the date and time format for displaying the tile time */
    private final DateFormat DATE_TIME_FORMAT = new SimpleDateFormat("M/d/yyyy h:mm:ss.SSS a");
    /** the tooltip information to show when hovered over */
    private Tooltip info;
    /** the PlaceTile object stored in the tile */
    private PlaceTile tile;

    /**
     * Create a new Tile object and initialize its appearance on the GUI board.
     * @param tile the PlaceTile object to store
     * @param side the side length of the rectangle
     */
    public Tile(PlaceTile tile, int side) {
        this.setWidth(side);
        this.setHeight(side);
        this.setFill(Color.rgb(tile.getColor().getRed(), tile.getColor().getGreen(), tile.getColor().getBlue()));
        this.tile = tile;
        this.info = new Tooltip();

        Tooltip.install(this, info);

        if(tile.getTime() == 0) {
            info.setText("(" + tile.getRow() + ", " + tile.getCol() + ") " + tile.getColor().getName() + "\noriginal tile");
        } else {
            info.setText("(" + tile.getRow() + ", " + tile.getCol() + ") " +
                    tile.getColor().getName() + "\n" + tile.getOwner() + "\n" + DATE_TIME_FORMAT.format(tile.getTime()));
        }
    }

    /**
     * Re-set the PlaceTile object to store.
     * @param tile the PlaceTile object
     */
    public void setTile(PlaceTile tile) {
        this.tile = tile;
        this.setFill(Color.rgb(tile.getColor().getRed(), tile.getColor().getGreen(), tile.getColor().getBlue()));
        info.setText("(" + tile.getRow() + ", " + tile.getCol() + ") " +
                tile.getColor().getName() + "\n" + tile.getOwner() + "\n" + DATE_TIME_FORMAT.format(tile.getTime()));
    }

    /**
     * Get the PlaceTile object stored in the tile.
     * @return the PlaceTile
     */
    public PlaceTile getTile() { return this.tile; }
}
