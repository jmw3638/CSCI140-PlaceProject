package place.client.gui;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
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
    /** the size of a GUI window side */
    private static final int WINDOW_SIDE = 725;
    /** the size of the color selection button height */
    private static final int COLOR_SELECTION_HEIGHT = 25;
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
    /** the scroll pane containing the grid of tiles */
    private ScrollPane scrollPane;
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
        this.scrollPane = new ScrollPane();
        this.colorSelect = new HBox();
    }

    /**
     * Set up and show all GUI elements. Adds this as an observer of the model.
     * Starts the listener for the server connection to allow for network inputs.
     * @param primaryStage the javafx stage
     */
    @Override
    public void start(Stage primaryStage) {
        Scene scene = new Scene(placeWindow, WINDOW_SIDE, WINDOW_SIDE + COLOR_SELECTION_HEIGHT);

        this.placeWindow.setCenter(new ZoomableScrollPane(createTiles()));
        this.placeWindow.setBottom(createColorSelect());
        this.colorSelect.setAlignment(Pos.CENTER);
        this.scrollPane.setContent(tiles);

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
    private Pane createTiles() {
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
                Tile tile = new Tile(this.model.getTiles()[r][c], WINDOW_SIDE / model.getDim());

                tile.setOnMouseClicked(e -> {
                    if(this.selectedColor != null && e.getButton() == MouseButton.PRIMARY) {
                        this.serverConnection.sendTileChange(new PlaceTile(tile.getTile().getRow(), tile.getTile().getCol(), this.username , this.selectedColor));
                    } else {
                        e.consume();
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
     * @return the HBox of ColorSection objects
     */
    private HBox createColorSelect() {
        for(PlaceColor c : PlaceColor.values()){
            ColorSelection colorSelTile = new ColorSelection(c, WINDOW_SIDE / PlaceColor.TOTAL_COLORS, COLOR_SELECTION_HEIGHT);
            colorSelTile.setOnMouseClicked(e -> {
                if(e.getButton() == MouseButton.PRIMARY) {
                    this.selectedColor = colorSelTile.getPlaceColor();
                    for (Node n : colorSelect.getChildren()) {
                        ((ColorSelection) n).setSelected(false);
                    }
                    colorSelTile.setSelected(true);
                } else {
                    e.consume();
                }
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
    public void update(ClientModel model, PlaceTile tile) {
        if(Platform.isFxApplicationThread()) {
            this.refresh(tile);
        }
        else{
            Platform.runLater(() -> refresh(tile));
        }
    }

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

    /**
     * The main method starts the GUI client
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        if (args.length != 3) {
            System.out.println("Usage: java PlaceGUI host port username");
            System.exit(-1);
        } else {
            Application.launch(args);
        }
    }
}

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

class ZoomableScrollPane extends ScrollPane {
    private double scaleValue = 1.0;
    private Node target;
    private Node zoomNode;

    ZoomableScrollPane(Node target) {
        super();
        this.target = target;
        this.zoomNode = new Group(target);
        setContent(outerNode(zoomNode));

        setHbarPolicy(ScrollBarPolicy.NEVER);
        setVbarPolicy(ScrollBarPolicy.NEVER);

        setFitToHeight(true);
        setFitToWidth(true);
        setPannable(true);

        updateScale();
    }

    private Node outerNode(Node node) {
        Node outerNode = centeredNode(node);
        outerNode.setOnScroll(e -> {
            e.consume();
            onScroll(e.getTextDeltaY(), new Point2D(e.getX(), e.getY()));
        });
        outerNode.addEventHandler(MouseEvent.ANY, e -> {
            if(e.getButton() != MouseButton.SECONDARY) { e.consume(); }
        });
        return outerNode;
    }

    private Node centeredNode(Node node) {
        VBox vBox = new VBox(node);
        vBox.setAlignment(Pos.CENTER);
        return vBox;
    }

    private void updateScale() {
        target.setScaleX(scaleValue);
        target.setScaleY(scaleValue);
    }

    private void onScroll(double wheelDelta, Point2D mousePoint) {
        double zoomIntensity = 0.05;
        double zoomFactor = Math.exp(wheelDelta * zoomIntensity);

        Bounds innerBounds = zoomNode.getLayoutBounds();
        Bounds viewportBounds = getLayoutBounds();

        // calculate pixel offsets from [0, 1] range
        double valX = this.getHvalue() * (innerBounds.getWidth() - viewportBounds.getWidth());
        double valY = this.getVvalue() * (innerBounds.getHeight() - viewportBounds.getHeight());

        scaleValue = scaleValue * zoomFactor;
        if(scaleValue < 1.0) { scaleValue = 1.0; }
        if(scaleValue < 10.0) {
            updateScale();
            this.layout(); // refresh ScrollPane scroll positions & target bounds
            // convert target coordinates to zoomTarget coordinates
            Point2D posInZoomTarget = target.parentToLocal(zoomNode.parentToLocal(mousePoint));

            // calculate adjustment of scroll position (pixels)
            Point2D adjustment = target.getLocalToParentTransform().deltaTransform(posInZoomTarget.multiply(zoomFactor - 1));

            // convert back to [0, 1] range
            // (too large/small values are automatically corrected by ScrollPane)
            Bounds updatedInnerBounds = zoomNode.getBoundsInLocal();
            this.setHvalue((valX + adjustment.getX()) / (updatedInnerBounds.getWidth() - viewportBounds.getWidth()));
            this.setVvalue((valY + adjustment.getY()) / (updatedInnerBounds.getHeight() - viewportBounds.getHeight()));
        } else {
            scaleValue = scaleValue / zoomFactor;
        }
    }
}

