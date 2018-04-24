package viewController;

import java.io.IOException;

import org.opencv.core.Core;

import jade.CommunicationAgent;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class CommunicationGUI extends Application {
	private static CommunicationAgent agent;

	/*
	 * Initialize agent of Communication interface
	 */
	public void setAgent(CommunicationAgent agent) {
		CommunicationGUI.agent = agent;
	}

	/*
	 * Initialize the Communication GUI
	 */
	@Override
	public void start(Stage primaryStage) throws IOException {
		FXMLLoader loader = new FXMLLoader(getClass().getResource("Communication.fxml"));
		Parent root = loader.load();
		primaryStage.setTitle("Communication Agent: " + agent.getLocalName());
		primaryStage.setScene(new Scene(root, 310, 388));
		primaryStage.setResizable(false);
		primaryStage.hide();

		CommunicationController controller = loader.getController();
		controller.setAgent(CommunicationGUI.agent);
		controller.init();
		primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
			@Override
			public void handle(WindowEvent event) {
				agent.doDelete();
			}
		});
	}

	/*
	 * Launch Communication Agent
	 */
	public static void main(String[] args) {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		launch(args);
	}

	public void dispose() {
		// TODO Auto-generated method stub
	}
}
