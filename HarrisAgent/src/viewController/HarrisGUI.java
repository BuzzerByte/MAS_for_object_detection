package viewController;

import java.io.IOException;

import org.opencv.core.Core;

import jade.HarrisAgent;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class HarrisGUI extends Application {
	private static HarrisAgent agent;

	/*
	 * Initialize agent of Harris interface
	 */
	public void setAgent(HarrisAgent agent) {
		HarrisGUI.agent = agent;
	}

	/*
	 * Initialize the Harris GUI
	 */
	@Override
	public void start(Stage primaryStage) throws IOException {
		FXMLLoader loader = new FXMLLoader(getClass().getResource("Harris.fxml"));
		Parent root = loader.load();
		primaryStage.setTitle("Harris Agent: " + agent.getLocalName());
		primaryStage.setScene(new Scene(root, 310, 388));
		primaryStage.setResizable(false);
		primaryStage.hide();

		HarrisController controller = loader.getController();
		controller.setAgent(HarrisGUI.agent);
		controller.init();
		primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
			@Override
			public void handle(WindowEvent event) {
				agent.doDelete();
			}
		});
	}

	/*
	 * Launch Harris Agent
	 */
	public static void main(String[] args) {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		launch(args);
	}

	public void dispose() {
		// TODO Auto-generated method stub

	}
}
