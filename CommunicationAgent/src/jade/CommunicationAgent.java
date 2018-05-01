package jade;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;

import org.opencv.core.KeyPoint;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import model.DirectoryModel;
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
	protected String mode = "";
	protected String img_info;
	protected int count;
	protected boolean alreadyExecuted = false;

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
					alreadyExecuted = false;
					String img_info = msg.getContent();
					System.out.println(img_info);
					ImgInfoModel.setImgInfo(img_info);
					String img_info_array[] = img_info.split("-");
					if (img_info_array.length <= 4) {
						mode = "dataset";
						DirectoryModel.setDir(img_info_array[img_info_array.length - 1]);
						String selected_option = img_info_array[img_info_array.length - 2];
						ObjectModel.set_file_name(img_info_array[img_info_array.length - 3]);
						ObjectModel.set_path(img_info_array[img_info_array.length - 4]);
						selection(selected_option, img_info);
					} else {
						mode = "normal";
						String selected_option = img_info_array[img_info_array.length - 6];
						ImageModel.setRow(Integer.parseInt(img_info_array[img_info_array.length - 5]));
						ImageModel.setCol(Integer.parseInt(img_info_array[img_info_array.length - 4]));
						ImageModel.setType(Integer.parseInt(img_info_array[img_info_array.length - 3]));
						ImageModel.set_file_name(img_info_array[img_info_array.length - 1]);
						ImageModel.set_path(img_info_array[img_info_array.length - 2]);
						ObjectModel.set_path(img_info_array[img_info_array.length - 8]);
						ObjectModel.set_file_name(img_info_array[img_info_array.length - 7]);
						selection(selected_option, img_info);
					}
				} else {
					block();
				}
			}
		});
	}

	public void selection(String selected_option, String img_info) {
		// extract content of image information and perform specific conversation
		String img_info_array[] = img_info.split("-");
		switch (selected_option) {
		case "recomm":
			mode = "sys_recomm";
			for (int i = 0; i < 3; i++) {
				String new_info = img_info.replace(img_info_array[img_info_array.length - 6], tags[0]);
				selection(tags[i], new_info);
			}
			break;
		case "canny":
			sendInfoToCanny(img_info);
			break;
		case "harris":
			sendInfoToHarris(img_info);
			break;
		case "shi_tomasi":
			sendInfoToShiTomasi(img_info);
			break;
		case "HC":
			sendInfoToCanny(img_info);
			sendInfoToHarris(img_info);
			break;
		case "HS":
			sendInfoToHarris(img_info);
			sendInfoToShiTomasi(img_info);
			break;
		case "SC":
			sendInfoToShiTomasi(img_info);
			sendInfoToCanny(img_info);
			break;
		case "HSC":
			sendInfoToHarris(img_info);
			sendInfoToShiTomasi(img_info);
			sendInfoToCanny(img_info);
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

					String msg_info = msg.getContent();
					String msg_info_array[] = msg_info.split("-");
					String msg_hybrid = msg_info_array[msg_info_array.length - 1];
					String img_info = ImgInfoModel.getImgInfo();
					String img_info_array[] = img_info.split("-");
					// analyze return result content and perform specific actions
					if (mode.equals("sys_recomm")) {

						if (!alreadyExecuted) {
							alreadyExecuted = true;
							CommunicationController.setHarrisObjPt(CommunicationController.keypoint_extraction(
									ObjectModel.get_path() + "/harris/" + ObjectModel.get_file_name(), "harris"));
							CommunicationController.setHarrisScenePt(CommunicationController.keypoint_extraction(
									ImageModel.get_path() + "/harris/" + ImageModel.get_file_name(), "harris"));

							CommunicationController.setCannyObjPt(CommunicationController.keypoint_extraction(
									ObjectModel.get_path() + "/canny/" + ObjectModel.get_file_name(), "canny"));
							CommunicationController.setCannyScenePt(CommunicationController.keypoint_extraction(
									ImageModel.get_path() + "/canny/" + ImageModel.get_file_name(), "canny"));

							CommunicationController.setShiTomasiObjPt(CommunicationController.keypoint_extraction(
									ObjectModel.get_path() + "/shitomasi/" + ObjectModel.get_file_name(),
									"shi_tomasi"));
							CommunicationController.setShiTomasiScenePt(CommunicationController.keypoint_extraction(
									ImageModel.get_path() + "/shitomasi/" + ImageModel.get_file_name(), "shi_tomasi"));

							List<KeyPoint> HCObjKeyPts = new ArrayList<KeyPoint>();
							HCObjKeyPts.addAll(CommunicationController.getCannyObjPt());
							HCObjKeyPts.addAll(CommunicationController.getHarrisObjPt());

							List<KeyPoint> HCSceneKeyPts = new ArrayList<KeyPoint>();
							HCSceneKeyPts.addAll(CommunicationController.getCannyScenePt());
							HCSceneKeyPts.addAll(CommunicationController.getHarrisScenePt());

							List<KeyPoint> HSObjKeyPts = new ArrayList<KeyPoint>();
							HSObjKeyPts.addAll(CommunicationController.getShiTomasiObjPt());
							HSObjKeyPts.addAll(CommunicationController.getHarrisObjPt());

							List<KeyPoint> HSSceneKeyPts = new ArrayList<KeyPoint>();
							HSSceneKeyPts.addAll(CommunicationController.getShiTomasiScenePt());
							HSSceneKeyPts.addAll(CommunicationController.getHarrisScenePt());

							List<KeyPoint> SCObjKeyPts = new ArrayList<KeyPoint>();
							SCObjKeyPts.addAll(CommunicationController.getCannyObjPt());
							SCObjKeyPts.addAll(CommunicationController.getShiTomasiObjPt());

							List<KeyPoint> SCSceneKeyPts = new ArrayList<KeyPoint>();
							SCSceneKeyPts.addAll(CommunicationController.getCannyScenePt());
							SCSceneKeyPts.addAll(CommunicationController.getShiTomasiScenePt());

							List<KeyPoint> HSCObjKeyPts = new ArrayList<KeyPoint>();
							HSCObjKeyPts.addAll(CommunicationController.getCannyObjPt());
							HSCObjKeyPts.addAll(CommunicationController.getHarrisObjPt());
							HSCObjKeyPts.addAll(CommunicationController.getShiTomasiObjPt());

							List<KeyPoint> HSCSceneKeyPts = new ArrayList<KeyPoint>();
							HSCSceneKeyPts.addAll(CommunicationController.getCannyScenePt());
							HSCSceneKeyPts.addAll(CommunicationController.getHarrisScenePt());
							HSCSceneKeyPts.addAll(CommunicationController.getShiTomasiScenePt());

							CommunicationController.matched_corners(CommunicationController.getCannyScenePt(),
									CommunicationController.getCannyObjPt(), "canny");
							percentages[0] = Double.toString(CommunicationController.computeAccuracy());
							System.out.println("canny: " + percentages[0]);

							CommunicationController.matched_corners(CommunicationController.getShiTomasiScenePt(),
									CommunicationController.getShiTomasiObjPt(), "shi_tomasi");
							percentages[1] = Double.toString(CommunicationController.computeAccuracy());
							System.out.println("shi_tomasi:" + percentages[1]);

							CommunicationController.matched_corners(CommunicationController.getHarrisScenePt(),
									CommunicationController.getHarrisObjPt(), "harris");
							percentages[2] = Double.toString(CommunicationController.computeAccuracy());
							System.out.println("harris:" + percentages[2]);

							CommunicationController.matched_corners(HCSceneKeyPts, HCObjKeyPts, "HC");
							percentages[3] = Double.toString(CommunicationController.computeAccuracy());
							System.out.println("HC:" + percentages[3]);

							CommunicationController.matched_corners(HSSceneKeyPts, HSObjKeyPts, "HS");
							percentages[4] = Double.toString(CommunicationController.computeAccuracy());
							System.out.println("HS:" + percentages[4]);

							CommunicationController.matched_corners(SCSceneKeyPts, SCObjKeyPts, "SC");
							percentages[5] = Double.toString(CommunicationController.computeAccuracy());
							System.out.println("SC:" + percentages[5]);

							CommunicationController.matched_corners(HSCSceneKeyPts, HSCObjKeyPts, "HSC");
							percentages[6] = Double.toString(CommunicationController.computeAccuracy());
							System.out.println("HSC:" + percentages[6]);

							int highest = CommunicationController.find_path(percentages);
							String new_info = img_info.replace(img_info_array[img_info_array.length - 6],
									tags[highest]);
							selection(tags[highest], new_info);

						}
						mode = "normal";
					} else if (mode.equals("normal")) {
						if (msg_hybrid.equals("canny")) {
							CommunicationController.setCannyObjPt(CommunicationController.keypoint_extraction(
									ObjectModel.get_path() + "/canny/" + ObjectModel.get_file_name(), "canny"));
							CommunicationController.setCannyScenePt(CommunicationController.keypoint_extraction(
									ImageModel.get_path() + "/canny/" + ImageModel.get_file_name(), "canny"));

							CommunicationController.matched_corners(CommunicationController.getCannyScenePt(),
									CommunicationController.getCannyObjPt(), "canny");

							String temp = Double.toString(CommunicationController.computeAccuracy());
							percentages[0] = temp;
							System.out.println("canny: " + percentages[0]);
							sendProcessedImage(temp + "-canny");
						} else if (msg_hybrid.equals("shi_tomasi")) {
							CommunicationController.setShiTomasiObjPt(CommunicationController.keypoint_extraction(
									ObjectModel.get_path() + "/shitomasi/" + ObjectModel.get_file_name(),
									"shi_tomasi"));
							CommunicationController.setShiTomasiScenePt(CommunicationController.keypoint_extraction(
									ImageModel.get_path() + "/shitomasi/" + ImageModel.get_file_name(), "shi_tomasi"));

							CommunicationController.matched_corners(CommunicationController.getShiTomasiScenePt(),
									CommunicationController.getShiTomasiObjPt(), "shi_tomasi");

							String temp = Double.toString(CommunicationController.computeAccuracy());
							percentages[1] = temp;
							System.out.println("shi_tomasi:" + percentages[1]);
							sendProcessedImage(temp + "-shi_tomasi");
						} else if (msg_hybrid.equals("harris")) {
							CommunicationController.setHarrisObjPt(CommunicationController.keypoint_extraction(
									ObjectModel.get_path() + "/harris/" + ObjectModel.get_file_name(), "harris"));
							CommunicationController.setHarrisScenePt(CommunicationController.keypoint_extraction(
									ImageModel.get_path() + "/harris/" + ImageModel.get_file_name(), "harris"));

							CommunicationController.matched_corners(CommunicationController.getHarrisScenePt(),
									CommunicationController.getHarrisObjPt(), "harris");

							String temp = Double.toString(CommunicationController.computeAccuracy());
							percentages[2] = temp;
							System.out.println("harris:" + percentages[2]);
							sendProcessedImage(temp + "-harris");
						} else if (msg_hybrid.equals("HC")) {

							if (!alreadyExecuted) {
								alreadyExecuted = true;
								CommunicationController.setHarrisObjPt(CommunicationController.keypoint_extraction(
										ObjectModel.get_path() + "/harris/" + ObjectModel.get_file_name(), "harris"));
								CommunicationController.setHarrisScenePt(CommunicationController.keypoint_extraction(
										ImageModel.get_path() + "/harris/" + ImageModel.get_file_name(), "harris"));

								CommunicationController.setCannyObjPt(CommunicationController.keypoint_extraction(
										ObjectModel.get_path() + "/canny/" + ObjectModel.get_file_name(), "canny"));
								CommunicationController.setCannyScenePt(CommunicationController.keypoint_extraction(
										ImageModel.get_path() + "/canny/" + ImageModel.get_file_name(), "canny"));
								List<KeyPoint> mergeObjKeyPts = new ArrayList<KeyPoint>();
								mergeObjKeyPts.addAll(CommunicationController.getCannyObjPt());
								mergeObjKeyPts.addAll(CommunicationController.getHarrisObjPt());

								List<KeyPoint> mergeSceneKeyPts = new ArrayList<KeyPoint>();
								mergeSceneKeyPts.addAll(CommunicationController.getCannyScenePt());
								mergeSceneKeyPts.addAll(CommunicationController.getHarrisScenePt());

								CommunicationController.matched_corners(mergeSceneKeyPts, mergeObjKeyPts, msg_hybrid);
								String temp = Double.toString(CommunicationController.computeAccuracy());
								percentages[3] = temp;
								System.out.println("HC:" + percentages[3]);
								sendProcessedImage(temp + "-HC");

							}
						} else if (msg_hybrid.equals("HS")) {

							if (!alreadyExecuted) {
								alreadyExecuted = true;
								CommunicationController.setHarrisObjPt(CommunicationController.keypoint_extraction(
										ObjectModel.get_path() + "/harris/" + ObjectModel.get_file_name(), "harris"));
								CommunicationController.setHarrisScenePt(CommunicationController.keypoint_extraction(
										ImageModel.get_path() + "/harris/" + ImageModel.get_file_name(), "harris"));

								CommunicationController.setShiTomasiObjPt(CommunicationController.keypoint_extraction(
										ObjectModel.get_path() + "/shitomasi/" + ObjectModel.get_file_name(),
										"shi_tomasi"));
								CommunicationController.setShiTomasiScenePt(CommunicationController.keypoint_extraction(
										ImageModel.get_path() + "/shitomasi/" + ImageModel.get_file_name(),
										"shi_tomasi"));
								List<KeyPoint> mergeObjKeyPts = new ArrayList<KeyPoint>();
								mergeObjKeyPts.addAll(CommunicationController.getShiTomasiObjPt());
								mergeObjKeyPts.addAll(CommunicationController.getHarrisObjPt());

								List<KeyPoint> mergeSceneKeyPts = new ArrayList<KeyPoint>();
								mergeSceneKeyPts.addAll(CommunicationController.getShiTomasiScenePt());
								mergeSceneKeyPts.addAll(CommunicationController.getHarrisScenePt());

								CommunicationController.matched_corners(mergeSceneKeyPts, mergeObjKeyPts, msg_hybrid);
								String temp = Double.toString(CommunicationController.computeAccuracy());
								percentages[4] = temp;
								System.out.println("HS:" + percentages[4]);
								sendProcessedImage(temp + "-HS");

							}
						} else if (msg_hybrid.equals("SC")) {

							if (!alreadyExecuted) {
								alreadyExecuted = true;
								CommunicationController.setCannyObjPt(CommunicationController.keypoint_extraction(
										ObjectModel.get_path() + "/canny/" + ObjectModel.get_file_name(), "canny"));
								CommunicationController.setCannyScenePt(CommunicationController.keypoint_extraction(
										ImageModel.get_path() + "/canny/" + ImageModel.get_file_name(), "canny"));

								CommunicationController.setShiTomasiObjPt(CommunicationController.keypoint_extraction(
										ObjectModel.get_path() + "/shitomasi/" + ObjectModel.get_file_name(),
										"shi_tomasi"));
								CommunicationController.setShiTomasiScenePt(CommunicationController.keypoint_extraction(
										ImageModel.get_path() + "/shitomasi/" + ImageModel.get_file_name(),
										"shi_tomasi"));
								List<KeyPoint> mergeObjKeyPts = new ArrayList<KeyPoint>();
								mergeObjKeyPts.addAll(CommunicationController.getShiTomasiObjPt());
								mergeObjKeyPts.addAll(CommunicationController.getCannyObjPt());

								List<KeyPoint> mergeSceneKeyPts = new ArrayList<KeyPoint>();
								mergeSceneKeyPts.addAll(CommunicationController.getShiTomasiScenePt());
								mergeSceneKeyPts.addAll(CommunicationController.getCannyScenePt());

								CommunicationController.matched_corners(mergeSceneKeyPts, mergeObjKeyPts, msg_hybrid);
								String temp = Double.toString(CommunicationController.computeAccuracy());
								percentages[5] = temp;
								System.out.println("SC:" + percentages[5]);
								sendProcessedImage(temp + "-SC");

							}
						} else if (msg_hybrid.equals("HSC")) {

							if (!alreadyExecuted) {
								alreadyExecuted = true;
								CommunicationController.setHarrisObjPt(CommunicationController.keypoint_extraction(
										ObjectModel.get_path() + "/harris/" + ObjectModel.get_file_name(), "harris"));
								CommunicationController.setHarrisScenePt(CommunicationController.keypoint_extraction(
										ImageModel.get_path() + "/harris/" + ImageModel.get_file_name(), "harris"));

								CommunicationController.setCannyObjPt(CommunicationController.keypoint_extraction(
										ObjectModel.get_path() + "/canny/" + ObjectModel.get_file_name(), "canny"));
								CommunicationController.setCannyScenePt(CommunicationController.keypoint_extraction(
										ImageModel.get_path() + "/canny/" + ImageModel.get_file_name(), "canny"));

								CommunicationController.setShiTomasiObjPt(CommunicationController.keypoint_extraction(
										ObjectModel.get_path() + "/shitomasi/" + ObjectModel.get_file_name(),
										"shi_tomasi"));
								CommunicationController.setShiTomasiScenePt(CommunicationController.keypoint_extraction(
										ImageModel.get_path() + "/shitomasi/" + ImageModel.get_file_name(),
										"shi_tomasi"));
								List<KeyPoint> mergeObjKeyPts = new ArrayList<KeyPoint>();
								mergeObjKeyPts.addAll(CommunicationController.getShiTomasiObjPt());
								mergeObjKeyPts.addAll(CommunicationController.getCannyObjPt());
								mergeObjKeyPts.addAll(CommunicationController.getHarrisObjPt());

								List<KeyPoint> mergeSceneKeyPts = new ArrayList<KeyPoint>();
								mergeSceneKeyPts.addAll(CommunicationController.getShiTomasiScenePt());
								mergeSceneKeyPts.addAll(CommunicationController.getCannyScenePt());
								mergeSceneKeyPts.addAll(CommunicationController.getHarrisScenePt());

								CommunicationController.matched_corners(mergeSceneKeyPts, mergeObjKeyPts, msg_hybrid);
								String temp = Double.toString(CommunicationController.computeAccuracy());
								percentages[6] = temp;
								System.out.println("HSC:" + percentages[6]);
								sendProcessedImage(temp + "-HSC");

							}
						}
					} else if (mode.equals("dataset")) {
						if (msg_hybrid.equals("canny")) {
							CommunicationController.setCannyObjPt(CommunicationController.keypoint_extraction(
									ObjectModel.get_path() + "/canny/" + ObjectModel.get_file_name(), "canny"));
							File folder = new File(DirectoryModel.getDir());
							File[] listOfFiles = folder.listFiles(new ImageFileFilter());

							for (File file : listOfFiles) {
								if (file.isFile()) {

									DirectoryModel.set_file_name(file.getName());

									CommunicationController.setCannyScenePt(CommunicationController.keypoint_extraction(
											DirectoryModel.getDir() + "/canny/" + DirectoryModel.get_file_name(),
											"canny"));
									CommunicationController.matched_corners_in_dataset(
											CommunicationController.getCannyScenePt(),
											CommunicationController.getCannyObjPt(), msg_hybrid);

									String temp = Double.toString(CommunicationController.computeAccuracy());
									if (Double.valueOf(temp) >= 80 && Double.valueOf(temp) < 100) {
										count++;
										System.out.println("Detected: " + count);
									} else {
										System.out.println("No Detected");
									}

								} else if (file.isDirectory()) {
									System.out.println("Directory " + file.getName());
								}
							}
							sendProcessedImage(count + "-canny");
							count = 0;
						} else if (msg_hybrid.equals("harris")) {
							CommunicationController.setHarrisObjPt(CommunicationController.keypoint_extraction(
									ObjectModel.get_path() + "/harris/" + ObjectModel.get_file_name(), "harris"));
							File folder = new File(DirectoryModel.getDir());
							File[] listOfFiles = folder.listFiles(new ImageFileFilter());

							for (File file : listOfFiles) {
								if (file.isFile()) {

									DirectoryModel.set_file_name(file.getName());

									CommunicationController.setHarrisScenePt(
											CommunicationController.keypoint_extraction(DirectoryModel.getDir()
													+ "/harris/" + DirectoryModel.get_file_name(), "harris"));
									CommunicationController.matched_corners_in_dataset(
											CommunicationController.getHarrisScenePt(),
											CommunicationController.getHarrisObjPt(), msg_hybrid);

									String temp = Double.toString(CommunicationController.computeAccuracy());
									if (Double.valueOf(temp) >= 80 && Double.valueOf(temp) < 100) {
										count++;
										System.out.println("Detected: " + count);
									} else {
										System.out.println("No Detected");
									}
								} else if (file.isDirectory()) {
									System.out.println("Directory " + file.getName());
								}
							}
							sendProcessedImage(count + "-harris");
							count = 0;
						} else if (msg_hybrid.equals("shi_tomasi")) {
							CommunicationController.setShiTomasiObjPt(CommunicationController.keypoint_extraction(
									ObjectModel.get_path() + "/shitomasi/" + ObjectModel.get_file_name(),
									"shi_tomasi"));
							File folder = new File(DirectoryModel.getDir());
							File[] listOfFiles = folder.listFiles(new ImageFileFilter());

							for (File file : listOfFiles) {
								if (file.isFile()) {

									DirectoryModel.set_file_name(file.getName());

									CommunicationController.setShiTomasiScenePt(
											CommunicationController.keypoint_extraction(DirectoryModel.getDir()
													+ "/shitomasi/" + DirectoryModel.get_file_name(), "shi_tomasi"));
									CommunicationController.matched_corners_in_dataset(
											CommunicationController.getShiTomasiScenePt(),
											CommunicationController.getShiTomasiObjPt(), msg_hybrid);

									String temp = Double.toString(CommunicationController.computeAccuracy());
									if (Double.valueOf(temp) >= 80 && Double.valueOf(temp) < 100) {
										count++;
										System.out.println("Detected: " + count);
									} else {
										System.out.println("No Detected");
									}
								} else if (file.isDirectory()) {
									System.out.println("Directory " + file.getName());
								}
							}
							sendProcessedImage(count + "-shi_tomasi");
							count = 0;
						} else if (msg_hybrid.equals("HC")) {

							if (!alreadyExecuted) {
								alreadyExecuted = true;
								CommunicationController.setHarrisObjPt(CommunicationController.keypoint_extraction(
										ObjectModel.get_path() + "/harris/" + ObjectModel.get_file_name(), "harris"));
								CommunicationController.setCannyObjPt(CommunicationController.keypoint_extraction(
										ObjectModel.get_path() + "/canny/" + ObjectModel.get_file_name(), "canny"));

								List<KeyPoint> mergeObjKeyPts = new ArrayList<KeyPoint>();
								mergeObjKeyPts.addAll(CommunicationController.getCannyObjPt());
								mergeObjKeyPts.addAll(CommunicationController.getHarrisObjPt());

								File folder = new File(DirectoryModel.getDir());
								File[] listOfFiles = folder.listFiles(new ImageFileFilter());

								for (File file : listOfFiles) {
									if (file.isFile()) {

										DirectoryModel.set_file_name(file.getName());

										CommunicationController.setHarrisScenePt(
												CommunicationController.keypoint_extraction(DirectoryModel.getDir()
														+ "/harris/" + DirectoryModel.get_file_name(), "harris"));
										CommunicationController.setCannyScenePt(
												CommunicationController.keypoint_extraction(ObjectModel.get_path()
														+ "/canny/" + ObjectModel.get_file_name(), "canny"));

										List<KeyPoint> mergeSceneKeyPts = new ArrayList<KeyPoint>();
										mergeSceneKeyPts.addAll(CommunicationController.getCannyObjPt());
										mergeSceneKeyPts.addAll(CommunicationController.getHarrisObjPt());

										CommunicationController.matched_corners_in_dataset(mergeSceneKeyPts,
												mergeObjKeyPts, msg_hybrid);

										String temp = Double.toString(CommunicationController.computeAccuracy());
										if (Double.valueOf(temp) >= 80 && Double.valueOf(temp) < 100) {
											count++;
											System.out.println("Detected: " + count);
										} else {
											System.out.println("No Detected");
										}
									} else if (file.isDirectory()) {
										System.out.println("Directory " + file.getName());
									}
								}
								sendProcessedImage(count + "-HC");
								count = 0;
							}
						} else if (msg_hybrid.equals("HS")) {

							if (!alreadyExecuted) {
								alreadyExecuted = true;
								CommunicationController.setHarrisObjPt(CommunicationController.keypoint_extraction(
										ObjectModel.get_path() + "/harris/" + ObjectModel.get_file_name(), "harris"));
								CommunicationController.setShiTomasiObjPt(CommunicationController.keypoint_extraction(
										ObjectModel.get_path() + "/shitomasi/" + ObjectModel.get_file_name(),
										"shi_tomasi"));

								List<KeyPoint> mergeObjKeyPts = new ArrayList<KeyPoint>();
								mergeObjKeyPts.addAll(CommunicationController.getShiTomasiObjPt());
								mergeObjKeyPts.addAll(CommunicationController.getHarrisObjPt());

								File folder = new File(DirectoryModel.getDir());
								File[] listOfFiles = folder.listFiles(new ImageFileFilter());

								for (File file : listOfFiles) {
									if (file.isFile()) {

										DirectoryModel.set_file_name(file.getName());

										CommunicationController.setHarrisScenePt(
												CommunicationController.keypoint_extraction(DirectoryModel.getDir()
														+ "/harris/" + DirectoryModel.get_file_name(), "harris"));
										CommunicationController.setShiTomasiScenePt(
												CommunicationController.keypoint_extraction(ObjectModel.get_path()
														+ "/shitomasi/" + ObjectModel.get_file_name(), "shi_tomasi"));

										List<KeyPoint> mergeSceneKeyPts = new ArrayList<KeyPoint>();
										mergeSceneKeyPts.addAll(CommunicationController.getShiTomasiObjPt());
										mergeSceneKeyPts.addAll(CommunicationController.getHarrisObjPt());

										CommunicationController.matched_corners_in_dataset(mergeSceneKeyPts,
												mergeObjKeyPts, msg_hybrid);

										String temp = Double.toString(CommunicationController.computeAccuracy());
										if (Double.valueOf(temp) >= 80 && Double.valueOf(temp) < 100) {
											count++;
											System.out.println("Detected: " + count);
										} else {
											System.out.println("No Detected");
										}
									} else if (file.isDirectory()) {
										System.out.println("Directory " + file.getName());
									}
								}
								sendProcessedImage(count + "-HS");
								count = 0;
							}

						} else if (msg_hybrid.equals("SC")) {

							if (!alreadyExecuted) {
								alreadyExecuted = true;
								CommunicationController.setShiTomasiObjPt(CommunicationController.keypoint_extraction(
										ObjectModel.get_path() + "/shitomasi/" + ObjectModel.get_file_name(),
										"shi_tomasi"));
								CommunicationController.setCannyObjPt(CommunicationController.keypoint_extraction(
										ObjectModel.get_path() + "/canny/" + ObjectModel.get_file_name(), "canny"));

								List<KeyPoint> mergeObjKeyPts = new ArrayList<KeyPoint>();
								mergeObjKeyPts.addAll(CommunicationController.getCannyObjPt());
								mergeObjKeyPts.addAll(CommunicationController.getShiTomasiObjPt());

								File folder = new File(DirectoryModel.getDir());
								File[] listOfFiles = folder.listFiles(new ImageFileFilter());

								for (File file : listOfFiles) {
									if (file.isFile()) {

										DirectoryModel.set_file_name(file.getName());

										CommunicationController
												.setShiTomasiScenePt(
														CommunicationController.keypoint_extraction(
																DirectoryModel.getDir() + "/shitomasi/"
																		+ DirectoryModel.get_file_name(),
																"shi_tomasi"));
										CommunicationController.setCannyScenePt(
												CommunicationController.keypoint_extraction(ObjectModel.get_path()
														+ "/canny/" + ObjectModel.get_file_name(), "canny"));

										List<KeyPoint> mergeSceneKeyPts = new ArrayList<KeyPoint>();
										mergeSceneKeyPts.addAll(CommunicationController.getCannyObjPt());
										mergeSceneKeyPts.addAll(CommunicationController.getShiTomasiObjPt());

										CommunicationController.matched_corners_in_dataset(mergeSceneKeyPts,
												mergeObjKeyPts, msg_hybrid);

										String temp = Double.toString(CommunicationController.computeAccuracy());
										if (Double.valueOf(temp) >= 80 && Double.valueOf(temp) < 100) {
											count++;
											System.out.println("Detected: " + count);
										} else {
											System.out.println("No Detected");
										}
									} else if (file.isDirectory()) {
										System.out.println("Directory " + file.getName());
									}
								}
								sendProcessedImage(count + "-SC");
								count = 0;
							}

						} else if (msg_hybrid.equals("HSC")) {

							if (!alreadyExecuted) {
								alreadyExecuted = true;
								CommunicationController.setHarrisObjPt(CommunicationController.keypoint_extraction(
										ObjectModel.get_path() + "/harris/" + ObjectModel.get_file_name(), "harris"));
								CommunicationController.setCannyObjPt(CommunicationController.keypoint_extraction(
										ObjectModel.get_path() + "/canny/" + ObjectModel.get_file_name(), "canny"));
								CommunicationController.setShiTomasiObjPt(CommunicationController.keypoint_extraction(
										ObjectModel.get_path() + "/shitomasi/" + ObjectModel.get_file_name(),
										"shi_tomasi"));

								List<KeyPoint> mergeObjKeyPts = new ArrayList<KeyPoint>();
								mergeObjKeyPts.addAll(CommunicationController.getCannyObjPt());
								mergeObjKeyPts.addAll(CommunicationController.getHarrisObjPt());
								mergeObjKeyPts.addAll(CommunicationController.getShiTomasiObjPt());

								File folder = new File(DirectoryModel.getDir());
								File[] listOfFiles = folder.listFiles(new ImageFileFilter());

								for (File file : listOfFiles) {
									if (file.isFile()) {

										DirectoryModel.set_file_name(file.getName());

										CommunicationController.setHarrisScenePt(
												CommunicationController.keypoint_extraction(DirectoryModel.getDir()
														+ "/harris/" + DirectoryModel.get_file_name(), "harris"));
										CommunicationController.setCannyScenePt(
												CommunicationController.keypoint_extraction(ObjectModel.get_path()
														+ "/canny/" + ObjectModel.get_file_name(), "canny"));
										CommunicationController.setShiTomasiScenePt(
												CommunicationController.keypoint_extraction(ObjectModel.get_path()
														+ "/shitomasi/" + ObjectModel.get_file_name(), "shi_tomasi"));

										List<KeyPoint> mergeSceneKeyPts = new ArrayList<KeyPoint>();
										mergeSceneKeyPts.addAll(CommunicationController.getCannyObjPt());
										mergeSceneKeyPts.addAll(CommunicationController.getHarrisObjPt());
										mergeSceneKeyPts.addAll(CommunicationController.getShiTomasiObjPt());

										CommunicationController.matched_corners_in_dataset(mergeSceneKeyPts,
												mergeObjKeyPts, msg_hybrid);

										String temp = Double.toString(CommunicationController.computeAccuracy());
										if (Double.valueOf(temp) >= 80 && Double.valueOf(temp) < 100) {
											count++;
											System.out.println("Detected: " + count);
										} else {
											System.out.println("No Detected");
										}
									} else if (file.isDirectory()) {
										System.out.println("Directory " + file.getName());
									}
								}
								sendProcessedImage(count + "-HSC");
								count = 0;
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

	/**
	 * A class that implements the Java FileFilter interface.
	 */
	public class ImageFileFilter implements FileFilter {
		private final String[] okFileExtensions = new String[] { "jpg", "png", "tif" };

		public boolean accept(File file) {
			for (String extension : okFileExtensions) {
				if (file.getName().toLowerCase().endsWith(extension)) {
					return true;
				}
			}
			return false;
		}
	}
}
