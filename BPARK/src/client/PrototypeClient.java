package client;

import ocsf.client.AbstractClient;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * Prototype client using console interaction only.
 */
public class PrototypeClient extends AbstractClient {

    private Scanner scanner = new Scanner(System.in);

    public PrototypeClient(String host, int port) {
        super(host, port);
    }

    @Override
    protected void handleMessageFromServer(Object msg) {
        if (msg instanceof String) {
            System.out.println("[SERVER] " + msg);
        } else if (msg instanceof ArrayList) {
            System.out.println("=== Orders ===");
            for (String order : (ArrayList<String>) msg) {
                System.out.println(order);
            }
        }
    }

    /**
     * Send request to fetch all orders from server.
     */
    public void requestAllOrders() {
        try {
            sendToServer("getAllOrders");
        } catch (IOException e) {
            System.out.println("Error sending request: " + e.getMessage());
        }
    }

    /**
     * Send request to update parking_space.
     */
    public void updateParkingSpace(int orderNumber, int newParking) {
        try {
            sendToServer(new Object[]{"updateOrder", orderNumber, "parking_space", String.valueOf(newParking)});
        } catch (IOException e) {
            System.out.println("Error sending update: " + e.getMessage());
        }
    }

    /**
     * Send request to update order_date.
     */
    public void updateOrderDate(int orderNumber, String newDate) {
        try {
            sendToServer(new Object[]{"updateOrder", orderNumber, "order_date", newDate});
        } catch (IOException e) {
            System.out.println("Error sending update: " + e.getMessage());
        }
    }

    /**
     * Run basic console interface.
     */
    public void runConsole() {
        try {
            openConnection();
            while (true) {
                System.out.println("\nOptions: [get] all orders | [update_date] | [update_parking] | [exit]");
                System.out.print("> ");
                String input = scanner.nextLine().trim();

                switch (input) {
                    case "get":
                        requestAllOrders();
                        break;
                    case "update_date":
                        System.out.print("Enter order number: ");
                        int orderId1 = Integer.parseInt(scanner.nextLine());
                        System.out.print("Enter new date (yyyy-MM-dd): ");
                        String newDate = scanner.nextLine();
                        updateOrderDate(orderId1, newDate);
                        break;
                    case "update_parking":
                        System.out.print("Enter order number: ");
                        int orderId2 = Integer.parseInt(scanner.nextLine());
                        System.out.print("Enter new parking spot: ");
                        int newSpot = Integer.parseInt(scanner.nextLine());
                        updateParkingSpace(orderId2, newSpot);
                        break;
                    case "exit":
                        closeConnection();
                        return;
                    default:
                        System.out.println("Unknown command.");
                }
            }
        } catch (Exception e) {
            System.out.println("Client error: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        PrototypeClient client = new PrototypeClient("localhost", 5555);
        client.runConsole();
    }
}
