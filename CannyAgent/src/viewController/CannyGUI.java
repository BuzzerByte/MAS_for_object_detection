package viewController;

import java.io.IOException;

import org.opencv.core.Core;

import jade.CannyAgent;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class CannyGUI extends Application {

	private static CannyAgent agent;

	/*
	 * Initialize agent of Canny interface
	 */
	public void setAgent(CannyAgent agent) {
		CannyGUI.agent = agent;
	}

	/*
	 * Initialize the Canny GUI
	 */
	@Override
	public void start(Stage primaryStage) throws IOException {
		FXMLLoader loader = new FXMLLoader(getClass().getResource("Canny.fxml"));
		Parent root = loader.load();
		primaryStage.setTitle("Canny Agent: " + agent.getLocalName());
		primaryStage.setScene(new Scene(root, 310, 388));
		primaryStage.setResizable(false);
		primaryStage.hide();

		CannyController controller = loader.getController();
		controller.setAgent(CannyGUI.agent);
		controller.init();
		primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
			@Override
			public void handle(WindowEvent event) {
				agent.doDelete();
			}
		});
	}

	/*
	 * Launch Canny Agent
	 */
	public static void main(String[] args) {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		launch(args);
	}

	public void dispose() {
		// TODO Auto-generated method stub
	}
}
