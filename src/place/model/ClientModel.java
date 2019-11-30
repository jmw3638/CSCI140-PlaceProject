package place.model;

import place.PlaceBoard;
import place.PlaceColor;
import place.PlaceTile;

import java.util.LinkedList;
import java.util.List;

/**
 * The client side model that is used as the "M" in the MVC paradigm.  All client
 * side applications (PTUI, GUI, bots) are observers of this model.
 *
 * @author Sean Strout @ RIT CS
 * @author Jake Waclawski
 */
public class ClientModel {
    /** observers of the model (PlacePTUI and PlaceGUI - the "views") */
    private List<Observer<ClientModel, PlaceTile>> observers = new LinkedList<>();
    /** the actual board of tiles */
    private static PlaceTile[][] tiles;

    /**
     * Add a new observer.
     * @param observer the new observer
     */
    public void addObserver(Observer<ClientModel, PlaceTile> observer) {
        this.observers.add(observer);
    }

    /**
     * Notify observers the model has changed.
     * @param tile the changed tile
     */
    private void notifyObservers(PlaceTile tile){
        for (Observer<ClientModel, PlaceTile> observer: observers) {
            observer.update(this, tile);
        }
    }

    /**
     * Initialize the board for the newly connected client.
     * @param board the place board
     */
    static void initBoard(PlaceBoard board) {
        tiles = new PlaceTile[board.DIM][board.DIM];
        for(int r = 0; r < tiles.length; r++){
            for(int c = 0; c < tiles[0].length; c++){
                tiles[r][c] = board.getTile(r, c);
            }
        }
    }

    /**
     * Determines if a tile change request is valid. A change is only
     * valid if the row and column are within the dimensions of the
     * place board and if the color is a valid place color.
     * @param row the row to check
     * @param col the column to check
     * @param color the color to check
     * @return if the change request is valid
     */
    boolean isValidChange(int row, int col, int color) {
        return (row >= 0 && row < getDim()) && (col >= 0 && col < getDim() && (color >= 0 && color < PlaceColor.TOTAL_COLORS));
    }

    /**
     * Change a tile on the client side board.
     * @param tile the tile to change
     */
    void tileChanged(PlaceTile tile) {
        tiles[tile.getRow()][tile.getCol()] = tile;
        notifyObservers(tile);
    }

    /**
     * Get the matrix of place tiles.
     * @return the tiles
     */
    public PlaceTile[][] getTiles() { return tiles; }

    /**
     * Get the dimensions of the board.
     * @return the dimension
     */
    public int getDim() { return tiles.length; }
}
