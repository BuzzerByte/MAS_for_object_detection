package viewController;

import java.io.IOException;

import jade.PreProcessingAgent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class UserInterface extends Stage {

	/*
	 * Initialize user interface
	 */
	public UserInterface(PreProcessingAgent agent) throws IOException {
		// TODO Auto-generated constructor stub
		FXMLLoader loader = new FXMLLoader(getClass().getResource("UserInterface.fxml"));
		Parent root = loader.load();
		this.getIcons().add(new Image("file:image/user.png"));
		this.setTitle("User Interface");
		this.setScene(new Scene(root, 585, 420));
		this.setResizable(false);
		this.show();

		UIController controller = loader.getController();
		controller.setAgent(agent);
		controller.init();
		this.setOnCloseRequest(new EventHandler<WindowEvent>() {
			@Override
			public void handle(WindowEvent event) {
				agent.doDelete();
			}
		});
	}
}
