package place.client.gui.elements;

import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;

/**
 * Represents a JavaFX ScrollPane that is also zoomable.
 *
 * @author Jake Waclawski
 */
public class ZoomableScrollPane extends ScrollPane {
    private double scaleValue = 1.0;
    /** the target ScrollPane node to make zoomable */
    private Node target;
    /** the node to group with the target node */
    private Node zoomNode;
    /** the dimension of the GUI board */
    private int DIM;

    /**
     * Create a new zoomable ScrollPane.
     * @param target the ScrollPane
     * @param DIM the dimension of GUI board
     */
    public ZoomableScrollPane(Node target, int DIM) {
        super();
        this.target = target;
        this.zoomNode = new Group(target);
        this.DIM = DIM;

        setContent(outerNode(zoomNode));

        setHbarPolicy(ScrollBarPolicy.NEVER);
        setVbarPolicy(ScrollBarPolicy.NEVER);

        setFitToHeight(true);
        setFitToWidth(true);
        setPannable(true);

        updateScale();
    }

    /**
     * Create the outer node.
     * @param node the node
     * @return the outer node
     */
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

    /**
     * Create the centered node.
     * @param node the node
     * @return the centered node
     */
    private Node centeredNode(Node node) {
        VBox vBox = new VBox(node);
        vBox.setAlignment(Pos.CENTER);
        return vBox;
    }

    /**
     * Update the current scale value of the ScrollPane.
     */
    private void updateScale() {
        target.setScaleX(scaleValue);
        target.setScaleY(scaleValue);
    }

    /**
     * Handle zooming upon a mouse scroll event.
     * @param wheelDelta the mouse scroll data
     * @param mousePoint the x, y position of the cursor
     */
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
        if(scaleValue < (this.DIM / 10.0)) {
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
