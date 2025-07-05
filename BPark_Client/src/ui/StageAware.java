package ui;

import javafx.stage.Stage;

/**
 * Interface for controllers that want to receive the primary Stage reference.
 * Used for enabling features like window dragging.
 */
public interface StageAware {
    void setStage(Stage stage);
}
