package place.client.gui;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import place.PlaceColor;
import place.PlaceTile;
import place.model.ClientModel;
import place.model.Observer;
import place.network.PlaceRequest;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Socket;
import java.util.List;
import java.util.Random;

public class PlaceGUI extends Application implements Observer<ClientModel, PlaceTile> {
    /** the side length of a GUI place tile on the board */
    private static final int TILE_SIDE_LENGTH = 50;
    /** the side length of the GUI color section tiles */
    private static final int COLOR_SELECT_SIDE_LENGTH = 30;
    /** the client socket which expects a host and port */
    private static Socket clientSocket;
    /** the incoming connection from the server */
    private static ObjectInputStream networkIn;
    /** the outgoing connection to the server */
    private static ObjectOutputStream networkOut;
    /** the client socket's username */
    private static String username;
    /** the model for the game */
    private ClientModel model;

    private Scene scene;
    private BorderPane gameWindow;
    private GridPane tiles;
    private HBox colorSelect;
    private BorderPane tileInfo;
    private Label tilePos;
    private Label tileUser;
    private Label tileTime;

    /**
     * Create network connection based on command line parameters
     */
    public void init() {
        List<String> args = getParameters().getRaw();
        String host = args.get(0);
        int port = Integer.parseInt(args.get(1));
        String username = args.get(2);

        createClient(host, port, username);

        this.model = new ClientModel();

        this.gameWindow = new BorderPane();
        this.tiles = new GridPane();
        this.colorSelect = new HBox();
        this.tileInfo = new BorderPane();
        this.tilePos = new Label();
        this.tileUser = new Label();
        this.tileTime = new Label();
    }

    /**
     * Creates the client and connects it to the server socket
     * @param host the host ip to connect to
     * @param port the host port to connect to
     * @param user the username of the client
     */
    private static void createClient(String host, int port, String user) {
        try {
            clientSocket = new Socket(host, port);
            username = user;

            networkOut = new ObjectOutputStream(clientSocket.getOutputStream());
            networkIn = new ObjectInputStream(clientSocket.getInputStream());

            networkOut.writeObject(new PlaceRequest<>(PlaceRequest.RequestType.LOGIN, user));
            System.out.println("Connecting to server");
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        this.scene = new Scene(gameWindow);
        this.gameWindow.setTop(tileInfo);
        this.gameWindow.setCenter(createTiles());
        this.gameWindow.setBottom(createColorSelect());
        this.colorSelect.setAlignment(Pos.CENTER);
        this.tileInfo.setLeft(tileUser);
        this.tileInfo.setCenter(tilePos);
        this.tileInfo.setRight(tileTime);

        primaryStage.setScene(scene);
        primaryStage.setTitle("Place: " + username);
        primaryStage.show();

        this.model.addObserver(this);
    }

    /**
     * Creates all the buttons on the board GridPane
     * @return the GridPane of buttons
     */
    private Pane createTiles(){
        int dim = model.getDIM();
        /** REMOVE */ dim = 10;
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
                Tile tile = new Tile(r, c, TILE_SIDE_LENGTH);

                tile.setOnMouseClicked(e -> {
                    try {
                        networkOut.writeUnshared(new PlaceRequest<Tile>(PlaceRequest.RequestType.CHANGE_TILE, tile));
                        networkOut.flush();
                        System.out.println(tile.getRow() + " " + tile.getCol() + " sent");
                    } catch (IOException ex) {
                        System.err.println(ex.getMessage());
                        System.exit(1);
                    }
                });
                tile.setOnMouseEntered(e -> {
                    tileUser.setText(tile.getTile().getOwner());
                    tilePos.setText("(" + tile.getRow() + "," + tile.getCol() + ")");
                    tileTime.setText(tile.getTile().getTime() + "ms");
                });
                tile.setOnMouseExited(e -> {
                    tileUser.setText("");
                    tilePos.setText("");
                    tileTime.setText("");
                });

                tiles.add(tile, r, c);
            }
        }
        return tiles;
    }

    private HBox createColorSelect(){
        int i = 0;
        for(PlaceColor c : PlaceColor.values()){
            ColorSelection colorSelTile = new ColorSelection(i, c, COLOR_SELECT_SIDE_LENGTH);
            i++;

            colorSelTile.setOnMouseClicked(e -> {
                System.out.println("Selected " + colorSelTile.getPlaceColor().getName());
            });
            colorSelTile.setOnMouseEntered(e -> {
                tilePos.setText(colorSelTile.getPlaceColor().getName());
            });
            colorSelTile.setOnMouseExited(e -> {
                tilePos.setText("");
            });

            HBox.setHgrow(colorSelTile, Priority.ALWAYS);
            colorSelect.getChildren().add(colorSelTile);
        }
        return colorSelect;
    }

    @Override
    public void update(ClientModel model, PlaceTile tile) {
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
    private int num;

    ColorSelection(int num, PlaceColor placeColor, int side){
        this.placeColor = placeColor;
        this.setWidth(side);
        this.setHeight(side);
        this.setAccessibleText("" + num);
        this.setFill(Color.rgb(
                this.placeColor.getRed(),
                this.placeColor.getGreen(),
                this.placeColor.getBlue()));
    }

    int getNum() { return this.num; }

    PlaceColor getPlaceColor() { return this.placeColor; }

    Color getColor() {
        return Color.rgb(
                this.placeColor.getRed(),
                this.placeColor.getGreen(),
                this.placeColor.getBlue());
    }
}

