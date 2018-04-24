package viewController;

import java.io.IOException;

import org.opencv.core.Core;

import jade.ShiTomasiAgent;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class ShiTomasiGUI extends Application {
	private static ShiTomasiAgent agent;

	/*
	 * Initialize agent of Shi-Tomasi interface
	 */
	public void setAgent(ShiTomasiAgent agent) {
		ShiTomasiGUI.agent = agent;
	}

	/*
	 * Initialize the Shi-Tomasi GUI
	 */
	@Override
	public void start(Stage primaryStage) throws IOException {
		FXMLLoader loader = new FXMLLoader(getClass().getResource("ShiTomasi.fxml"));
		Parent root = loader.load();
		primaryStage.setTitle("ShiTomasi Agent: " + agent.getLocalName());
		primaryStage.setScene(new Scene(root, 310, 388));
		primaryStage.setResizable(false);
		primaryStage.hide();

		ShiTomasiController controller = loader.getController();
		controller.setAgent(ShiTomasiGUI.agent);
		controller.init();
		primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
			@Override
			public void handle(WindowEvent event) {
				agent.doDelete();
			}
		});
	}

	/*
	 * Launch Shi-Tomasi Agent
	 */
	public static void main(String[] args) {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		launch(args);
	}

	public void dispose() {
		// TODO Auto-generated method stub

	}
}
