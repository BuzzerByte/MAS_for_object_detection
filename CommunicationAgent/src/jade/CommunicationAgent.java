package jade;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import model.ImageModel;
import model.ImgInfoModel;
import model.ObjectModel;
import viewController.CommunicationController;
import viewController.CommunicationGUI;

@SuppressWarnings("serial")
public class CommunicationAgent extends Agent {
	private CommunicationController controller;
	private static CommunicationGUI gui;
	String percentages[] = new String[7];
	String tags[] = { "canny", "shi_tomasi", "harris", "HC", "HS", "SC", "HSC" };
	public double accuracy[] = new double[7];
	int i = 0;
	private String hybrid_tag = "";
	protected String sys_recomm = "unavailable";
	protected String img_info;

	/*
	 * Get the agent controller
	 */
	public CommunicationController getController() {
		return controller;
	}

	/*
	 * Set the agent controller
	 */
	public void setController(CommunicationController controller) {
		this.controller = controller;
	}

	/*
	 * Setup the agent property
	 */
	protected void setup() {
		gui = new CommunicationGUI();
		gui.setAgent(this);
		new Thread() {
			@Override
			public void run() {
				CommunicationGUI.main(null);
			}
		}.start();
		receiveImageInfo();
		receiveProcessedImage();
		agent_availability();
	}

	/*
	 * Agent clean up operation
	 */
	protected void takeDown() {
		gui.dispose();
		System.out.println("Pre-Processing agent " + getAID().getName() + " terminating.");
	}

	/*
	 * Check agent availability
	 */
	public void agent_availability() {
		addBehaviour(new OneShotBehaviour(this) {
			@Override
			public void action() {
				ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
				msg.addReceiver(new AID("preprocessing", AID.ISLOCALNAME));
				msg.setContent("Communication agent activated");
				msg.setConversationId("availability");
				send(msg);
			}
		});
	}

	/*
	 * perform image processing task when message is received from pre-processing
	 * agent
	 */
	public void receiveImageInfo() {
		addBehaviour(new CyclicBehaviour(this) {
			@Override
			public void action() {
				MessageTemplate mt = MessageTemplate.MatchConversationId("img_info");
				ACLMessage msg = receive(mt);
				if (msg != null) {

					String img_info = msg.getContent();
					System.out.println(img_info);
					ImgInfoModel.setImgInfo(img_info);
					String img_info_array[] = img_info.split("-");
					String selected_option = img_info_array[img_info_array.length - 6];

					ImageModel.setRow(Integer.parseInt(img_info_array[img_info_array.length - 5]));
					ImageModel.setCol(Integer.parseInt(img_info_array[img_info_array.length - 4]));
					ImageModel.setType(Integer.parseInt(img_info_array[img_info_array.length - 3]));
					ImageModel.set_file_name(img_info_array[img_info_array.length - 1]);
					ImageModel.set_path(img_info_array[img_info_array.length - 2]);
					ObjectModel.set_path(img_info_array[img_info_array.length - 8]);
					ObjectModel.set_file_name(img_info_array[img_info_array.length - 7]);
					selection(selected_option, img_info);
				} else {
					block();
				}
			}
		});
	}

	public void selection(String selected_option, String img_info) {
		System.out.println(selected_option);
		// extract content of image information and perform specific conversation
		String img_info_array[] = img_info.split("-");
		switch (selected_option) {
		case "recomm":
			sys_recomm = "available";
			for (int i = 0; i < 3; i++) {
				String new_info = img_info.replace(img_info_array[img_info_array.length - 6], tags[0]);
				selection(tags[i], new_info);
			}
			break;
		case "canny":
			if (hybrid_tag.equals("HC")) {
				sendInfoToCanny(img_info);
			} else if (hybrid_tag.equals("SC")) {
				sendInfoToCanny(img_info);
			} else {
				sendInfoToCanny(img_info);
			}
			break;
		case "harris":
			sendInfoToHarris(img_info);
			// hybrid_tag = selected_option;
			break;
		case "shi_tomasi":
			if (hybrid_tag.equals("HS")) {
				sendInfoToShiTomasi(img_info);
			} else {
				sendInfoToShiTomasi(img_info);
			}
			break;
		case "HC":
			hybrid_tag = selected_option;
			sendInfoToHarris(img_info);
			break;
		case "HS":
			hybrid_tag = selected_option;
			sendInfoToHarris(img_info);
			break;
		case "SC":
			hybrid_tag = selected_option;
			sendInfoToShiTomasi(img_info);
			break;
		case "HSC":
			hybrid_tag = selected_option;
			sendInfoToHarris(img_info);
			break;
		default:
			System.out.println(selected_option);
			System.out.println("nothing is selected");
		}
		System.out.println("Image information received at communication agent");
	}

	/*
	 * Receive result from recognition agents
	 */
	private void receiveProcessedImage() {
		// TODO Auto-generated method stub
		addBehaviour(new CyclicBehaviour(this) {
			@Override
			public void action() {
				MessageTemplate mt = MessageTemplate.MatchConversationId("processed_status");
				ACLMessage msg = receive(mt);
				if (msg != null) {
					String object = ObjectModel.get_path() + "/" + ObjectModel.get_file_name();
					String scene = ImageModel.get_path() + "/" + ImageModel.get_file_name();

					String msg_info = msg.getContent();
					String msg_info_array[] = msg_info.split("-");
					String msg_hybrid = msg_info_array[msg_info_array.length - 1];
					String img_info = ImgInfoModel.getImgInfo();
					String img_info_array[] = img_info.split("-");

					// analyze return result content and perform specific actions
					if (sys_recomm.equals("available")) {
						if (msg_hybrid.equals("canny")) {
							CommunicationController.setCannyScenePt(CommunicationController.keypoint_extraction(
									ImageModel.get_path() + "/" + ImageModel.get_file_name(), "canny"));
							CommunicationController.setCannyObjPt(CommunicationController.keypoint_extraction(
									ObjectModel.get_path() + "/" + ObjectModel.get_file_name(), "canny"));
							System.out.println("canny saved");
							CommunicationController.matched_corners(object, scene, msg_hybrid);
							String temp = Double.toString(CommunicationController.computeAccuracy());
							percentages[0] = temp;
							System.out.println("canny: " + percentages[0]);
						} else if (msg_hybrid.equals("harris")) {
							CommunicationController.setHarrisScenePt(CommunicationController.keypoint_extraction(
									ImageModel.get_path() + "/" + ImageModel.get_file_name(), "harris"));
							CommunicationController.setHarrisObjPt(CommunicationController.keypoint_extraction(
									ObjectModel.get_path() + "/" + ObjectModel.get_file_name(), "harris"));
							System.out.println("harris saved");
							CommunicationController.matched_corners(object, scene, msg_hybrid);
							String temp = Double.toString(CommunicationController.computeAccuracy());
							percentages[2] = temp;
							System.out.println("Harris: " + percentages[2]);

							percentages[3] = String.valueOf(CommunicationController.compute_hybrid(
									CommunicationController.getCannyScenePt(),
									CommunicationController.getHarrisScenePt(), CommunicationController.getCannyObjPt(),
									CommunicationController.getHarrisObjPt(), "HC"));
							System.out.println("HC: " + percentages[3]);

							percentages[5] = String.valueOf(
									CommunicationController.compute_hybrid(CommunicationController.getCannyScenePt(),
											CommunicationController.getShiTomasiScenePt(),
											CommunicationController.getCannyObjPt(),
											CommunicationController.getShiTomasiObjPt(), "SC"));
							System.out.println("SC: " + percentages[5]);

							percentages[4] = String.valueOf(CommunicationController.compute_hybrid(
									CommunicationController.getShiTomasiScenePt(),
									CommunicationController.getHarrisScenePt(),
									CommunicationController.getShiTomasiObjPt(),
									CommunicationController.getHarrisObjPt(), "HS"));
							System.out.println("HS: " + percentages[4]);

							percentages[6] = String.valueOf(CommunicationController.compute_hybrid_HSC(
									CommunicationController.getCannyScenePt(),
									CommunicationController.getShiTomasiScenePt(),
									CommunicationController.getCannyObjPt(),
									CommunicationController.getShiTomasiObjPt(),
									CommunicationController.getHarrisObjPt(),
									CommunicationController.getHarrisScenePt(), "HSC"));
							System.out.println("HSC: " + percentages[6]);

							int highest = CommunicationController.find_path(percentages);
							String new_info = img_info.replace(img_info_array[img_info_array.length - 6],
									tags[highest]);
							selection(tags[highest], new_info);
							sys_recomm = "";
						} else if (msg_hybrid.equals("shi_tomasi")) {
							CommunicationController.setShiTomasiScenePt(CommunicationController.keypoint_extraction(
									ImageModel.get_path() + "/" + ImageModel.get_file_name(), "shi_tomasi"));
							CommunicationController.setShiTomasiObjPt(CommunicationController.keypoint_extraction(
									ObjectModel.get_path() + "/" + ObjectModel.get_file_name(), "shi_tomasi"));
							System.out.println("shitomasi saved");
							CommunicationController.matched_corners(object, scene, msg_hybrid);
							String temp = Double.toString(CommunicationController.computeAccuracy());
							percentages[1] = temp;
							System.out.println("Shi-Tomasi: " + percentages[1]);
						}
					} else {
						if (msg_hybrid.equals("canny")) {
							if (hybrid_tag.equals("HC")) {
								CommunicationController.setCannyScenePt(CommunicationController.keypoint_extraction(
										ImageModel.get_path() + "/" + ImageModel.get_file_name(), "canny"));
								CommunicationController.setCannyObjPt(CommunicationController.keypoint_extraction(
										ObjectModel.get_path() + "/" + ObjectModel.get_file_name(), "canny"));

								percentages[3] = String.valueOf(CommunicationController.compute_hybrid(
										CommunicationController.getCannyScenePt(),
										CommunicationController.getHarrisScenePt(),
										CommunicationController.getCannyObjPt(),
										CommunicationController.getHarrisObjPt(), hybrid_tag));

								sendProcessedImage(percentages[3] + "-HC");
								hybrid_tag = "";
							} else if (hybrid_tag.equals("SC")) {
								CommunicationController.setCannyScenePt(CommunicationController.keypoint_extraction(
										ImageModel.get_path() + "/" + ImageModel.get_file_name(), "canny"));
								CommunicationController.setCannyObjPt(CommunicationController.keypoint_extraction(
										ObjectModel.get_path() + "/" + ObjectModel.get_file_name(), "canny"));

								percentages[5] = String.valueOf(CommunicationController.compute_hybrid(
										CommunicationController.getCannyScenePt(),
										CommunicationController.getShiTomasiScenePt(),
										CommunicationController.getCannyObjPt(),
										CommunicationController.getShiTomasiObjPt(), hybrid_tag));
								sendProcessedImage(percentages[5] + "-SC");
								hybrid_tag = "";
							} else if (hybrid_tag.equals("HSC")) {
								CommunicationController.setCannyScenePt(CommunicationController.keypoint_extraction(
										ImageModel.get_path() + "/" + ImageModel.get_file_name(), "canny"));
								CommunicationController.setCannyObjPt(CommunicationController.keypoint_extraction(
										ObjectModel.get_path() + "/" + ObjectModel.get_file_name(), "canny"));

								percentages[6] = String.valueOf(CommunicationController.compute_hybrid_HSC(
										CommunicationController.getCannyScenePt(),
										CommunicationController.getShiTomasiScenePt(),
										CommunicationController.getHarrisScenePt(),
										CommunicationController.getCannyObjPt(),
										CommunicationController.getShiTomasiObjPt(),
										CommunicationController.getHarrisObjPt(), hybrid_tag));
								sendProcessedImage(percentages[6] + "-HSC");
								hybrid_tag = "";
							} else {
								CommunicationController.matched_corners(object, scene, msg_hybrid);
								String temp = Double.toString(CommunicationController.computeAccuracy());
								percentages[0] = temp;
								System.out.println("canny: " + percentages[0]);
								sendProcessedImage(temp + "-canny");
							}
						} else if (msg_hybrid.equals("shi_tomasi")) {
							if (hybrid_tag.equals("SC")) {
								CommunicationController.setShiTomasiScenePt(CommunicationController.keypoint_extraction(
										ImageModel.get_path() + "/" + ImageModel.get_file_name(), "shi_tomasi"));
								CommunicationController.setShiTomasiObjPt(CommunicationController.keypoint_extraction(
										ObjectModel.get_path() + "/" + ObjectModel.get_file_name(), "shi_tomasi"));
								CommunicationController.matched_corners(object, scene, msg_hybrid);
								String temp = Double.toString(CommunicationController.computeAccuracy());
								percentages[1] = temp;
								System.out.println("shi-tomasi:" + percentages[1]);
								String new_info = img_info.replace(img_info_array[img_info_array.length - 6], tags[0]);
								selection("canny", new_info);
							} else if (hybrid_tag.equals("HS")) {
								CommunicationController.setShiTomasiScenePt(CommunicationController.keypoint_extraction(
										ImageModel.get_path() + "/" + ImageModel.get_file_name(), "shi_tomasi"));
								CommunicationController.setShiTomasiObjPt(CommunicationController.keypoint_extraction(
										ObjectModel.get_path() + "/" + ObjectModel.get_file_name(), "shi_tomasi"));

								percentages[4] = String.valueOf(CommunicationController.compute_hybrid(
										CommunicationController.getShiTomasiScenePt(),
										CommunicationController.getHarrisScenePt(),
										CommunicationController.getShiTomasiObjPt(),
										CommunicationController.getHarrisObjPt(), hybrid_tag));
								sendProcessedImage(percentages[4] + "-HS");
								hybrid_tag = "";
							} else if (hybrid_tag.equals("HSC")) {
								CommunicationController.setShiTomasiScenePt(CommunicationController.keypoint_extraction(
										ImageModel.get_path() + "/" + ImageModel.get_file_name(), "shi_tomasi"));
								CommunicationController.setShiTomasiObjPt(CommunicationController.keypoint_extraction(
										ObjectModel.get_path() + "/" + ObjectModel.get_file_name(), "shi_tomasi"));

								CommunicationController.matched_corners(object, scene, msg_hybrid);
								String temp = Double.toString(CommunicationController.computeAccuracy());
								percentages[1] = temp;
								System.out.println("shi_tomasi:" + percentages[1]);
								String new_info = img_info.replace(img_info_array[img_info_array.length - 6], tags[0]);
								selection("canny", new_info);
							} else {
								CommunicationController.matched_corners(object, scene, msg_hybrid);
								String temp = Double.toString(CommunicationController.computeAccuracy());
								percentages[1] = temp;
								System.out.println("shi_tomasi:" + percentages[1]);
								sendProcessedImage(temp + "-shi_tomasi");
								hybrid_tag = "";
							}
						} else if (msg_hybrid.equals("harris")) {
							if (hybrid_tag.equals("HC")) {
								CommunicationController.matched_corners(object, scene, msg_hybrid);
								String temp = Double.toString(CommunicationController.computeAccuracy());
								percentages[2] = temp;
								System.out.println("harris:" + percentages[2]);
								CommunicationController.setHarrisScenePt(CommunicationController.keypoint_extraction(
										ImageModel.get_path() + "/" + ImageModel.get_file_name(), "harris"));
								CommunicationController.setHarrisObjPt(CommunicationController.keypoint_extraction(
										ObjectModel.get_path() + "/" + ObjectModel.get_file_name(), "harris"));
								String new_info = img_info.replace(img_info_array[img_info_array.length - 6], tags[0]);
								selection("canny", new_info);
							} else if (hybrid_tag.equals("HS")) {
								CommunicationController.matched_corners(object, scene, msg_hybrid);
								String temp = Double.toString(CommunicationController.computeAccuracy());
								percentages[2] = temp;
								System.out.println("harris:" + percentages[2]);
								CommunicationController.setHarrisScenePt(CommunicationController.keypoint_extraction(
										ImageModel.get_path() + "/" + ImageModel.get_file_name(), "harris"));
								CommunicationController.setHarrisObjPt(CommunicationController.keypoint_extraction(
										ObjectModel.get_path() + "/" + ObjectModel.get_file_name(), "harris"));
								String new_info = img_info.replace(img_info_array[img_info_array.length - 6], tags[2]);
								selection("shi_tomasi", new_info);
							} else if (hybrid_tag.equals("HSC")) {
								CommunicationController.setHarrisScenePt(CommunicationController.keypoint_extraction(
										ImageModel.get_path() + "/" + ImageModel.get_file_name(), "harris"));
								CommunicationController.setHarrisObjPt(CommunicationController.keypoint_extraction(
										ObjectModel.get_path() + "/" + ObjectModel.get_file_name(), "harris"));

								CommunicationController.matched_corners(object, scene, msg_hybrid);
								String temp = Double.toString(CommunicationController.computeAccuracy());
								percentages[2] = temp;
								System.out.println("harris:" + percentages[2]);
								String new_info = img_info.replace(img_info_array[img_info_array.length - 6], tags[6]);
								selection("shi_tomasi", new_info);
							} else {
								CommunicationController.matched_corners(object, scene, msg_hybrid);
								String temp = Double.toString(CommunicationController.computeAccuracy());
								percentages[2] = temp;
								System.out.println("harris:" + percentages[2]);
								sendProcessedImage(temp + "-harris");
								hybrid_tag = "";
							}
						}
					}
				} else {
					block();
				}
			}

		});

	}

	/*
	 * Send image information to Canny Agent
	 */
	public void sendInfoToCanny(String img_info) {
		addBehaviour(new OneShotBehaviour(this) {
			@Override
			public void action() {
				// TODO Auto-generated method stub
				ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
				msg.addReceiver(new AID("CannyAgent", AID.ISLOCALNAME));
				msg.setContent(img_info);
				msg.setConversationId("canny_img_info");
				send(msg);
				System.out.println("Image information send to Canny Agent");
			}
		});
	}

	/*
	 * Send image information to Harris Agent
	 */
	public void sendInfoToHarris(String img_info) {
		addBehaviour(new OneShotBehaviour(this) {
			@Override
			public void action() {
				// TODO Auto-generated method stub
				ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
				msg.addReceiver(new AID("HarrisAgent", AID.ISLOCALNAME));
				msg.setContent(img_info);
				msg.setConversationId("harris_img_info");
				send(msg);
				System.out.println("Image information send to Harris Agent");
			}
		});
	}

	/*
	 * Send image information to Shi-Tomasi Agent
	 */
	public void sendInfoToShiTomasi(String img_info) {
		addBehaviour(new OneShotBehaviour(this) {
			@Override
			public void action() {
				// TODO Auto-generated method stub
				ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
				msg.addReceiver(new AID("ShiTomasiAgent", AID.ISLOCALNAME));
				msg.setContent(img_info);
				msg.setConversationId("shitomasi_img_info");
				send(msg);
				System.out.println("Image information send to ShiTomasi Agent");
			}
		});
	}

	/*
	 * When the object detection is completed, return the result to pre-processing
	 * agent
	 */
	protected void sendProcessedImage(String content) {
		// TODO Auto-generated method stub
		addBehaviour(new OneShotBehaviour(this) {
			@Override
			public void action() {
				// TODO Auto-generated method stub
				ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
				msg.addReceiver(new AID("preprocessing", AID.ISLOCALNAME));
				msg.setContent(content);
				msg.setConversationId("processed_result");
				send(msg);
				System.out.println("Process_status_send\n");
			}
		});
	}
}
