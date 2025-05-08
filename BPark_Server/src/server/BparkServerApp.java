package server;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Entry point for launching the BPARK server application.
 * This class is responsible for starting the server, binding it to a port,
 * and initializing the listening process using the OCSF framework.
 */
public class BparkServerApp extends Application {

    /**
     * Main method – the starting point of the server application.
     * Creates an instance of BparkServer bound to port 5555 and starts listening for client connections.
     *
     * @param args Command-line arguments (not used in this application).
     */
   /* public static void main(String[] args) {
        // Create a new server instance bound to port 5555
        BparkServer server = new BparkServer(5555);

        try {
            // Start the OCSF server to begin listening for incoming client connections
            server.listen();
        } catch (Exception e) {
            // Print an error message if the server fails to start
            System.out.println("Server error: " + e.getMessage());
        }
               
    }*/
	
	public static void main( String args[] ) throws Exception
	   {   
		 launch(args);
	  } 
    
	public static void runServer(String p)
	{
		 int port = 0; //Port to listen on

	        try
	        {
	        	port = Integer.parseInt(p); //Set port to 5555
	          
	        }
	        catch(Throwable t)
	        {
	        	System.out.println("ERROR - Could not connect!");
	        }
	    	
	        BparkServer sv = new BparkServer(port);
	        
	        try 
	        {
	          sv.listen(); //Start listening for connections
	        } 
	        catch (Exception e) 
	        {
	          System.out.println("Server error: " + e.getMessage());
	        }
	}
	
	public void start(Stage primaryStage) throws Exception {	
		System.out.println(getClass().getResource("/server/ConnectionToServerView.fxml"));
		Parent root = FXMLLoader.load(getClass().getResource("/server/ConnectionToServerView.fxml"));
				
		Scene scene = new Scene(root);
		primaryStage.setTitle("BPark server");
		primaryStage.setScene(scene);
		primaryStage.show();		
	}
}
