package viewController;

import jade.PreProcessingAgent;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import model.AgentModel;
import model.MessageModel;

public class ACPController {
	@FXML
	public CheckBox sys_recomm_check;
	@FXML
	public CheckBox enable_pp_check;
	@FXML
	public CheckBox canny_check;
	@FXML
	public CheckBox harris_check;
	@FXML
	public CheckBox shi_tomasi_check;
	@FXML
	public CheckBox select_all_check;
	@FXML
	public TextField min_match;
	@FXML
	public Button btn_save;
	@FXML
	public ListView<String> list_agents = new ListView<String>();
	public static ObservableList<String> data = FXCollections.observableArrayList("Pre-processing agent activated");
	private static PreProcessingAgent agent;
	private static String min_accurate_val;

	/*
	 * Set the agent for agent control panel controller
	 */
	public void setAgent(PreProcessingAgent agent) {
		ACPController.agent = agent;
	}

	/*
	 * Initialize controller for pre-processing agent
	 */
	public void init() {
		agent.setController(this);
		list_agents.setItems(data);
		min_match.setText("0");
		setMinMatch(min_match.getText());
		MessageModel.setMessage("none");
	}

	public void updateList(String newAgent) {
		try {
			data.add(newAgent);
			list_agents.setItems(data);
		} catch (java.lang.IllegalStateException e) {

		}
	}

	/*
	 * Configure user options when check box system recommend is checked
	 */
	@FXML
	public void sys_recomm() {
		if (this.sys_recomm_check.isSelected()) {
			this.canny_check.setDisable(true);
			this.harris_check.setDisable(true);
			this.shi_tomasi_check.setDisable(true);
			this.select_all_check.setDisable(true);
			MessageModel.setMessage("recomm");
			AgentModel.setSysRecomm(true);
		} else {
			MessageModel.setMessage("none");
			this.canny_check.setDisable(false);
			this.harris_check.setDisable(false);
			this.shi_tomasi_check.setDisable(false);
			this.select_all_check.setDisable(false);
			AgentModel.setSysRecomm(false);
		}
	}

	/*
	 * Configure user options when check box select all is checked
	 */
	@FXML
	public void select_all() {
		if (this.select_all_check.isSelected()) {
			this.canny_check.setDisable(true);
			this.harris_check.setDisable(true);
			this.shi_tomasi_check.setDisable(true);
			AgentModel.setCanny(true);
			AgentModel.setHarris(true);
			AgentModel.setShiTomasi(true);
			MessageModel.setMessage("HSC");
		} else {
			this.canny_check.setDisable(false);
			this.harris_check.setDisable(false);
			this.shi_tomasi_check.setDisable(false);
			AgentModel.setCanny(false);
			AgentModel.setHarris(false);
			AgentModel.setShiTomasi(false);
			MessageModel.setMessage("none");
		}
	}

	/*
	 * Configure user options when check box pre processing is checked
	 */
	@FXML
	public void enable_pp() {
		if (this.enable_pp_check.isSelected()) {
			AgentModel.setPP(true);
		} else {
			AgentModel.setPP(false);
		}
	}

	/*
	 * Configure user option when check box Canny is checked
	 */
	@FXML
	public void canny_ag() {
		config_checkbox();
	}

	/*
	 * Configure user option when check box Harris is checked
	 */
	@FXML
	public void harris_ag() {
		config_checkbox();
	}

	/*
	 * Configure user option when check box Shi Tomasi is checked
	 */
	@FXML
	public void shi_tomasi_ag() {
		config_checkbox();
	}

	@FXML
	public void save_setting() {
		setMinMatch(min_match.getText());
		Alert alert = new Alert(AlertType.INFORMATION, "Configuration saved");
		alert.show();
	}

	/*
	 * Function used to set true and false for every check box
	 */
	private void config_checkbox() {
		// TODO Auto-generated method stub
		if (this.shi_tomasi_check.isSelected()) {
			System.out.println("shi-tomasi");
			AgentModel.setShiTomasi(true);
			AgentModel.setCanny(false);
			AgentModel.setShiTomasi(false);
			MessageModel.setMessage("shi_tomasi");
			this.select_all_check.setDisable(true);
		} else if (this.harris_check.isSelected()) {
			System.out.println("harris");
			AgentModel.setHarris(true);
			AgentModel.setCanny(false);
			AgentModel.setShiTomasi(false);
			MessageModel.setMessage("harris");
			this.select_all_check.setDisable(true);
		} else if (this.canny_check.isSelected()) {
			System.out.println("canny");
			AgentModel.setCanny(true);
			AgentModel.setHarris(false);
			AgentModel.setShiTomasi(false);
			MessageModel.setMessage("canny");
			this.select_all_check.setDisable(true);
		}
		if ((this.canny_check.isSelected() && this.harris_check.isSelected())) {
			System.out.println("canny and harris");
			AgentModel.setCanny(true);
			AgentModel.setHarris(true);
			AgentModel.setShiTomasi(false);
			MessageModel.setMessage("HC");
			this.select_all_check.setDisable(true);
		}
		if ((this.harris_check.isSelected() && this.shi_tomasi_check.isSelected())) {
			System.out.println("harris and shit_tomasi");
			AgentModel.setCanny(false);
			AgentModel.setHarris(true);
			AgentModel.setShiTomasi(true);
			MessageModel.setMessage("HS");
			this.select_all_check.setDisable(true);
		}
		if ((this.canny_check.isSelected() && this.shi_tomasi_check.isSelected())) {
			System.out.println("canny and shi-tomasi");
			AgentModel.setCanny(true);
			AgentModel.setHarris(false);
			AgentModel.setShiTomasi(true);
			MessageModel.setMessage("SC");
			this.select_all_check.setDisable(true);
		}
		if (this.canny_check.isSelected() && this.harris_check.isSelected() && this.shi_tomasi_check.isSelected()) {
			System.out.println("canny_harris_shittomasi");
			AgentModel.setCanny(true);
			AgentModel.setHarris(true);
			AgentModel.setShiTomasi(true);
			MessageModel.setMessage("HSC");
			this.select_all_check.setDisable(true);
		}
		if (!this.canny_check.isSelected() && !this.harris_check.isSelected() && !this.shi_tomasi_check.isSelected()) {
			System.out.println("nothing are selected");
			AgentModel.setCanny(true);
			AgentModel.setHarris(true);
			AgentModel.setShiTomasi(true);
			MessageModel.setMessage("none");
			this.select_all_check.setDisable(false);
		}
	}

	/*
	 * Set the accuracy of object detected
	 */
	public static void setMinMatch(String min_match) {
		ACPController.min_accurate_val = min_match;
	}

	/*
	 * Get the accuracy of object detected
	 */
	public static String getMinMatch() {
		return ACPController.min_accurate_val;
	}
}
