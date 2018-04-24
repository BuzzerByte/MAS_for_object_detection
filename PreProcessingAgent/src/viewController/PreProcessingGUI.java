package viewController;

import java.io.IOException;

import org.opencv.core.Core;

import jade.PreProcessingAgent;
import javafx.application.Application;
import javafx.stage.Stage;

public class PreProcessingGUI extends Application {
	private static PreProcessingAgent agent;

	/*
	 * Set agent for pre-processing GUI
	 */
	public void setAgent(PreProcessingAgent agent) {
		PreProcessingGUI.agent = agent;
	}

	/*
	 * Initialize the Pre-processing GUI
	 */
	@Override
	public void start(Stage primaryStage) throws IOException {
		new UserInterface(agent);
		new ACP(agent);
	}

	/*
	 * Launch Pre-Processing Agent
	 */
	public static void main(String[] args) {
		// load the native OpenCV library
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		launch(args);
	}

	public void dispose() {
		// TODO Auto-generated method stub

	}
}
