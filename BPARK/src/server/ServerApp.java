package server;

/**
 * Entry point to start the server.
 */
public class ServerApp {
    public static void main(String[] args) {
        PrototypeServer server = new PrototypeServer(5555);
        try {
            server.listen();
        } catch (Exception e) {
            System.out.println("Server error: " + e.getMessage());
        }
    }
}
