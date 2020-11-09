package place.client.gui.elements;

import javafx.scene.control.Tooltip;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import place.PlaceColor;

import java.io.Serializable;

/**
 * Represents a color selection button. Allows the user
 * to select which color they want.
 *
 * @author Jake Waclawski
 */
public class ColorSelection extends Rectangle implements Serializable {
    /** the place color of the button */
    private PlaceColor placeColor;
    /** the tooltip information for the button */
    private Tooltip info;

    /**
     * Create a new button representing a place color.
     * @param placeColor the color for the button
     * @param width the width of the button
     * @param height the height of the button
     */
    public ColorSelection(PlaceColor placeColor, int width, int height){
        this.placeColor = placeColor;
        this.setWidth(width);
        this.setHeight(height);
        this.setFill(Color.rgb(
                this.placeColor.getRed(),
                this.placeColor.getGreen(),
                this.placeColor.getBlue()));
        info = new Tooltip(placeColor.getNumber() + " - " + this.getPlaceColor().getName());

        Tooltip.install(this, info);
    }

    /**
     * Set whether the button is currently selected or not,
     * update the tool tip accordingly.
     * @param val if it should be set to selected or not
     */
    public void setSelected(boolean val) {
        if(val) {
            info.setText(this.placeColor.getNumber() + " - " + this.placeColor.getName() + "\nselected");
        } else {
            info.setText(this.placeColor.getNumber() + " - " + this.placeColor.getName());
        }
    }

    /**
     * Get the place color of the button.
     * @return the place color
     */
    public PlaceColor getPlaceColor() { return this.placeColor; }
}
