package ui;

import javafx.stage.Stage;

/**
 * Interface for controllers that need access to the main Stage.
 * Used for things like making the window draggable.
 */
public interface StageAware {

    /**
     * Passes the main Stage to the controller.
     * Useful for setting up window behavior like dragging.
     *
     * @param stage the primary stage of the application
     */
    void setStage(Stage stage);
}
