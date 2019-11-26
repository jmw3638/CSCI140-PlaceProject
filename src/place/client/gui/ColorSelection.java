package place.client.gui;

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
class ColorSelection extends Rectangle implements Serializable {
    /** the place color of the button */
    private PlaceColor placeColor;
    /** the tooltip information for the button */
    private Tooltip info;

    /**
     * Create a new button representing a place color
     * @param placeColor the color for the button
     * @param width the width of the button
     * @param height the height of the button
     */
    ColorSelection(PlaceColor placeColor, int width, int height){
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
     * Get the place color of the button
     * @return the place color
     */
    PlaceColor getPlaceColor() { return this.placeColor; }

    /**
     * Set whether the button is currently selected or not, update the tool tip accordingly
     * @param val if it should be set to selected or not
     */
    void setSelected(boolean val) {
        if(val) {
            info.setText(this.placeColor.getNumber() + " - " + this.placeColor.getName() + "\nselected");
        } else {
            info.setText(this.placeColor.getNumber() + " - " + this.placeColor.getName());
        }
    }
}
