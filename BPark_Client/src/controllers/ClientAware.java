package controllers;

import client.ClientController;

/**
 * Interface for JavaFX controllers that need access to the ClientController.
 * Classes that implement this interface will receive the ClientController
 * during screen loading and can use it to communicate with the server.
 */
public interface ClientAware {

    /**
     * Injects the active ClientController instance into the implementing class.
     *
     * @param client the client controller used for communication with the server
     */
    void setClient(ClientController client);
}
