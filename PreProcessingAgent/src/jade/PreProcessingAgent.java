package jade;

import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import javafx.scene.image.Image;
import javafx.scene.text.Font;
import model.DirectoryModel;
import model.ImageModel;
import model.MessageModel;
import model.ObjectModel;
import viewController.ACPController;
import viewController.PreProcessingGUI;
import viewController.UIController;
import viewController.Utils;

@SuppressWarnings("serial")
public class PreProcessingAgent extends Agent {
	private UIController controller;
	private static PreProcessingGUI gui;
	private double start_time;
	private double end_time;
	private double time_taken;

	/*
	 * Get the agent controller
	 */
	public UIController getController() {
		return controller;
	}

	/*
	 * Set the user interface controller
	 */
	public void setController(UIController controller) {
		this.controller = controller;
	}

	/*
	 * Set the agent control panel controller
	 */
	public void setController(ACPController acpController) {
	}

	/*
	 * Setup agent, GUI, and initialize agent communication
	 */
	protected void setup() {
		gui = new PreProcessingGUI();
		gui.setAgent(this);
		new Thread() {
			@Override
			public void run() {
				PreProcessingGUI.main(null);
			}
		}.start();
		receiveResult();
		check_available_agents();
	}

	public void check_available_agents() {
		// TODO Auto-generated method stub
		addBehaviour(new CyclicBehaviour(this) {
			@Override
			public void action() {
				// TODO Auto-generated method stub
				MessageTemplate mt = MessageTemplate.MatchConversationId("availability");
				ACLMessage msg = receive(mt);
				if (msg != null) {
					String content = msg.getContent();
					if (content.equals("Communication agent activated")) {
						try {
							// acpController.updateList(content);
						} catch (java.lang.IllegalStateException e) {

						}
					} else if (content.equals("Canny agent activated")) {
						try {
							// acpController.updateList(content);
						} catch (java.lang.IllegalStateException e) {

						}
					} else if (content.equals("Harris agent activated")) {
						try {
							// acpController.updateList(content);
						} catch (java.lang.IllegalStateException e) {

						}
					} else if (content.equals("Shi-Tomasi agent activated")) {
						try {
							// acpController.updateList(content);
						} catch (java.lang.IllegalStateException e) {

						}
					}
				} else {
					block();
				}
			}
		});
	}

	/*
	 * Put agent clean-up operations here
	 */
	protected void takeDown() {
		gui.dispose();
		System.out.println("Pre-Processing agent " + getAID().getName() + " terminating.");
	}

	/*
	 * Send image information to communication agent
	 */
	public void sendImageInfo() {
		addBehaviour(new OneShotBehaviour(this) {

			@Override
			public void action() {
				// TODO Auto-generated method stub
				ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
				msg.addReceiver(new AID("CommunicationAgent", AID.ISLOCALNAME));
				start_time = System.currentTimeMillis();
				if (UIController.dataset_selected) {
					String img_info = ObjectModel.get_path() + "-" + ObjectModel.get_file_name() + "-"
							+ MessageModel.getMessage() + "-" + DirectoryModel.getDir();
					System.out.println(img_info);
					msg.setContent(img_info);
				} else if (!UIController.dataset_selected) {
					String img_info = ObjectModel.get_path() + "-" + ObjectModel.get_file_name() + "-"
							+ MessageModel.getMessage() + "-" + String.valueOf(ImageModel.getRow()) + "-"
							+ String.valueOf(ImageModel.getCol()) + "-" + String.valueOf(ImageModel.getType() + "-"
									+ ImageModel.get_path() + "-" + ImageModel.get_file_name());
					System.out.println(img_info);
					msg.setContent(img_info);
				}
				msg.setConversationId("img_info");
				send(msg);
				System.out.println("Image information send");
			}
		});
	}

	/*
	 * receiving processed result image from communication agent
	 */
	private void receiveResult() {
		addBehaviour(new CyclicBehaviour(this) {
			@Override
			public void action() {

				MessageTemplate mt = MessageTemplate.MatchConversationId("processed_result");
				ACLMessage msg = receive(mt);
				if (msg != null) {
					String msg_info = msg.getContent();
					String msg_info_array[] = msg_info.split("-");
					String hybrid_img = msg_info_array[msg_info_array.length - 1];
					String accuracy = msg_info_array[msg_info_array.length - 2];
					end_time = System.currentTimeMillis();
					time_taken = end_time - start_time;
					controller.txt_time.setText(String.valueOf((double) (time_taken / 1000) % 60) + "(s)");
					controller.status.setText("Processing done");
					if (UIController.dataset_selected) {
						Mat mat_img = Imgcodecs.imread(DirectoryModel.getDir() + "/results/"
								+ ObjectModel.get_file_name() + hybrid_img + ".jpg");
						Image result_img = Utils.mat2Image(mat_img);
						UIController.updateImageView(controller.morpho_img, result_img);
						controller.txt_accuracy.setFont(Font.font("Verdana", 15));
						controller.txt_accuracy.setText(accuracy);
					} else if (!UIController.dataset_selected) {
						Mat mat_img = Imgcodecs.imread(
								ImageModel.get_path() + "/results/" + ImageModel.get_file_name() + hybrid_img + ".jpg");
						Image result_img = Utils.mat2Image(mat_img);
						UIController.updateImageView(controller.morpho_img, result_img);
						if (Double.valueOf(accuracy) >= Double.valueOf(ACPController.getMinMatch())) {
							controller.txt_accuracy.setStyle("-fx-text-inner-color: green;");
						} else {
							controller.txt_accuracy.setStyle("-fx-text-inner-color: red;");
						}
						controller.txt_accuracy.setFont(Font.font("Verdana", 15));
						double dacc = Double.valueOf(accuracy);
						if (Double.valueOf(dacc) > 100) {
							dacc = modulus(dacc);
						}
						controller.txt_accuracy.setText(String.format("%.2f", Double.valueOf(dacc)) + "%");
					}
				} else {
					block();
				}
			}

			private double modulus(double dacc) {
				// TODO Auto-generated method stub
				double temp;
				temp = dacc / 100;
				if (temp > 100) {
					modulus(temp);
				} else {
					return temp;
				}
				return temp;
			}
		});
	}
}
