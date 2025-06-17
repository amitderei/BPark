package controllers;

import client.ClientController;

/**
 * Marker for any JavaFX controller that needs a reference to the
 * central ClientController so it can communicate with the server.
 *
 * Implementations get the reference during their initialization
 * and keep it for later use.
 */
public interface ClientAware {

    /**
     * Injects the shared ClientController instance.
     *
     * @param client the connected client controller
     */
    void setClient(ClientController client);
}
