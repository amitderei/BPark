package controllers;

import client.ClientController;

/**
 * Controller for the Subscriber home screen.
 */
public class SubscriberHomeController implements ClientAware {

    private ClientController client;

    /**
     * Sets the client controller.
     *
     * @param client the connected client
     */
    @Override
    public void setClient(ClientController client) {
        this.client = client;
    }

    // Future methods for subscriber functionality can be added here.
}
