package client;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import ocsf.client.AbstractClient;
import common.Order;

/**
 * Console-based client for testing the BPARK prototype without JavaFX.
 */
public class ClientConsole extends AbstractClient {

    public ClientConsole(String host, int port) {
        super(host, port);
    }

    @Override
    protected void handleMessageFromServer(Object msg) {
        if (msg instanceof String) {
            System.out.println("[SERVER] " + msg);
        } else if (msg instanceof ArrayList<?>) {
            ArrayList<?> list = (ArrayList<?>) msg;
            if (!list.isEmpty() && list.get(0) instanceof Order) {
                System.out.println("=== All Orders ===");
                ArrayList<Order> orders = (ArrayList<Order>) msg;
                for (Order order : orders) {
                    System.out.println(order);
                }
            } else {
                System.out.println("Received unexpected list from server.");
            }
        }
    }

    public void runConsole() {
        try {
            openConnection();
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

            while (true) {
                System.out.println("\nCommands: [get] orders | [update] order_number field new_value | [exit]");
                System.out.print("> ");
                String input = reader.readLine();
                if (input == null) continue;

                String[] parts = input.trim().split(" ");
                if (parts[0].equalsIgnoreCase("get")) {
                    sendToServer("getAllOrders");
                } else if (parts[0].equalsIgnoreCase("update") && parts.length == 4) {
                    int orderNum = Integer.parseInt(parts[1]);
                    String field = parts[2];
                    String newVal = parts[3];
                    sendToServer(new Object[]{"updateOrder", orderNum, field, newVal});
                } else if (parts[0].equalsIgnoreCase("exit")) {
                    closeConnection();
                    break;
                } else {
                    System.out.println("Invalid command.");
                }
            }

        } catch (Exception e) {
            System.out.println("Console error: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        ClientConsole client = new ClientConsole("localhost", 5555);
        client.runConsole();
    }
}
