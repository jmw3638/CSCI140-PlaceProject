package place.client.gui;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import place.PlaceColor;
import place.PlaceTile;
import place.model.ClientModel;
import place.model.NetworkClient;
import place.model.Observer;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

/**
 * The GUI class for the Place game. Acts as the view in the MVC design pattern.
 * Displays all current place tiles and allows the user to interact with the board.
 *
 * @author Jake Waclawski
 */
public class PlaceGUI extends Application implements Observer<ClientModel, PlaceTile> {
    /** the side length of a GUI place tile on the board */
    private static final int TILE_SIDE_LENGTH = 50;
    /** the side length of the GUI color section tiles */
    private static final int COLOR_SELECT_SIDE_LENGTH = 30;
    /** the connection to the server */
    private NetworkClient serverConnection;
    /** the client socket's username */
    private String username;
    /** the model for the game */
    private ClientModel model;
    /** the current selected color */
    private PlaceColor selectedColor;
    /** the main place window containing all elements */
    private BorderPane placeWindow;
    /** the grid of place tiles */
    private GridPane tiles;
    /** the collection of ColorSection objects */
    private HBox colorSelect;

    /**
     * Create network connection based on command line parameters.
     * Initialize all GUI elements.
     */
    public void init() {
        List<String> args = getParameters().getRaw();
        String host = args.get(0);
        int port = Integer.parseInt(args.get(1));
        this.username = args.get(2);

        this.model = new ClientModel();

        this.serverConnection = new NetworkClient(host, port, this.username, this.model);

        this.selectedColor = null;

        this.placeWindow = new BorderPane();
        this.tiles = new GridPane();
        this.colorSelect = new HBox();
    }

    /**
     * Set up and show all GUI elements. Adds this as an observer of the model.
     * Starts the listener for the server connection to allow for network inputs.
     * @param primaryStage the javafx stage
     */
    @Override
    public void start(Stage primaryStage) {
        Scene scene = new Scene(placeWindow);
        this.placeWindow.setCenter(createTiles());
        this.placeWindow.setBottom(createColorSelect());
        this.colorSelect.setAlignment(Pos.CENTER);

        primaryStage.setScene(scene);
        primaryStage.setTitle("Place: " + username);
        primaryStage.setOnCloseRequest(e -> { serverConnection.shutDown(); });
        primaryStage.show();

        this.model.addObserver(this);
        this.serverConnection.startListener();
    }

    /**
     * Creates all the buttons on the board GridPane. When a button
     * is clicked, send it to the server.
     * @return the GridPane of buttons
     */
    private Pane createTiles(){
        int dim = this.model.getDim();
        RowConstraints rowC = new RowConstraints();
        rowC.setPercentHeight(100.0 / dim);
        ColumnConstraints colC = new ColumnConstraints();
        colC.setPercentWidth(100.0 / dim);

        for(int i = 0; i < dim; i++){
            tiles.getRowConstraints().add(rowC);
        }
        for(int i = 0; i < dim; i++){
            tiles.getColumnConstraints().add(colC);
        }

        for(int r = 0; r < dim; r++){
            for(int c = 0; c < dim; c++){
                Tile tile = new Tile(this.model.getTiles()[r][c], TILE_SIDE_LENGTH);

                tile.setOnMouseClicked(e -> {
                    if(this.selectedColor != null) {
                        this.serverConnection.sendTileChange(new PlaceTile(tile.getTile().getRow(), tile.getTile().getCol(), this.username , this.selectedColor));
                    }
                });

                tiles.add(tile, r, c);
            }
        }
        return tiles;
    }

    /**
     * Creates and returns the collection of ColorSelection objects and stores
     * them into an HBox. When a color is clicked, set it as the
     * selected color.
     * @return the Hbox of ColorSection objects
     */
    private HBox createColorSelect(){
        int i = 0;
        for(PlaceColor c : PlaceColor.values()){
            ColorSelection colorSelTile = new ColorSelection(i, c, COLOR_SELECT_SIDE_LENGTH);
            i++;

            colorSelTile.setOnMouseClicked(e -> {
                this.selectedColor = colorSelTile.getPlaceColor();
                for(Node n : colorSelect.getChildren()) {
                    ((ColorSelection) n).setSelected(false);
                }
                colorSelTile.setSelected(true);
            });

            HBox.setHgrow(colorSelTile, Priority.ALWAYS);
            colorSelect.getChildren().add(colorSelTile);
        }
        return colorSelect;
    }

    /**
     * Request from the client model to update the GUI
     * @param model the client model
     * @param tile the tile to update
     */
    @Override
    public void update(ClientModel model, PlaceTile tile) { refresh(tile); }

    /**
     * Updates the GUI and updates the specified tile
     * @param tile the tile to update
     */
    private void refresh(PlaceTile tile) {
        Objects.requireNonNull(getElement(tile.getRow(), tile.getCol())).setTile(tile);
    }

    /**
     * Gets an element from the grid of tiles
     * @param col the column the tile is located
     * @param row the row the tile is located
     * @return the tile object; null if not found
     */
    private Tile getElement(int row, int col){
        for(Node n : tiles.getChildren()){
            if(GridPane.getRowIndex(n) == col && GridPane.getColumnIndex(n) == row){
                return (Tile) n;
            }
        }
        return null;
    }

    public static void main(String[] args) {
        if (args.length != 3) {
            System.out.println("Usage: java PlaceGUI host port username");
            System.exit(-1);
        } else {
            Application.launch(args);
        }
    }
}

class ColorSelection extends Rectangle implements Serializable {
    private PlaceColor placeColor;
    private Tooltip info;
    private int num;

    ColorSelection(int num, PlaceColor placeColor, int side){
        this.num = num;
        this.placeColor = placeColor;
        this.setWidth(side);
        this.setHeight(side);
        this.setFill(Color.rgb(
                this.placeColor.getRed(),
                this.placeColor.getGreen(),
                this.placeColor.getBlue()));
        info = new Tooltip(num + " - " + this.getPlaceColor().getName());

        Tooltip.install(this, info);
    }

    PlaceColor getPlaceColor() { return this.placeColor; }

    void setSelected(boolean val) {
        if(val) {
            info.setText(num + " - " + this.getPlaceColor().getName() + "\nselected");
        } else {
            info.setText(num + " - " + this.getPlaceColor().getName());
        }
    }
}

