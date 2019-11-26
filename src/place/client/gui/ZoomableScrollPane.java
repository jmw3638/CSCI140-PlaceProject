package place.client.gui;

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
 * Represents a JavaFX ScrollPane that is zoomable
 */
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
