package ui;

import javafx.scene.Node;
import javafx.stage.Stage;

/**
 * Utility class for enabling drag-to-move functionality
 * for undecorated JavaFX windows.
 */
public class DragUtilClient {

    /** Stores the horizontal distance between the mouse and the window when dragging starts */
    private static double xOffset = 0;

    /** Stores the vertical distance between the mouse and the window when dragging starts */
    private static double yOffset = 0;


	/**
	 * Enables window dragging using the specified node as the drag handle.
	 *
	 * @param dragHandle the node (e.g., ToolBar, AnchorPane) the user will drag
	 * @param stage      the Stage to move
	 */
	public static void enableDrag(Node dragHandle, Stage stage) {
		// Drag event: record initial offset when mouse is pressed
		dragHandle.setOnMousePressed(event -> {
			xOffset = event.getSceneX();
			yOffset = event.getSceneY();
		});

		// Drag event: move the stage when mouse is dragged
		dragHandle.setOnMouseDragged(event -> {
			stage.setX(event.getScreenX() - xOffset);
			stage.setY(event.getScreenY() - yOffset);
		});
	}
}
