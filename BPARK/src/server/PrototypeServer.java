package server;

import ocsf.server.*;
import java.io.IOException;
import java.util.ArrayList;
import DB.DBController;

/**
 * OCSF server that receives client requests and communicates with the database.
 */
public class PrototypeServer extends AbstractServer {

    private DBController db;

    public PrototypeServer(int port) {
        super(port);
        db = new DBController();
    }

    @Override
    protected void serverStarted() {
        db.connectToDB();
        System.out.println("Server started on port " + getPort());
    }

    @Override
    protected void handleMessageFromClient(Object msg, ConnectionToClient client) {
        try {
            if (msg instanceof String) {
                String command = (String) msg;

                if (command.equals("getAllOrders")) {
                    ArrayList<String> orders = db.getAllOrders();
                    client.sendToClient(orders);
                }

            } else if (msg instanceof Object[]) {
                Object[] data = (Object[]) msg;

                if (data.length == 4 && data[0].equals("updateOrder")) {
                    int orderNumber = (int) data[1];
                    String field = (String) data[2];
                    String newValue = (String) data[3];

                    boolean success = db.updateOrderField(orderNumber, field, newValue);
                    client.sendToClient(success ? "Order updated." : "Update failed.");
                }
            }
        } catch (IOException e) {
            System.out.println("Client communication error: " + e.getMessage());
        }
    }
}

