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

public class ACP extends Stage {

	/*
	 * Initialize agent control panel interface
	 */
	public ACP(PreProcessingAgent agent) throws IOException {
		// TODO Auto-generated constructor stub
		FXMLLoader loader = new FXMLLoader(getClass().getResource("AgentControl.fxml"));
		Parent root = loader.load();
		this.getIcons().add(new Image("file:image/control_panel.png"));
		this.setTitle("Agent Control Panel");
		this.setScene(new Scene(root, 450, 300));
		this.setResizable(false);
		this.show();

		ACPController controller = loader.getController();
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
