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
 */
public class ClientModel {
    /** observers of the model (PlacePTUI and PlaceGUI - the "views") */
    private List<Observer<ClientModel, PlaceTile>> observers = new LinkedList<>();
    /** the actual board of tiles */
    private static PlaceTile[][] tiles;

    /**
     * Add a new observer.
     *
     * @param observer the new observer
     */
    public void addObserver(Observer<ClientModel, PlaceTile> observer) {
        this.observers.add(observer);
    }

    /**
     * Notify observers the model has changed.
     */
    private void notifyObservers(PlaceTile tile){
        for (Observer<ClientModel, PlaceTile> observer: observers) {
            observer.update(this, tile);
        }
    }

    static void initBoard(PlaceBoard board) {
        tiles = new PlaceTile[board.DIM][board.DIM];
        for(int r = 0; r < tiles.length; r++){
            for(int c = 0; c < tiles[0].length; c++){
                tiles[r][c] = board.getTile(r, c);
            }
        }
    }

    void tileChanged(PlaceTile tile) {
        tiles[tile.getRow()][tile.getCol()] = tile;
        notifyObservers(tile);
    }

    public PlaceTile[][] getTiles() { return tiles; }

    public int getDim() { return tiles.length; }
}
