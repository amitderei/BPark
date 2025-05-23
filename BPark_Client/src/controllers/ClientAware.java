package controllers;

import client.ClientController;

/**
 * Interface for controllers that need to receive a ClientController instance.
 */
public interface ClientAware {
    void setClient(ClientController client);
}
